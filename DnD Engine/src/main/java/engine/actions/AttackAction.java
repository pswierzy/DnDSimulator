package engine.actions;

import engine.context.TurnContext;
import engine.state.Combatant;
import engine.state.DamageComponent;
import engine.state.Vector2d;

import java.util.List;

public class AttackAction implements IAction {
    private final Combatant attacker;
    private final Combatant target;
    private final String actionName;
    private final int range;
    private final int toHitBonus;
    private final List<DamageComponent> damageComponents;

    public AttackAction(Combatant attacker, Combatant target, String actionName, int range, int toHitBonus, List<DamageComponent> damageComponents) {
        this.attacker = attacker;
        this.target = target;
        this.actionName = actionName;
        this.range = range;
        this.toHitBonus = toHitBonus;
        this.damageComponents = damageComponents;
    }

    public int getRange() {
        return range;
    }

    @Override
    public boolean canExecute(TurnContext turnContext) {
        return attacker.getHp() > 0
                && target.getHp() > 0
                && turnContext.hasAction()
                && isInRange();
    }

    @Override
    public void execute(TurnContext turnContext) {
        turnContext.consumeAction();

        turnContext.getCombatResolver().resolveAttack(attacker, target, actionName, toHitBonus, damageComponents, turnContext.getCombatLogger());
    }

    private boolean isInRange() {
        for (Vector2d targetTile : target.getOccupiedTiles()) {
            for (Vector2d attackerTile : attacker.getOccupiedTiles()) {
                if (attackerTile.distance(targetTile) <= range) {
                    return true;
                }
            }
        }
        return false;
    }
}
