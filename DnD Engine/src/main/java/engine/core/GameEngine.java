package engine.core;

import engine.actions.AttackAction;
import engine.actions.IAction;
import engine.actions.MoveAction;
import engine.context.TurnContext;
import engine.events.ICombatLogger;
import engine.state.Combatant;
import engine.state.GameState;
import engine.state.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private final GameState state;
    private final ICombatLogger logger;
    private final CombatResolver resolver;
    private final IPathfinder pathfinder;

    public GameEngine(GameState state, ICombatLogger logger, CombatResolver resolver, IPathfinder pathfinder) {
        this.state = state;
        this.logger = logger;
        this.resolver = resolver;
        this.pathfinder = pathfinder;
    }

    public void run() {
        logger.log(() -> "=== START SYMULACJI ===");
        while(!isCombatOver()) {
            Combatant curr = state.getNextCombatant();
            if (curr.getHp() <= 0) continue;

            TurnContext context = new TurnContext(state, logger, resolver);
            Combatant target = findNearestEnemy(curr);

            if (target != null) {
                int range = getWeaponRange(curr);

                if (!isInRange(curr, target, range)) {
                    List<Vector2d> path = calculatePathToAttackPosition(curr, target, range);
                    if (!path.isEmpty()) {
                        IAction move = new MoveAction(curr, path);
                        if(move.canExecute(context)) {
                            move.execute(context);
                        }
                    }
                }

                if (isInRange(curr, target, range)) {
                    IAction attack = new AttackAction(curr, target);
                    if(attack.canExecute(context)) {
                        attack.execute(context);
                    }
                }
            }
        }

        logger.log(() -> "=== END SYMULACJI ===");
    }

    private boolean isCombatOver() {
        String firstAliveFaction = null;
        for (Combatant c: state.getAllCombatants()) {
            if (c.getHp() > 0) {
                if (firstAliveFaction == null) {
                    firstAliveFaction = c.getFactionId();
                } else if (!firstAliveFaction.equals(c.getFactionId())) {
                    return false;
                }
            }
        }
        return true;
    }

    private Combatant findNearestEnemy(Combatant attacker) {
        Combatant target = null;
        int minDistance = Integer.MAX_VALUE;
        for (Combatant c: state.getAllCombatants()) {
            if (c.getHp()>0 && !c.getFactionId().equals(attacker.getFactionId())) {
                int dist = attacker.getPosition().distance(c.getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    target = c;
                }
            }
        }
        return target;
    }

    private int getWeaponRange(Combatant combatant) {
        return 1; // TODO
    }

    private boolean isInRange(Combatant attacker, Combatant target, int range) {
        for (Vector2d targetTile : target.getOccupiedTiles()) {
            for (Vector2d attackerTile : attacker.getOccupiedTiles()) {
                if (attackerTile.distance(targetTile) <= range) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Vector2d> calculatePathToAttackPosition(Combatant attacker, Combatant target, int range) {
        List<Vector2d> bestPath = new ArrayList<>();
        int minPathLength = Integer.MAX_VALUE;

        for (Vector2d targetTile : target.getOccupiedTiles()) {
            for (int dx = -range; dx <= range; dx++) {
                for (int dy = -range; dy <= range; dy++) {
                    Vector2d checkPos = new Vector2d(targetTile.getX() + dx, targetTile.getY() + dy);

                    if (target.getOccupiedTiles().contains(checkPos)) continue;
                    if (!state.getBattleMap().isWithinBounds(checkPos)) continue;

                    if (pathfinder.isWalkable(attacker, checkPos, state)) {
                        List<Vector2d> path = pathfinder.findPath(attacker, checkPos, state);
                        if (!path.isEmpty() && path.size() < minPathLength) {
                            bestPath = path;
                            minPathLength = path.size();
                        }
                    }
                }
            }
        }
        return bestPath;
    }
}
