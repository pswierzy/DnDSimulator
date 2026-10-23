package engine.context;

import engine.actions.SaveActionDefinition;
import engine.core.dice.advantage.AdvantageState;
import engine.state.Combatant;
import engine.state.GameState;
import engine.state.RolledDamage;

import java.util.ArrayList;
import java.util.List;

public class SaveContext {
    private final Combatant performer;
    private final Combatant target;
    private final SaveActionDefinition definition;
    private final GameState gameState;

    private int advantageCount = 0;
    private int disadvantageCount = 0;
    private AdvantageState advantageState = AdvantageState.STRAIGHT;
    private int saveBonus;

    private int naturalRoll;
    private int totalRoll;
    private boolean autoFail = false;
    private boolean autoSuccess = false;
    private boolean isSuccess = false;

    private final List<RolledDamage> baseDamages;
    private final List<RolledDamage> finalDamages = new ArrayList<>();

    public SaveContext(Combatant performer, Combatant target, SaveActionDefinition definition,
                       List<RolledDamage> baseDamages, GameState gameState) {
        this.performer = performer;
        this.target = target;
        this.definition = definition;
        this.gameState = gameState;
        this.saveBonus = target.getSaveBonus(definition.saveAbility());
        this.baseDamages = baseDamages != null ? List.copyOf(baseDamages) : List.of();
    }

    public void addAdvantage() { this.advantageCount++; }
    public void addDisadvantage() { this.disadvantageCount++; }
    public void addSaveBonus(int bonus) { this.saveBonus += bonus; }

    public Combatant getPerformer() { return performer; }
    public Combatant getTarget() { return target; }
    public SaveActionDefinition getDefinition() { return definition; }
    public GameState getGameState() { return gameState; }

    public int getAdvantageCount() { return advantageCount; }
    public int getDisadvantageCount() { return disadvantageCount; }
    public AdvantageState getAdvantageState() { return advantageState; }
    public void setAdvantageState(AdvantageState advantageState) { this.advantageState = advantageState; }
    public int getSaveBonus() { return saveBonus; }

    public int getNaturalRoll() { return naturalRoll; }
    public void setNaturalRoll(int naturalRoll) { this.naturalRoll = naturalRoll; }
    public int getTotalRoll() { return totalRoll; }
    public void setTotalRoll(int totalRoll) { this.totalRoll = totalRoll; }

    public boolean isAutoFail() { return autoFail; }
    public void setAutoFail(boolean autoFail) { this.autoFail = autoFail; }
    public boolean isAutoSuccess() { return autoSuccess; }
    public void setAutoSuccess(boolean autoSuccess) { this.autoSuccess = autoSuccess; }
    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean success) { this.isSuccess = success; }

    public List<RolledDamage> getBaseDamages() { return baseDamages; }
    public List<RolledDamage> getFinalDamages() { return finalDamages; }
    public void setFinalDamages(List<RolledDamage> damages) {
        this.finalDamages.clear();
        this.finalDamages.addAll(damages);
    }

    public int getTotalDamageDealt() {
        return finalDamages.stream().mapToInt(RolledDamage::amount).sum();
    }
}