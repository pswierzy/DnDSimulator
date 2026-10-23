package engine.state;

import engine.core.metric.GridGeometry;
import engine.core.metric.IGridMetric;

import java.util.HashSet;
import java.util.Set;

public class BattleMap {
    private final int width;
    private final int height;
    private final GridGeometry geometry;

    private final Set<Vector2d> blockedTiles = new HashSet<>(); // ściany
    private final Set<Vector2d> chasmTiles = new HashSet<>(); // przepaście

    public BattleMap(int width, int height) {
        this(width, height, new GridGeometry());
    }
    public BattleMap(int width, int height, IGridMetric gridMetric) {
        this(width, height, new GridGeometry(gridMetric));
    }
    public BattleMap(int width, int height, GridGeometry geometry) {
        this.width = width;
        this.height = height;
        this.geometry = geometry;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public GridGeometry getGeometry() { return geometry; }


    public void addWallTile(Vector2d tile) {
        blockedTiles.add(tile);
    }
    public void addChasmTile(Vector2d tile) {
        chasmTiles.add(tile);
    }

    public boolean isBlocked(Vector2d tile) {
        return blockedTiles.contains(tile);
    }
    public boolean isChasm(Vector2d tile) {
        return chasmTiles.contains(tile);
    }
    public boolean isWithinBounds(Vector2d position) {
        return position.x() >= 0 && position.x() < width
                && position.y() >= 0 && position.y() < height;
    }
}
