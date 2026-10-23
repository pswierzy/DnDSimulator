package engine.actions;

import engine.context.TurnContext;
import engine.core.metric.IGridMetric;
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

        Vector2d startPos = entity.getPosition();
        Vector2d currentPos = startPos;

        List<Vector2d> stepsTaken = new ArrayList<>();

        IGridMetric metric = turnContext.getGameState().getGeometry().getMetric();
        for (Vector2d step : path) {
            int stepCost = metric.calculateStepCost(currentPos, step, turnContext.getDiagonalsTraversed());

            if (stepCost > turnContext.getMovementRemaining()) {
                break;
            }

            if (metric.isDiagonalStep(currentPos, step)) {
                turnContext.incrementDiagonalsTraversed();
            }
            turnContext.consumeMovement(stepCost);
            entity.setPosition(step);

            currentCost += stepCost;
            currentPos = step;
            stepsTaken.add(step);
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