package engine.core;

import engine.actions.SaveActionDefinition;
import engine.context.AttackContext;
import engine.context.SaveContext;
import engine.core.dice.advantage.AdvantageState;
import engine.core.dice.IDiceRoller;
import engine.core.dice.StandardDiceRoller;
import engine.events.ICombatLogger;
import engine.state.*;

import java.util.ArrayList;
import java.util.List;

public class AttackResolver {
    private final IDiceRoller diceRoller;

    public AttackResolver() {
        this(new StandardDiceRoller());
    }
    public AttackResolver(IDiceRoller diceRoller) {
        this.diceRoller = diceRoller;
    }

    public List<RolledDamage> rollDamage(List<DamageComponent> components) { return rollDamage(components, false); }
    public List<RolledDamage> rollDamage(List<DamageComponent> components, boolean isCrit) {
        List<RolledDamage> results = new ArrayList<>();
        if (components != null) {
            for (DamageComponent comp : components) {
                int amount = diceRoller.parseRoll(comp.dice(), isCrit);
                results.add(new RolledDamage(amount, comp.damageType()));
            }
        }
        return results;
    }

    public void resolveAttack(AttackContext context, ICombatLogger combatLogger) {
        Combatant attacker = context.getAttacker();
        Combatant target = context.getTarget();

        // 1. Ranged attacks have disadvantage if there is an enemy within 5ft
        if (context.getDefinition().hasCloseCombatDisadvantage()
                && context.getGameState().hasEnemyInMeleeReach(attacker)) {
            context.addDisadvantage();
        }

        // TODO - onBeforeAttackAsAttacker / onBeforeAttackAsTarget Hooks

        // 2. Advantage / Disadvantage / Straight d20 - hit checking
        AdvantageState advState = diceRoller.getAdvantageState(
                context.getAdvantageCount(),
                context.getDisadvantageCount()
        );
        context.setAdvantageState(advState);

        int natRoll = diceRoller.roll(20, advState);
        int totalRoll = natRoll + context.getBonusToHit();

        context.setNaturalRoll(natRoll);
        context.setTotalRoll(totalRoll);

        if (natRoll == 20) {
            context.setHit(true);
            context.setCrit(true);
        } else if (natRoll == 1) {
            context.setHit(false);
        } else {
            boolean hits = totalRoll >= target.getArmorClass();
            context.setHit(hits);
            if (hits && context.isAutoCrit()) {
                context.setCrit(true);
            }
        }

        // TODO - onBeforeHitting / onBeforeGettingHit Hooks

        // 3. Damage roll
        String damageString = null;
        if (context.isHit()) {
            // TODO - onDamageRoll Hooks

            List<RolledDamage> rolledDamage = rollDamage(context.getDamageComponents(), context.isCrit());
            context.setRolledDamages(rolledDamage);

            for (RolledDamage damage : rolledDamage) {
                target.takeDamage(damage.amount(), damage.damageType());
            }

            // TODO - onHit Hooks

            damageString = rolledDamage.stream()
                    .map(rd -> rd.amount() + " " + rd.damageType())
                    .reduce((a, b) -> a + " + " + b).orElse("0");
        }

        String hitLabel = context.isHit() ? (context.isCrit() ? "CRITICALLY hits" : "hits") : (context.getNaturalRoll() == 1 ? "critically misses" : "misses");
        String advLabel = switch (context.getAdvantageState()) {
            case STRAIGHT -> "";
            case ADVANTAGE -> " (with advantage)";
            case DISADVANTAGE -> " (with disadvantage)";
        };
        String damageLabel = context.isHit() ? " and deals " + damageString + " point of damage. HP of the target: " + target.getHp() : "";

        combatLogger.log(() -> attacker.getName() + " " + hitLabel + " " + context.getDefinition().name() + " [" + totalRoll + advLabel + "]" + damageLabel);

        // TODO - afterAttackAgainst Hooks
    }

    public void resolveSavingThrow(SaveContext context, ICombatLogger logger) {
        Combatant target = context.getTarget();
        SaveActionDefinition definition = context.getDefinition();

        // TODO - onBeforeMakingEnemyMakeSave / onBeforeSave Hooks

        // 1. Advantage / Disadvantage / Straight d20 - save checking
        AdvantageState advState = diceRoller.getAdvantageState(
                context.getAdvantageCount(),
                context.getDisadvantageCount()
        );
        context.setAdvantageState(advState);

        if (context.isAutoFail()) {
            context.setSuccess(false);
        } else if (context.isAutoSuccess()) {
            context.setSuccess(true);
        } else {
            int naturalRoll = diceRoller.roll(20, advState);
            int totalSave = naturalRoll + context.getSaveBonus();
            context.setNaturalRoll(naturalRoll);
            context.setTotalRoll(totalSave);
            context.setSuccess(totalSave >= definition.saveDc());
        }

        List<RolledDamage> appliedDamages = new ArrayList<>();
        for (RolledDamage rd : context.getBaseDamages()) {
            int finalDamage = rd.amount();
            if (context.isSuccess()) {
                finalDamage = definition.halfDamageOnSuccess() ? Math.floorDiv(finalDamage, 2) : 0;
                // TODO - afterSuccessfulSave Hooks
            } else {
                // TODO - afterUnsuccessfulSave Hooks
            }
            if (finalDamage > 0) {
                target.takeDamage(finalDamage, rd.damageType());
                appliedDamages.add(new RolledDamage(finalDamage, rd.damageType()));
            }
        }
        context.setFinalDamages(appliedDamages);

        logger.log(() -> {
            String outcome = context.isSuccess() ? "SUCCEEDS on " : "FAILS ";
            String rollInfo = context.isAutoFail() ? "[AUTO-FAIL]"
                    : (context.isAutoSuccess() ? "[AUTO-SUCCESS]"
                    : "[" + context.getTotalRoll() + " vs DC " + definition.saveDc() + "]");
            String dmgInfo = context.getBaseDamages().isEmpty() ? ""
                    : " and takes " + context.getTotalDamageDealt() + " damage. HP: " + target.getHp();
            return target.getName() + " " + outcome + definition.saveAbility() + " save " + rollInfo + dmgInfo;
        });

        // TODO - afterSave Hooks

    }
}
