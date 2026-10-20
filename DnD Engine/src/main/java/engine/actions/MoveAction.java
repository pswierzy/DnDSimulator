package engine.actions;

import engine.context.TurnContext;
import engine.events.SimpleEvent;
import engine.state.Combatant;
import engine.state.Vector2d;

import java.util.List;

public class MoveAction implements IAction {
    private final Combatant entity;
    private final List<Vector2d> path;

    public MoveAction(Combatant entity, List<Vector2d> path) {
        this.entity = entity;
        this.path = path;
    }

    @Override
    public boolean canExecute(TurnContext turnContext) {
        return entity.getHp() > 0 && path != null && !path.isEmpty();
    }

    @Override
    public void execute(TurnContext turnContext) {
        int speedInSquares = turnContext.getMovementRemaining();
        int currentCost = 0;
        int diagonalsTraversed = 0;

        Vector2d currentPos = entity.getPosition();
        Vector2d finalDestination = currentPos;

        for (Vector2d step : path) {
            int dx = Math.abs(step.getX() - currentPos.getX());
            int dy = Math.abs(step.getY() - currentPos.getY());

            int stepCost;
            if (dx == 1 && dy == 1) {
                diagonalsTraversed++;
                stepCost = (diagonalsTraversed % 2 == 0) ? 2 : 1;
            } else {
                stepCost = 1;
            }

            if (currentCost + stepCost > speedInSquares) {
                break;
            }

            currentCost += stepCost;
            currentPos = step;
            finalDestination = step;
        }

        if (!finalDestination.equals(entity.getPosition())) {

            for (Vector2d step : path) {
                entity.setPosition(step);
                turnContext.consumeMovement(1);

                // new place checks / opportunity attacks
            }

            int finalCostFt = currentCost * 5;

            turnContext.getCombatLogger().log(new SimpleEvent(
                    entity.getName() + " moves to " + finalDestination +
                            " using " + finalCostFt + "ft of movement. " + path
            ));
        }
    }
}