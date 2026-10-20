package ai.decider;

import ai.movement.IPathfinder;
import engine.actions.*;
import engine.context.TurnContext;
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
            Combatant target,
            double expectedScore,
            boolean alreadyInRange,
            List<Vector2d> pathToAttack,
            int movementCostInSquares
    ) {}

    @Override
    public IAction decideNextAction(Combatant combatant, TurnContext turnContext) {
        GameState state = turnContext.getGameState();
        List<Combatant> enemies = getLivingEnemies(combatant, state);
        if (enemies.isEmpty()) {
            return null;
        }

        List<IActionDefinition> usableDefinitions = getAvailableActionDefinitions(combatant, turnContext);
        if (usableDefinitions.isEmpty()) {
            return null;
        }

        List<ActionCandidate> candidates = new ArrayList<>();

        for (IActionDefinition actionDef : usableDefinitions) {
            for (Combatant enemy : enemies) {
                boolean inRangeNow = isInRange(combatant.getPosition(), combatant.getSize(), enemy, actionDef.range());
                List<Vector2d> path = List.of();
                int moveCost = 0;
                Vector2d attackFromPos = combatant.getPosition();

                if (!inRangeNow) {
                    path = calculatePathToAttackPosition(combatant, enemy, actionDef, state);
                    if (!path.isEmpty()) {
                        moveCost = calculatePathCostInSquares(combatant.getPosition(), path);
                        attackFromPos = path.get(path.size() - 1);
                    }
                }

                if (!inRangeNow && path.isEmpty()) {
                    continue;
                }

                double score = evaluateActionScore(combatant, enemy, actionDef, attackFromPos, state);
                candidates.add(new ActionCandidate(actionDef, enemy, score, inRangeNow, path, moveCost));
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort(
                Comparator.comparingDouble(ActionCandidate::expectedScore).reversed()
                        .thenComparingInt(ActionCandidate::movementCostInSquares)
        );

        int remainingMove = turnContext.getMovementRemaining();
        for (ActionCandidate candidate : candidates) {
            if (candidate.alreadyInRange()) {
                return candidate.action().createAction(combatant, candidate.target().getPosition());
            }

            if (remainingMove > 0 && candidate.movementCostInSquares() <= remainingMove) {
                return new MoveAction(combatant, candidate.pathToAttack());
            }
        }

        if (remainingMove > 0) {
            ActionCandidate bestUnreachable = candidates.get(0);
            if (!bestUnreachable.pathToAttack().isEmpty()) {
                return new MoveAction(combatant, bestUnreachable.pathToAttack());
            }
        }

        return null;
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

    private double evaluateActionScore(Combatant attacker, Combatant target, IActionDefinition action,
                                       Vector2d attackPosition, GameState state) {
        double expectedDamage = action.calculateBaseExpectedValue();

        if (action.hasCloseCombatDisadvantage() && isEnemyInMeleeReach(attacker, attackPosition, state)) {
            expectedDamage *= DISADVANTAGE_PENALTY;
        }

        return expectedDamage;
    }

    private boolean isEnemyInMeleeReach(Combatant attacker, Vector2d attackerPos, GameState state) {
        for (Combatant other : state.getAllCombatants()) {
            if (other.getHp() > 0 && !other.getFactionId().equals(attacker.getFactionId())) {
                if (isInRange(attackerPos, attacker.getSize(), other, 1)) {
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

    private boolean isInRange(Vector2d attackerPos, int attackerSize, Combatant target, int range) {
        for (Vector2d targetTile : target.getOccupiedTiles()) {
            for (int dx = 0; dx < attackerSize; dx++) {
                for (int dy = 0; dy < attackerSize; dy++) {
                    Vector2d attackerTile = new Vector2d(attackerPos.getX() + dx, attackerPos.getY() + dy);
                    if (attackerTile.distance(targetTile) <= range) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<Vector2d> calculatePathToAttackPosition(Combatant attacker, Combatant target,
                                                         IActionDefinition action, GameState state) {
        List<Vector2d> bestPath = new ArrayList<>();
        int minCost = Integer.MAX_VALUE;
        int range = action.range();

        for (Vector2d targetTile : target.getOccupiedTiles()) {
            for (int dx = -range; dx <= range; dx++) {
                for (int dy = -range; dy <= range; dy++) {
                    Vector2d checkPos = new Vector2d(targetTile.getX() + dx, targetTile.getY() + dy);

                    if (!state.getBattleMap().isWithinBounds(checkPos)) continue;
                    if (!isInRange(checkPos, attacker.getSize(), target, range)) continue;
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
        }
        return bestPath;
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