package engine.state;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameState {
    private List<Combatant> initiativeList;

    private Queue<Combatant> currentInitiative = new LinkedList<>();
    private int currRound = 0;

    public GameState(List<Combatant> initiative) {
        this.initiativeList = initiative;
        currentInitiative.addAll(initiativeList);
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
}
