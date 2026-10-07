package engine.state;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameState {
    private List<Combatant> initiativeList;
    private BattleMap battleMap;

    private Queue<Combatant> currentInitiative = new LinkedList<>();
    private int currRound = 0;

    public GameState(List<Combatant> initiative, BattleMap battleMap) {
        this.initiativeList = initiative;
        currentInitiative.addAll(initiativeList);
        this.battleMap = battleMap;
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

    public int getCurrRound() {
        return currRound;
    }
    public BattleMap getBattleMap() {
        return battleMap;
    }
}
