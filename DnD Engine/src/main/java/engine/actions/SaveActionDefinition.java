package engine.actions;

import engine.core.DiceMath;
import engine.state.Combatant;
import engine.state.DamageComponent;
import engine.state.Vector2d;

import java.util.List;

public record SaveActionDefinition(
        String name,
        ActionCost cost,
        int range,
        int radius,
        //Ability saveAbility,
        int saveDc,
        boolean halfDamageOnSuccess,
        List<DamageComponent> damageComponents
) implements IActionDefinition {
    @Override
    public double calculateBaseExpectedValue() {
        double avg = DiceMath.calculateAverageDamage(damageComponents);
        // Jeśli nawet przy udanym rzucie obronnym cel dostaje połowę obrażeń, akcja jest pewniejsza:
        return halfDamageOnSuccess ? avg * 0.75 : avg * 0.6;
    }

    @Override
    public IAction createAction(Combatant performer, Vector2d targetTile) {
        return null; //new SaveAction(performer, target, this);
    }
}
