package ai.decider;

import ai.movement.IPathfinder;
import engine.actions.*;
import engine.context.TurnContext;
import engine.core.GridGeometry;
import engine.state.Combatant;
import engine.state.GameState;
import engine.state.Vector2d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AggressiveDecider implements IDecider {
    private static final double DISADVANTAGE_PENALTY = 0.7;
    private final IPathfinder pathfinder;

    public AggressiveDecider(IPathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    private record ActionCandidate(
            IActionDefinition action,
            Vector2d targetTile,
            double expectedScore,
            boolean alreadyInRange,
            List<Vector2d> pathToAttack,
            int movementCostInSquares
    ) {}

    @Override
    public IAction decideNextAction(Combatant combatant, TurnContext turnContext) {
        GameState state = turnContext.getGameState();

        List<Combatant> enemies = getLivingEnemies(combatant, state);
        if (enemies.isEmpty()) { return null; }

        // PHASE 1: Looking for the best action THIS TURN
        List<IActionDefinition> usableDefinitions = getAvailableActionDefinitions(combatant, turnContext);
        List<ActionCandidate> currentTurnCandidates = generateCandidates(combatant, enemies, usableDefinitions, state);

        if (!currentTurnCandidates.isEmpty()) {
            currentTurnCandidates.sort(
                    Comparator.comparingDouble(ActionCandidate::expectedScore).reversed()
                            .thenComparingInt(ActionCandidate::movementCostInSquares)
            );

            int remainingMove = turnContext.getMovementRemaining();
            for (ActionCandidate candidate : currentTurnCandidates) {
                if (candidate.alreadyInRange()) {
                    return candidate.action().createAction(combatant, candidate.targetTile());
                }

                if (remainingMove > 0 && candidate.movementCostInSquares() <= remainingMove) {
                    return new MoveAction(combatant, candidate.pathToAttack());
                }
            }

            // Fallback PHASE 1: run towards the best attack this turn even if we don't arrive now
            if (remainingMove > 0) {
                ActionCandidate bestUnreachable = currentTurnCandidates.getFirst();
                if (!bestUnreachable.pathToAttack().isEmpty()) {
                    return new MoveAction(combatant, bestUnreachable.pathToAttack());
                }
            }
        }


        // PHASE 2: Repositioning for the next turn - if we used all actions this turn
        if (turnContext.getMovementRemaining() > 0) {
            // all actions available - TODO - thinking about which will be available next turn
            List<IActionDefinition> allDefinitions = combatant.getAvailableActions();
            List<ActionCandidate> futureCandidates = generateCandidates(combatant, enemies, allDefinitions, state);

            if (!futureCandidates.isEmpty()) {
                futureCandidates.sort(
                        Comparator.comparingDouble(ActionCandidate::expectedScore).reversed()
                                .thenComparingInt(ActionCandidate::movementCostInSquares)
                );

                ActionCandidate bestFuture = futureCandidates.getFirst();
                if (!bestFuture.alreadyInRange() && !bestFuture.pathToAttack().isEmpty()) {
                    return new MoveAction(combatant, bestFuture.pathToAttack());
                }
            }
        }

        return null;
    }

    private List<ActionCandidate> generateCandidates(Combatant combatant, List<Combatant> enemies,
                                                     List<IActionDefinition> definitions, GameState state) {
        List<ActionCandidate> candidates = new ArrayList<>();

        for (IActionDefinition actionDef : definitions) {
            List<Vector2d> potentialTargets = getPotentialTargetTiles(combatant, enemies, actionDef, state);

            for (Vector2d targetTile : potentialTargets) {
                boolean inRangeNow = isTileInRange(combatant.getPosition(), combatant.getSize(), targetTile, actionDef.range());
                List<Vector2d> path = List.of();
                int moveCost = 0;
                Vector2d attackFromPos = combatant.getPosition();

                if (!inRangeNow) {
                    path = calculatePathToTargetTile(combatant, targetTile, actionDef, state);
                    if (!path.isEmpty()) {
                        moveCost = calculatePathCostInSquares(combatant.getPosition(), path);
                        attackFromPos = path.get(path.size() - 1);
                    }
                }

                if (!inRangeNow && path.isEmpty()) {
                    continue;
                }

                double score = evaluateTileScore(combatant, targetTile, actionDef, attackFromPos, state);
                if (score > 0) {
                    candidates.add(new ActionCandidate(actionDef, targetTile, score, inRangeNow, path, moveCost));
                }
            }
        }
        return candidates;
    }


    private List<Vector2d> getPotentialTargetTiles(Combatant attacker, List<Combatant> enemies, IActionDefinition action, GameState state) {
        List<Vector2d> tiles = new ArrayList<>();

        if (action.aoe() == null || action.aoe().length() == 0) {
            for (Combatant e : enemies) {
                for (Vector2d tile : e.getOccupiedTiles()) {
                    if (!tiles.contains(tile)) tiles.add(tile);
                }
            }
        } else {
            int r = action.aoe().length();
            for (Combatant e : enemies) {
                for (Vector2d enemyTile : e.getOccupiedTiles()) {
                    for (int dx = -r; dx <= r; dx++) {
                        for (int dy = -r; dy <= r; dy++) {
                            Vector2d t = new Vector2d(enemyTile.getX() + dx, enemyTile.getY() + dy);
                            if (state.getBattleMap().isWithinBounds(t) && !tiles.contains(t)) {
                                tiles.add(t);
                            }
                        }
                    }
                }
            }
        }
        return tiles;
    }

    private double evaluateTileScore(Combatant attacker, Vector2d targetTile, IActionDefinition action,
                                     Vector2d castFromPos, GameState state) {
        double baseValue = action.calculateBaseExpectedValue();

        // TODO - advantage and disadvantage calculations
        if (action.hasCloseCombatDisadvantage() && isEnemyInMeleeReach(attacker, castFromPos, state)) {
            baseValue *= DISADVANTAGE_PENALTY;
        }

        // Not AoE => expected damage
        if (action.aoe().length() == 0) {
            Combatant target = state.getAliveCombatantAt(targetTile);
            if (target == null || target.getFactionId().equals(attacker.getFactionId())) {
                return -1.0;
            }
            return baseValue;
        }

        // AoE =>
        double totalScore = 0.0;
        int enemiesHit = 0;

        List<Vector2d> affectedTiles = GridGeometry.calculateAoE(
                action.aoe(), castFromPos, targetTile, attacker.getSize()
        );
        List<Combatant> hitCombatants = new ArrayList<>();

        for (Vector2d tile : affectedTiles) {
            if (!state.getBattleMap().isWithinBounds(tile)) continue;
            Combatant c = state.getAliveCombatantAt(tile);

            if (c != null && !hitCombatants.contains(c)) {
                hitCombatants.add(c);
                if (!c.getFactionId().equals(attacker.getFactionId())) {
                    totalScore += baseValue;
                    enemiesHit++;
                } else {
                    // Hitting ally penalty
                    totalScore -= baseValue * 1.5;
                }
            }
        }

        // Hitting yourself penalty
        if (hitCombatants.contains(attacker)) {
            totalScore -= baseValue * 1.5;
        }

        return enemiesHit > 0 ? totalScore : -1.0;
    }

    private boolean isTileInRange(Vector2d attackerPos, int attackerSize, Vector2d targetTile, int range) {
        for (int dx = 0; dx < attackerSize; dx++) {
            for (int dy = 0; dy < attackerSize; dy++) {
                Vector2d attackerTile = new Vector2d(attackerPos.getX() + dx, attackerPos.getY() + dy);
                if (attackerTile.distance(targetTile) <= range) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Vector2d> calculatePathToTargetTile(Combatant attacker, Vector2d targetTile,
                                                     IActionDefinition action, GameState state) {
        List<Vector2d> bestPath = new ArrayList<>();
        int minCost = Integer.MAX_VALUE;
        int range = action.range();

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                Vector2d checkPos = new Vector2d(targetTile.getX() + dx, targetTile.getY() + dy);

                if (!state.getBattleMap().isWithinBounds(checkPos)) continue;
                if (!isTileInRange(checkPos, attacker.getSize(), targetTile, range)) continue;
                if (!pathfinder.isWalkable(attacker, checkPos, state)) continue;

                List<Vector2d> path = pathfinder.findPath(attacker, checkPos, state);
                if (!path.isEmpty()) {
                    int cost = calculatePathCostInSquares(attacker.getPosition(), path);
                    if (action.hasCloseCombatDisadvantage() && isEnemyInMeleeReach(attacker, checkPos, state)) {
                        cost += 100;
                    }
                    if (cost < minCost) {
                        minCost = cost;
                        bestPath = path;
                    }
                }
            }
        }
        return bestPath;
    }

    private List<IActionDefinition> getAvailableActionDefinitions(Combatant combatant, TurnContext turnContext) {
        List<IActionDefinition> result = new ArrayList<>();
        for (IActionDefinition def : combatant.getAvailableActions()) {
            if (def.cost() == ActionCost.ACTION && turnContext.hasAction()) {
                result.add(def);
            } else if (def.cost() == ActionCost.BONUS_ACTION && turnContext.hasBonusAction()) {
                result.add(def);
            }
        }
        return result;
    }

    private boolean isEnemyInMeleeReach(Combatant attacker, Vector2d attackerPos, GameState state) {
        for (Combatant other : state.getAllCombatants()) {
            if (other.getHp() > 0 && !other.getFactionId().equals(attacker.getFactionId())) {
                if (isTileInRange(attacker.getPosition(), other.getSize(), attackerPos, other.getSize())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Combatant> getLivingEnemies(Combatant attacker, GameState state) {
        List<Combatant> enemies = new ArrayList<>();
        for (Combatant c : state.getAllCombatants()) {
            if (c.getHp() > 0 && !c.getFactionId().equals(attacker.getFactionId())) {
                enemies.add(c);
            }
        }
        return enemies;
    }

    private int calculatePathCostInSquares(Vector2d startPos, List<Vector2d> path) {
        int cost = 0;
        int diagonals = 0;
        Vector2d current = startPos;
        for (Vector2d step : path) {
            int dx = Math.abs(step.getX() - current.getX());
            int dy = Math.abs(step.getY() - current.getY());
            if (dx == 1 && dy == 1) {
                diagonals++;
                cost += (diagonals % 2 == 0) ? 2 : 1;
            } else {
                cost += 1;
            }
            current = step;
        }
        return cost;
    }
}