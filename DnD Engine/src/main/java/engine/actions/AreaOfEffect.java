package engine.actions;

public record AreaOfEffect(
        AreaShape shape,
        int length,
        int width
) {}
