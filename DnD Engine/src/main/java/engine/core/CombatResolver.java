package engine.core;

import engine.events.ICombatLogger;
import engine.state.Combatant;

public class CombatResolver {
    private final IDiceRoller diceRoller;

    public CombatResolver(IDiceRoller diceRoller) {
        this.diceRoller = diceRoller;
    }

    public void resolveAttack(Combatant attacker, Combatant target, ICombatLogger combatLogger) {
        int attackRoll = diceRoller.parseRoll("d20");
        boolean hits = attackRoll >= target.getArmorClass();

        if(hits) {
            target.setHp(target.getHp() - 1);
            combatLogger.log(() -> attacker.getName() + " hits [" + attackRoll + "] " + target.getName() + " and deals 1 point of damage. HP of the target: " + target.getHp());
        } else {
            combatLogger.log(() -> attacker.getName() + " misses [" + attackRoll + "] " + target.getName());
        }
    }
}
