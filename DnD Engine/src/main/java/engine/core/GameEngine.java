package engine.core;

import ai.decider.IDecider;
import engine.actions.IAction;
import engine.context.TurnContext;
import engine.events.ICombatLogger;
import engine.state.Combatant;
import engine.state.GameState;

public class GameEngine {
    private final GameState state;
    private final ICombatLogger logger;
    private final AttackResolver resolver;

    public GameEngine(GameState state, ICombatLogger logger, AttackResolver resolver) {
        this.state = state;
        this.logger = logger;
        this.resolver = resolver;
    }

    public void run() {
        logger.log(() -> "=== START SYMULACJI ===");
        while (runSingleTurn()) {}
        logger.log(() -> "=== END SYMULACJI ===");
    }

    public boolean runSingleTurn() {
        if (isCombatOver()) {
            return false;
        }

        Combatant curr = state.getNextCombatant();
        if (curr.getHp() <= 0) {
            return true;
        }

        TurnContext context = new TurnContext(state, logger, resolver, curr.getSpeed());

        while (curr.getHp() > 0) {
            IAction action = curr.getDecider().decideNextAction(curr, context);
            if (action == null || !action.canExecute(context)) {
                break;
            }
            action.execute(context);
        }

        return !isCombatOver();
    }

    public boolean isCombatOver() {
        String firstAliveFaction = null;
        for (Combatant c : state.getAllCombatants()) {
            if (c.getHp() > 0) {
                if (firstAliveFaction == null) {
                    firstAliveFaction = c.getFactionId();
                } else if (!firstAliveFaction.equals(c.getFactionId())) {
                    return false;
                }
            }
        }
        return true;
    }
}
