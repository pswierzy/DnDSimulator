package ai.decider;

import ai.movement.IPathfinder;
import engine.actions.ActionDefinition;
import engine.actions.AttackAction;
import engine.actions.IAction;
import engine.actions.MoveAction;
import engine.context.TurnContext;
import engine.state.Combatant;
import engine.state.GameState;
import engine.state.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class AggressiveDecider implements IDecider {
    private final IPathfinder pathfinder;

    public AggressiveDecider(IPathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public IAction decideNextAction(Combatant combatant, TurnContext turnContext) {
        GameState state = turnContext.getGameState();
        Combatant target = findNearestEnemy(combatant, state);
        if (target == null) { return null; }

        ActionDefinition primaryAction = combatant.getPrimaryAction();
        int range = primaryAction.range();

        if (!isInRange(combatant, target, range)) {
            List<Vector2d> path = calculatePathToAttackPosition(combatant, target, range, state);
            if (!path.isEmpty()) {
                return new MoveAction(combatant, path);
            }
        }

        if (isInRange(combatant, target, range)) {
            return new AttackAction(combatant, target, primaryAction.name(), primaryAction.range(), primaryAction.toHitBonus(), primaryAction.damageComponents());
        }

        return null;
    }

    private Combatant findNearestEnemy(Combatant attacker, GameState state) {
        Combatant target = null;
        int minDistance = Integer.MAX_VALUE;
        for (Combatant c : state.getAllCombatants()) {
            if (c.getHp() > 0 && !c.getFactionId().equals(attacker.getFactionId())) {
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
        return 1; // TODO: w przyszłości pobierane z wyposażonej broni
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

    private List<Vector2d> calculatePathToAttackPosition(Combatant attacker, Combatant target, int range, GameState state) {
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
