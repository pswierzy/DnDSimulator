package engine.core;

import engine.actions.AreaOfEffect;
import engine.actions.AreaShape;
import engine.state.Vector2d;

import java.util.ArrayList;
import java.util.List;

public final class GridGeometry {
    private GridGeometry() {}

    public static List<Vector2d> calculateAoE(AreaOfEffect aoe, Vector2d origin, Vector2d target, int originSize) {
        if (aoe == null || aoe.shape() == AreaShape.SINGLE_TARGET) return List.of(target);

        return switch (aoe.shape()) {
            case SPHERE -> getSphere(target, aoe.length());
            case EMANATION -> getEmanation(origin, originSize, aoe.length());
            case CONE -> getCone(origin, target, aoe.length());
            case LINE -> getLine(origin, target, aoe.length(), aoe.width());
            case CUBE -> getCube(target, aoe.length());
            default -> List.of(target);
        };
    }

    private static List<Vector2d> getSphere(Vector2d center, int radius) {
        List<Vector2d> tiles = new ArrayList<>();
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = center.getY() - radius; y <= center.getY() + radius; y++) {
                Vector2d checkPos = new Vector2d(x, y);
                if (center.distance(checkPos) <= radius) {
                    tiles.add(checkPos);
                }
            }
        }
        return tiles;
    }

    private static List<Vector2d> getEmanation(Vector2d origin, int originSize, int radius) {
        List<Vector2d> tiles = new ArrayList<>();
        int searchBound = originSize + radius;
        for (int x = origin.getX() - searchBound; x <= origin.getX() + searchBound; x++) {
            for (int y = origin.getY() - searchBound; y <= origin.getY() + searchBound; y++) {
                Vector2d checkPos = new Vector2d(x, y);

                int minDistance = Integer.MAX_VALUE;
                for (int dx = 0; dx < originSize; dx++) {
                    for (int dy = 0; dy < originSize; dy++) {
                        int dist = checkPos.distance(new Vector2d(origin.getX() + dx, origin.getY() + dy));
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

    private static List<Vector2d> getCone(Vector2d origin, Vector2d target, int length) {
        List<Vector2d> tiles = new ArrayList<>();
        if (origin.equals(target)) return tiles;

        double targetAngle = Math.atan2(target.getY() - origin.getY(), target.getX() - origin.getX());
        double halfAngle = Math.toRadians(26.565);

        for (int x = origin.getX() - length; x <= origin.getX() + length; x++) {
            for (int y = origin.getY() - length; y <= origin.getY() + length; y++) {
                Vector2d checkPos = new Vector2d(x, y);
                if (checkPos.equals(origin)) continue;

                if (origin.distance(checkPos) <= length) {
                    double tileAngle = Math.atan2(checkPos.getY() - origin.getY(), checkPos.getX() - origin.getX());
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

    private static List<Vector2d> getLine(Vector2d origin, Vector2d target, int length, int width) {
        List<Vector2d> tiles = new ArrayList<>();
        if (origin.equals(target)) return tiles;

        double dx = target.getX() - origin.getX();
        double dy = target.getY() - origin.getY();
        double magnitude = Math.sqrt(dx * dx + dy * dy);

        double vx = dx / magnitude;
        double vy = dy / magnitude;

        double currentDist = 0;
        double step = 0.5;

        while (true) {
            currentDist += step;
            int cx = (int) Math.round(origin.getX() + vx * currentDist);
            int cy = (int) Math.round(origin.getY() + vy * currentDist);
            Vector2d checkPos = new Vector2d(cx, cy);

            if (origin.distance(checkPos) > length) {
                break;
            }

            if (!checkPos.equals(origin) && !tiles.contains(checkPos)) {
                tiles.add(checkPos);
                // TODO: width > 1
            }
        }
        return tiles;
    }

    private static List<Vector2d> getCube(Vector2d center, int size) {
        List<Vector2d> tiles = new ArrayList<>();
        int halfSize = size / 2;
        for (int x = center.getX() - halfSize; x <= center.getX() + halfSize; x++) {
            for (int y = center.getY() - halfSize; y <= center.getY() + halfSize; y++) {
                tiles.add(new Vector2d(x, y));
            }
        }
        return tiles;
    }
}
