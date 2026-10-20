package engine.actions;

import engine.context.TurnContext;
import engine.events.SimpleEvent;
import engine.state.Combatant;
import engine.state.Vector2d;

import java.util.ArrayList;
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
        int currentCost = 0;
        int diagonalsTraversed = 0;

        Vector2d startPos = entity.getPosition();
        Vector2d currentPos = startPos;

        List<Vector2d> stepsTaken = new ArrayList<>();

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

            if (stepCost > turnContext.getMovementRemaining()) {
                break;
            }

            entity.setPosition(step);
            turnContext.consumeMovement(stepCost);
            currentCost += stepCost;
            currentPos = step;
            stepsTaken.add(step);

            // TODO - opportunity attacks
        }

        if (!currentPos.equals(startPos)) {
            int finalCostFt = currentCost * 5;

            turnContext.getCombatLogger().log(new SimpleEvent(
                    entity.getName() + " moves to " + currentPos +
                            " using " + finalCostFt + "ft of movement. " + stepsTaken
            ));
        }
    }
}