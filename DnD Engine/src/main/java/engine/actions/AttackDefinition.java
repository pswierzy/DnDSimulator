package engine.actions;

import engine.core.dice.DiceMath;
import engine.state.Combatant;
import engine.state.DamageComponent;
import engine.state.Vector2d;

import java.util.List;

public record AttackDefinition (
        String name,
        ActionCost cost,
        AttackType attackType,
        int range,
        int toHitBonus,
        List<DamageComponent> damageComponents
) implements IActionDefinition{

    @Override
    public boolean hasCloseCombatDisadvantage() {
        return attackType == AttackType.RANGED;
    }

    @Override
    public double calculateBaseExpectedValue() {
        return DiceMath.calculateAverageDamage(damageComponents);
    }

    @Override
    public IAction createAction(Combatant performer, Vector2d targetTile) {
        return new AttackAction(performer, targetTile, this);
    }
}