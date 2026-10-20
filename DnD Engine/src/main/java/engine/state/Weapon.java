package engine.state;

import java.util.List;

public class Weapon {
    private String id;
    private String name;
    private int range;
    private List<String> dmgDices;
    private List<String> damageTypes;

    public Weapon(String id, String name, int range, List<String> dmgDices, List<String> damageTypes) {
        this.id = id;
        this.name = name;
        this.range = range;
        this.dmgDices = dmgDices;
        this.damageTypes = damageTypes;
    }


    public List<String> getDamageTypes() {
        return damageTypes;
    }
    public List<String> getDmgDices() {
        return dmgDices;
    }
    public int getRange() {
        return range;
    }
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setRange(int range) {
        this.range = range;
    }
    public void setDmgDices(List<String> dmgDices) {
        this.dmgDices = dmgDices;
    }
    public void setDamageTypes(List<String> damageTypes) {
        this.damageTypes = damageTypes;
    }
}
