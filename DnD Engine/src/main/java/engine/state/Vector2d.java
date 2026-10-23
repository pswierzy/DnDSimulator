package engine.state;

public record Vector2d(int x, int y) {
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return x == ((Vector2d) o).x && y == ((Vector2d) o).y;
    }
}
