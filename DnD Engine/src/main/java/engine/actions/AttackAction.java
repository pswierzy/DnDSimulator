package engine.actions;

import engine.context.TurnContext;
import engine.state.Combatant;

public class AttackAction implements IAction {
    private final Combatant attacker;
    private final Combatant target;

    public AttackAction(Combatant attacker, Combatant target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public boolean canExecute(TurnContext turnContext) {
        return attacker.getHp() > 0 && target.getHp() > 0;
    }

    @Override
    public void execute(TurnContext turnContext) {
        turnContext.getCombatResolver().resolveAttack(attacker, target, turnContext.getCombatLogger());
    }
}
