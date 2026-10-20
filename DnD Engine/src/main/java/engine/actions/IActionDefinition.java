package engine.actions;

import engine.state.Combatant;
import engine.state.DamageType;
import engine.state.Vector2d;

public interface IActionDefinition {
    String name();
    ActionCost cost();
    int range();

    default int aoeRadius() {
        return 0;
    }

    default boolean hasCloseCombatDisadvantage() {
        return false;
    }

    double calculateBaseExpectedValue();
    IAction createAction(Combatant performer, Vector2d targetTile);
}
