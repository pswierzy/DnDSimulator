package engine.state;

import engine.core.metric.GridGeometry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameState {
    private List<Combatant> initiativeList;
    private BattleMap battleMap;

    private Queue<Combatant> currentInitiative = new LinkedList<>();
    private int currRound = 1;

    public GameState(List<Combatant> initiative, BattleMap battleMap) {
        this.initiativeList = initiative;
        currentInitiative.addAll(initiativeList);
        this.battleMap = battleMap;
    }

    public GridGeometry getGeometry() {
        return battleMap.getGeometry();
    }

    public Combatant getNextCombatant() {
        if (currentInitiative.isEmpty()) {
            currentInitiative.addAll(initiativeList);
            currRound++;
        }

        return currentInitiative.poll();
    }

    public List<Combatant> getAllCombatants() {
        return initiativeList;
    }

    public Combatant getAliveCombatantAt(Vector2d position) {
        for (Combatant combatant : initiativeList) {
            if (combatant.getHp() > 0 && combatant.getOccupiedTiles().contains(position)) {
                return combatant;
            }
        }
        return null;
    }
    public List<Combatant> getAliveCombatantsInRadius(Vector2d centre, int radius) {
        List<Combatant> result = new ArrayList<>();
        for (Combatant c : initiativeList) {
            if (c.getHp() <= 0) continue;
            for (Vector2d occupied : c.getOccupiedTiles()) {
                if (this.getGeometry().getMetric().measureDistance(centre, occupied) <= radius) {
                    result.add(c);
                    break;
                }
            }
        }
        return result;
    }

    public int getCurrRound() {
        return currRound;
    }
    public BattleMap getBattleMap() {
        return battleMap;
    }
}
