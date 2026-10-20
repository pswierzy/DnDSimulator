package engine.state;

import ai.decider.IDecider;
import engine.actions.ActionDefinition;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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

    private final List<ActionDefinition> availableActions;
    //List<Effect> effects;
    private IDecider decider;


    public Combatant(String id, String name, String factionId, int maxHp, int armorClass, Vector2d position, List<ActionDefinition> availableActions, int speed, int size, EnumSet<MovementTrait> movementTraits, IDecider decider) {
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
    public List<ActionDefinition> getAvailableActions() {
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

    public List<Vector2d> getOccupiedTiles() {
        List<Vector2d> tiles = new ArrayList<>();
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                tiles.add(new Vector2d(position.getX() + dx, position.getY() + dy));
            }
        }
        return tiles;
    }

    public ActionDefinition getPrimaryAction() {
        if (availableActions == null || availableActions.isEmpty()) {
            return new ActionDefinition("Fists", 1, 2, List.of(new DamageComponent("1", DamageType.BLUDGEONING)));
        }
        return availableActions.get(0);
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
    public int takeDamage(int damage, DamageType damageType) {
        this.hp -= damage;
        return damage;
    }
    public void heal(int value) {
        this.hp += Math.min(value, maxHp);
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
}
