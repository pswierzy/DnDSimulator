package engine.state;

public class Vector2d {
    private int x;
    private int y;

    public Vector2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return x == ((Vector2d) o).x && y == ((Vector2d) o).y;
    }
    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }

    public int distance(Vector2d vector) {
        // distance 5_10_5
        int dx = Math.abs(this.x - vector.x);
        int dy = Math.abs(this.y - vector.y);

        int diagonals = Math.min(dx, dy);
        int straights = Math.abs(dx - dy);

        return (diagonals / 2) * 3 + (diagonals % 2) + straights;
    }
}
