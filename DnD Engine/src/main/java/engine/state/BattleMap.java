package engine.state;

import java.util.HashSet;
import java.util.Set;

public class BattleMap {
    private final int width;
    private final int height;
    private final Set<Vector2d> blockedTiles = new HashSet<Vector2d>(); // ściany
    private final Set<Vector2d> chasmTiles = new HashSet<Vector2d>(); // przepaście

    public BattleMap(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

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
        return position.getX() >= 0 && position.getX() < width
                && position.getY() >= 0 && position.getY() < height;
    }
}
