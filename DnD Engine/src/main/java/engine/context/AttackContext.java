package engine.context;

import engine.actions.AttackDefinition;
import engine.core.dice.advantage.AdvantageState;
import engine.state.Combatant;
import engine.state.DamageComponent;
import engine.state.GameState;
import engine.state.RolledDamage;

import java.util.ArrayList;
import java.util.List;

public class AttackContext {
    private final Combatant attacker;
    private final Combatant target;
    private final AttackDefinition definition;
    private final GameState gameState;

    private int advantageCount = 0;
    private int disadvantageCount = 0;
    private AdvantageState advantageState = AdvantageState.STRAIGHT;
    private int bonusToHit;

    private int naturalRoll;
    private int totalRoll;
    private boolean isCrit = false;
    private boolean autoCrit = false;
    private boolean isHit = false;

    private final List<DamageComponent> damageComponents;
    private final List<RolledDamage> rolledDamages = new ArrayList<>();

    public AttackContext(Combatant attacker, Combatant target, AttackDefinition definition, GameState gameState) {
        this.attacker = attacker;
        this.target = target;
        this.definition = definition;
        this.gameState = gameState;
        this.bonusToHit = definition.toHitBonus();
        this.damageComponents = new ArrayList<>(
                definition.damageComponents() != null ? definition.damageComponents() : List.of()
        );
    }

    public void addAdvantage() { this.advantageCount++; }
    public void addDisadvantage() { this.disadvantageCount++; }
    public void setAdvantageState(AdvantageState advantageState) { this.advantageState = advantageState; }
    public void addBonusToHit(int bonus) { this.bonusToHit += bonus; }
    public void addDamageComponent(DamageComponent component) { this.damageComponents.add(component); }

    public Combatant getAttacker() { return attacker; }
    public Combatant getTarget() { return target; }
    public AttackDefinition getDefinition() { return definition; }
    public GameState getGameState() { return gameState; }

    public int getAdvantageCount() { return advantageCount; }
    public int getDisadvantageCount() { return disadvantageCount; }
    public AdvantageState getAdvantageState() { return advantageState; }
    public int getBonusToHit() { return bonusToHit; }

    public int getNaturalRoll() { return naturalRoll; }
    public void setNaturalRoll(int naturalRoll) { this.naturalRoll = naturalRoll; }

    public int getTotalRoll() { return totalRoll; }
    public void setTotalRoll(int totalRoll) { this.totalRoll = totalRoll; }

    public boolean isCrit() { return isCrit; }
    public void setCrit(boolean crit) { this.isCrit = crit; }

    public boolean isAutoCrit() { return autoCrit; }
    public void setAutoCrit(boolean autoCrit) { this.autoCrit = autoCrit; }

    public boolean isHit() { return isHit; }
    public void setHit(boolean hit) { this.isHit = hit; }

    public List<DamageComponent> getDamageComponents() { return damageComponents; }
    public List<RolledDamage> getRolledDamages() { return rolledDamages; }

    public void setRolledDamages(List<RolledDamage> damages) {
        this.rolledDamages.clear();
        this.rolledDamages.addAll(damages);
    }

    public int getTotalDamageDealt() {
        return rolledDamages.stream().mapToInt(RolledDamage::amount).sum();
    }
}
