package engine.state;

public record Vector2d(int x, int y) {
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
