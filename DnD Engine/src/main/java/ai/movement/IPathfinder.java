package ai.movement;

import engine.state.Combatant;
import engine.state.GameState;
import engine.state.Vector2d;

import java.util.List;

public interface IPathfinder {
    List<Vector2d> findPath(Combatant entity, Vector2d destination, GameState state);
    boolean isWalkable(Combatant entity, Vector2d place, GameState state);
}
