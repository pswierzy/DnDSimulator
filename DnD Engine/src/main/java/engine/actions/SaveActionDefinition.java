package engine.actions;

import engine.core.DiceMath;
import engine.state.Ability;
import engine.state.Combatant;
import engine.state.DamageComponent;
import engine.state.Vector2d;

import java.util.List;

public record SaveActionDefinition(
        String name,
        ActionCost cost,
        int range,
        AreaOfEffect aoe,
        Ability saveAbility,
        int saveDc,
        boolean halfDamageOnSuccess,
        List<DamageComponent> damageComponents
) implements IActionDefinition {

    @Override
    public double calculateBaseExpectedValue() {
        double avg = DiceMath.calculateAverageDamage(damageComponents);
        return halfDamageOnSuccess ? avg : avg * 0.75;
    }

    @Override
    public IAction createAction(Combatant performer, Vector2d targetTile) {
        return new SaveAction(performer, targetTile, this);
    }
}
