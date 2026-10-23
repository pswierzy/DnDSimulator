package engine.state;

import ai.decider.IDecider;
import engine.actions.ActionCost;
import engine.actions.AttackDefinition;
import engine.actions.AttackType;
import engine.actions.IActionDefinition;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class Combatant {
    private String id;
    private String name;
    private String factionId;
    private int hp;
    private int maxHp;
    private int armorClass;
    private int tempHp = 0;
    private Vector2d position;
    private int speed;
    private int size;
    private EnumSet<MovementTrait> movementTraits;

    private final List<IActionDefinition> availableActions;
    //List<Effect> effects;
    private IDecider decider;

    private boolean hasReaction = true;

    private final Map<Ability, Integer> abilityModifiers;
    private final Map<Ability, Integer> saveBonuses;

    // Spell ATK bonus / spell save DC

    public Combatant(String id, String name, String factionId, int maxHp, int armorClass, Vector2d position, List<IActionDefinition> availableActions, int speed, int size, EnumSet<MovementTrait> movementTraits, IDecider decider, Map<Ability, Integer> abilityModifiers, Map<Ability, Integer> saveBonuses) {
        this.id = id;
        this.name = name;
        this.factionId = factionId;
        this.hp = maxHp;
        this.maxHp = maxHp;
        this.armorClass = armorClass;
        this.position = position;
        this.availableActions = availableActions;
        this.speed = speed;
        this.size = size;
        this.movementTraits = movementTraits;
        this.decider = decider;
        this.abilityModifiers = abilityModifiers;
        this.saveBonuses = saveBonuses;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getFactionId() {
        return factionId;
    }
    public int getHp() {
        return hp;
    }
    public int getMaxHp() {
        return maxHp;
    }
    public int getArmorClass() {
        return armorClass;
    }
    public int getTempHp() {
        return tempHp;
    }
    public Vector2d getPosition() {
        return position;
    }
    public List<IActionDefinition> getAvailableActions() {
        return availableActions;
    }
    public int getSpeed() { return speed; }
    public int getSize() { return size; }
    public boolean hasTrait(MovementTrait trait) {
        return movementTraits.contains(trait);
    }
    public IDecider getDecider() {
        return decider;
    }
    public boolean hasReaction() {
        return hasReaction;
    }
    public int getAbilityModifier(Ability ability) {
        return abilityModifiers.getOrDefault(ability, 0);
    }
    public int getSaveBonus(Ability ability) {
        return saveBonuses.getOrDefault(ability, 0);
    }

    public List<Vector2d> getOccupiedTiles() {
        List<Vector2d> tiles = new ArrayList<>();
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                tiles.add(new Vector2d(position.x() + dx, position.y() + dy));
            }
        }
        return tiles;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }
    public void takeDamage(int damage, DamageType damageType) {
        if (damage <= 0) return;
        int remainingDamage = damage;
        if (tempHp > 0) {
            int absorbed = Math.min(tempHp, remainingDamage);
            this.tempHp -= absorbed;
            remainingDamage -= absorbed;
        }
        this.hp = Math.max(0, this.hp - remainingDamage);
    }
    public void heal(int value) {
        this.hp = Math.min(this.hp + value, this.maxHp);
    }
    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }
    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }
    public void setTempHp(int tempHp) {
        this.tempHp = Math.max(tempHp, this.tempHp);
    }
    public void setPosition(Vector2d position) {
        this.position = position;
    }
    public void setDecider(IDecider decider) {
        this.decider = decider;
    }
    public void useReaction() {
        this.hasReaction = false;
    }
    public void resetReaction() {
        this.hasReaction = true;
    }
}
