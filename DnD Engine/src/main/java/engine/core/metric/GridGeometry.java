package engine.core.metric;

import engine.actions.AreaOfEffect;
import engine.actions.AreaShape;
import engine.state.Vector2d;

import java.util.ArrayList;
import java.util.List;

public final class GridGeometry {
    private final IGridMetric metric;

    public GridGeometry() {
        this(GridMetricRule.VARIANT_5_10_5);
    }
    public GridGeometry(IGridMetric metric) {
        this.metric = metric;
    }

    public IGridMetric getMetric() {
        return metric;
    }

    public boolean isInRange(Vector2d originPos, int originSize, Vector2d targetTile, int range) {
        for (int dx = 0; dx < originSize; dx++) {
            for (int dy = 0; dy < originSize; dy++) {
                Vector2d occupiedTile = new Vector2d(originPos.x() + dx, originPos.y() + dy);
                if (metric.measureDistance(occupiedTile, targetTile) <= range) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Vector2d> calculateAoE(AreaOfEffect aoe, Vector2d origin, Vector2d target, int originSize) {
        if (aoe == null || aoe.shape() == AreaShape.SINGLE_TARGET) return List.of(target);

        return switch (aoe.shape()) {
            case SPHERE -> getSphere(target, aoe.length());
            case EMANATION -> getEmanation(origin, originSize, aoe.length());
            case CONE -> getCone(origin, target, aoe.length());
            case LINE -> getLine(origin, target, aoe.length(), aoe.width());
            case CUBE_SELF -> getCubeSelf(origin, originSize, target, aoe.length());
            case CUBE_RANGED -> getCubeRange(target, aoe.length());
            default -> List.of(target);
        };
    }

    private List<Vector2d> getSphere(Vector2d center, int radius) {
        List<Vector2d> tiles = new ArrayList<>();
        for (int x = center.x() - radius; x <= center.x() + radius; x++) {
            for (int y = center.y() - radius; y <= center.y() + radius; y++) {
                Vector2d checkPos = new Vector2d(x, y);
                if (metric.measureDistance(center, checkPos) <= radius) {
                    tiles.add(checkPos);
                }
            }
        }
        return tiles;
    }

    private List<Vector2d> getEmanation(Vector2d origin, int originSize, int radius) {
        List<Vector2d> tiles = new ArrayList<>();
        int searchBound = originSize + radius;
        for (int x = origin.x() - searchBound; x <= origin.x() + searchBound; x++) {
            for (int y = origin.y() - searchBound; y <= origin.y() + searchBound; y++) {
                Vector2d checkPos = new Vector2d(x, y);

                int minDistance = Integer.MAX_VALUE;
                for (int dx = 0; dx < originSize; dx++) {
                    for (int dy = 0; dy < originSize; dy++) {
                        int dist = metric.measureDistance(checkPos, new Vector2d(origin.x() + dx, origin.y() + dy));
                        minDistance = Math.min(minDistance, dist);
                    }
                }

                if (minDistance > 0 && minDistance <= radius) {
                    tiles.add(checkPos);
                }
            }
        }
        return tiles;
    }

    private List<Vector2d> getCone(Vector2d origin, Vector2d target, int length) {
        List<Vector2d> tiles = new ArrayList<>();
        if (origin.equals(target)) return tiles;

        double targetAngle = Math.atan2(target.y() - origin.y(), target.x() - origin.x());
        double halfAngle = Math.toRadians(26.565);

        for (int x = origin.x() - length; x <= origin.x() + length; x++) {
            for (int y = origin.y() - length; y <= origin.y() + length; y++) {
                Vector2d checkPos = new Vector2d(x, y);
                if (checkPos.equals(origin)) continue;

                if (metric.measureDistance(origin, checkPos) <= length) {
                    double tileAngle = Math.atan2(checkPos.y() - origin.y(), checkPos.x() - origin.x());
                    double angularDiff = Math.abs(tileAngle - targetAngle);

                    if (angularDiff > Math.PI) {
                        angularDiff = 2 * Math.PI - angularDiff;
                    }

                    if (angularDiff <= halfAngle) {
                        tiles.add(checkPos);
                    }
                }
            }
        }
        return tiles;
    }

    private List<Vector2d> getLine(Vector2d origin, Vector2d target, int length, int width) {
        List<Vector2d> tiles = new ArrayList<>();
        if (origin.equals(target)) return tiles;

        double dx = target.x() - origin.x();
        double dy = target.y() - origin.y();
        double magnitude = Math.sqrt(dx * dx + dy * dy);

        double vx = dx / magnitude;
        double vy = dy / magnitude;

        double currentDist = 0;
        double step = 0.5;

        while (true) {
            currentDist += step;
            int cx = (int) Math.round(origin.x() + vx * currentDist);
            int cy = (int) Math.round(origin.y() + vy * currentDist);
            Vector2d checkPos = new Vector2d(cx, cy);

            if (metric.measureDistance(origin, checkPos) > length) {
                break;
            }

            if (!checkPos.equals(origin) && !tiles.contains(checkPos)) {
                tiles.add(checkPos);
                // TODO: width > 1
            }
        }
        return tiles;
    }

    private List<Vector2d> getCubeSelf(Vector2d origin, int originSize, Vector2d target, int size) {
        List<Vector2d> tiles = new ArrayList<>();
        if (size <= 0 || (target.x() >= origin.x() && target.x() < origin.x() + originSize
                && target.y() >= origin.y() && target.y() < origin.y() + originSize)) {
            return tiles;
        }

        double centerX = origin.x() + (originSize - 1) / 2.0;
        double centerY = origin.y() + (originSize - 1) / 2.0;
        double dx = target.x() - centerX;
        double dy = target.y() - centerY;

        int startX;
        int startY;

        if (Math.abs(dx) >= Math.abs(dy)) {
            startX = (dx > 0) ? (origin.x() + originSize) : (origin.x() - size);
            int minStartY = origin.y() - size + 1;
            int maxStartY = origin.y() + originSize - 1;
            startY = Math.clamp(target.y() - size / 2, minStartY, maxStartY);
        } else {
            startY = (dy > 0) ? (origin.y() + originSize) : (origin.y() - size);

            int minStartX = origin.x() - size + 1;
            int maxStartX = origin.x() + originSize - 1;
            startX = Math.clamp(target.x() - size / 2, minStartX, maxStartX);
        }

        for (int x = startX; x < startX + size; x++) {
            for (int y = startY; y < startY + size; y++) {
                tiles.add(new Vector2d(x, y));
            }
        }
        return tiles;
    }

    private List<Vector2d> getCubeRange(Vector2d target, int size) {
        List<Vector2d> tiles = new ArrayList<>();
        int startX = target.x() - (size - 1) / 2;
        int startY = target.y() - (size - 1) / 2;

        for (int x = startX; x < startX + size; x++) {
            for (int y = startY; y < startY + size; y++) {
                tiles.add(new Vector2d(x, y));
            }
        }
        return tiles;
    }
}
