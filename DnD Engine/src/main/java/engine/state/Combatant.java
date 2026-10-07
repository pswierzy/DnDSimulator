package engine.state;

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

    private final List<Weapon> weapons;
    //List<Effect> effects;


    public Combatant(String id, String name, String factionId, int maxHp, int armorClass, Vector2d position, List<Weapon> weapons, int speed, int size, EnumSet<MovementTrait> movementTraits) {
        this.id = id;
        this.name = name;
        this.factionId = factionId;
        this.hp = maxHp;
        this.maxHp = maxHp;
        this.armorClass = armorClass;
        this.position = position;
        this.weapons = weapons;
        this.speed = speed;
        this.size = size;
        this.movementTraits = movementTraits;
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
    public List<Weapon> getWeapons() {
        return weapons;
    }
    public int getSpeed() { return speed; }
    public int getSize() { return size; }
    public boolean hasTrait(MovementTrait trait) {
        return movementTraits.contains(trait);
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

    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }
    public void setHp(int hp) {
        this.hp = hp;
    }
    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }
    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }
    public void setTempHp(int tempHp) {
        this.tempHp = tempHp;
    }
    public void setPosition(Vector2d position) {
        this.position = position;
    }
}
