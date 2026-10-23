package engine.core.metric;

import engine.state.Vector2d;

public enum GridMetricRule implements IGridMetric {
    RAW_CHEBYSHEV {         // RAW - diagonal movement is the same as straight
        @Override
        public int measureDistance(Vector2d from, Vector2d to) {
            int dx = Math.abs(from.x() - to.x());
            int dy = Math.abs(from.y() - to.y());
            return Math.max(dx, dy);
        }

        @Override
        public int calculateStepCost(Vector2d from, Vector2d to, int diagonalsAlreadyMoved) {
            return 1;
        }
    },

    VARIANT_5_10_5 {        // Variant rule - every second diagonal movement is 10ft
        @Override
        public int measureDistance(Vector2d from, Vector2d to) {
            int dx = Math.abs(from.x() - to.x());
            int dy = Math.abs(from.y() - to.y());
            int diagonals = Math.min(dx, dy);
            int straights = Math.abs(dx - dy);
            return (diagonals / 2) * 3 + (diagonals % 2) + straights;
        }

        @Override
        public int calculateStepCost(Vector2d from, Vector2d to, int diagonalsAlreadyMoved) {
            if (isDiagonalStep(from, to)) {
                return ((diagonalsAlreadyMoved + 1) % 2 == 0) ? 2 : 1;
            }
            return 1;
        }
    },

    MANHATTAN {         // Every diagonal is 10ft
        @Override
        public int measureDistance(Vector2d from, Vector2d to) {
            int dx = Math.abs(from.x() - to.x());
            int dy = Math.abs(from.y() - to.y());
            return dx + dy;
        }

        @Override
        public int calculateStepCost(Vector2d from, Vector2d to, int diagonalsAlreadyMoved) {
            return isDiagonalStep(from, to) ? 2 : 1;
        }
    }
}
