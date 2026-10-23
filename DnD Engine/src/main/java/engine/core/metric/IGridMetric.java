package engine.core.metric;

import engine.state.Vector2d;

import java.util.List;

public interface IGridMetric {
    int measureDistance(Vector2d from, Vector2d to);

    int calculateStepCost(Vector2d from, Vector2d to, int diagonalsAlreadyMoved);

    default boolean isDiagonalStep(Vector2d from, Vector2d to) {
        return Math.abs(to.x() - from.x()) == 1
                && Math.abs(to.y() - from.y()) == 1;
    }

    default int calculatePathCost(Vector2d from, List<Vector2d> path,int diagonalsAlreadyMoved) {
        int cost = 0;
        int simulatedDiagonals = diagonalsAlreadyMoved;
        Vector2d curr = from;

        for (Vector2d v : path) {
            cost += calculateStepCost(curr, v, simulatedDiagonals);
            if (isDiagonalStep(curr, v)) {
                simulatedDiagonals++;
            }
            curr = v;
        }

        return cost;
    }
}
