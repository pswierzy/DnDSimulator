package engine.core;

import engine.actions.AttackDefinition;
import engine.events.ICombatLogger;
import engine.state.Combatant;
import engine.state.DamageComponent;

import java.util.ArrayList;
import java.util.List;

public class AttackResolver {
    private final IDiceRoller diceRoller;

    public AttackResolver(IDiceRoller diceRoller) {
        this.diceRoller = diceRoller;
    }

    public void resolveAttack(Combatant attacker, Combatant target, AttackDefinition definition, ICombatLogger combatLogger) {
        int attackRoll = diceRoller.roll(20) + definition.toHitBonus();
        boolean hits = attackRoll >= target.getArmorClass();

        if(hits) {
            List<Integer> damageDealtLogs = new ArrayList<>();
            for (DamageComponent damageComponent : definition.damageComponents()) {
                int damageRoll = diceRoller.parseRoll(damageComponent.dice());
                int damageDealt = target.takeDamage(damageRoll, damageComponent.damageType());
                damageDealtLogs.add(damageDealt);
            }
            combatLogger.log(() -> attacker.getName() + " hits " + definition.name() + " [" + attackRoll + "] " + target.getName() + " and deals " + damageDealtLogs + " point of damage. HP of the target: " + target.getHp());
        } else {
            combatLogger.log(() -> attacker.getName() + " misses " + definition.name() + " [" + attackRoll + "] " + target.getName());
        }
    }
}
