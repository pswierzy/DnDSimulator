package engine.core;

import engine.actions.SaveActionDefinition;
import engine.context.AttackContext;
import engine.core.dice.advantage.AdvantageRule;
import engine.core.dice.advantage.AdvantageState;
import engine.core.dice.advantage.IAdvantageStrategy;
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
                && isEnemyInMeleeReach(attacker, context.getGameState())) {
            context.addDisadvantage();
        }

        // TODO - onBeforeAttackAsAttacker / onBeforeAttackAsTarget Hooks

        // 2. Advantage / Disadvantage / Straight d20 - hit checking
        AdvantageState advState = diceRoller.getAdvantageState(
                context.getAdvantageCount(),
                context.getDisadvantageCount()
        );
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
        String advLabel = switch (advState) {
            case STRAIGHT -> "";
            case ADVANTAGE -> " (with advantage)";
            case DISADVANTAGE -> " (with disadvantage)";
        };
        String damageLabel = context.isHit() ? " and deals " + damageString + " point of damage. HP of the target: " + target.getHp() : "";

        combatLogger.log(() -> attacker.getName() + " " + hitLabel + " " + context.getDefinition().name() + " [" + totalRoll + advLabel + "]" + damageLabel);

        // TODO - afterAttackAgainst Hooks
    }

    // TODO with advantages and disadvantages
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

    private boolean isEnemyInMeleeReach(Combatant attacker, GameState state) {
        if (state == null) return false;
        for (Combatant other : state.getAllCombatants()) {
            if (other.getHp() > 0 && !other.getFactionId().equals(attacker.getFactionId())) {
                for (Vector2d enemyTile : other.getOccupiedTiles()) {
                    if (state.getGeometry().isInRange(attacker.getPosition(), attacker.getSize(), enemyTile, 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
