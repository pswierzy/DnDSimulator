package engine.core;

import engine.actions.AttackDefinition;
import engine.actions.SaveActionDefinition;
import engine.events.ICombatLogger;
import engine.state.Combatant;
import engine.state.DamageComponent;
import engine.state.RolledDamage;

import java.util.ArrayList;
import java.util.List;

public class AttackResolver {
    private final IDiceRoller diceRoller;

    public AttackResolver(IDiceRoller diceRoller) {
        this.diceRoller = diceRoller;
    }

    public List<RolledDamage> rollDamage(List<DamageComponent> components) {
        List<RolledDamage> results = new ArrayList<>();
        if (components != null) {
            for (DamageComponent comp : components) {
                int amount = diceRoller.parseRoll(comp.dice());
                results.add(new RolledDamage(amount, comp.damageType()));
            }
        }
        return results;
    }

    public void resolveAttack(Combatant attacker, Combatant target, AttackDefinition definition, ICombatLogger combatLogger) {
        int attackRoll = diceRoller.roll(20) + definition.toHitBonus();
        boolean hits = attackRoll >= target.getArmorClass();

        if(hits) {
            List<RolledDamage> rolledDamage = rollDamage(definition.damageComponents());
            for (RolledDamage damage : rolledDamage) {
                target.takeDamage(damage.amount(), damage.damageType());
            }

            String damageString = rolledDamage.stream()
                    .map(rd -> rd.amount() + " " + rd.damageType())
                    .reduce((a, b) -> a + " + " + b).orElse("0");

            combatLogger.log(() -> attacker.getName() + " hits " + definition.name() + " [" + attackRoll + "] " + target.getName() + " and deals " + damageString + " point of damage. HP of the target: " + target.getHp());
        } else {
            combatLogger.log(() -> attacker.getName() + " misses " + definition.name() + " [" + attackRoll + "] " + target.getName());
        }
    }

    public void resolveSavingThrow(Combatant performer, Combatant target, SaveActionDefinition definition,
                                   List<RolledDamage> baseDamage, ICombatLogger logger) {

        int naturalRoll = diceRoller.roll(20);
        int saveBonus = target.getSaveBonus(definition.saveAbility());
        int totalSave = naturalRoll + saveBonus;
        boolean success = (totalSave >= definition.saveDc());

        int totalDamageDealt = 0;

        for (RolledDamage rd : baseDamage) {
            int finalDamage = rd.amount();

            if (success) {
                if (definition.halfDamageOnSuccess()) {
                    finalDamage = Math.floorDiv(finalDamage, 2);
                } else {
                    finalDamage = 0;
                }
            }

            if (finalDamage > 0) {
                target.takeDamage(finalDamage, rd.damageType());
                totalDamageDealt += finalDamage;
            }
        }

        int dmgForLog = totalDamageDealt;
        if (success) {
            if (definition.halfDamageOnSuccess()) {
                logger.log(() -> target.getName() + " SUCCEEDS on " + definition.saveAbility() +
                        " save [" + totalSave + " vs DC " + definition.saveDc() +
                        "] and takes half damage (" + dmgForLog + "). HP: " + target.getHp());
            } else {
                logger.log(() -> target.getName() + " SUCCEEDS on " + definition.saveAbility() +
                        " save [" + totalSave + " vs DC " + definition.saveDc() + "] and takes NO damage.");
            }
        } else {
            logger.log(() -> target.getName() + " FAILS " + definition.saveAbility() +
                    " save [" + totalSave + " vs DC " + definition.saveDc() +
                    "] and takes full damage (" + dmgForLog + "). HP: " + target.getHp());
        }
    }
}
