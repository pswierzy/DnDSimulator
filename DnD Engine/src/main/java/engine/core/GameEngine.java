package engine.core;

import engine.actions.AttackAction;
import engine.actions.IAction;
import engine.context.TurnContext;
import engine.events.ICombatLogger;
import engine.state.Combatant;
import engine.state.GameState;

public class GameEngine {
    private final GameState state;
    private final ICombatLogger logger;
    private final CombatResolver resolver;

    public GameEngine(GameState state, ICombatLogger logger, CombatResolver resolver) {
        this.state = state;
        this.logger = logger;
        this.resolver = resolver;
    }

    public void run() {
        logger.log(() -> "=== START SYMULACJI ===");
        while(!isCombatOver()) {
            Combatant curr = state.getNextCombatant();

            if (curr.getHp() <= 0) continue;

            TurnContext context = new TurnContext(state, logger, resolver);

            Combatant target = findNearestEnemy(curr);
            if (target != null) {
                IAction action = new AttackAction(curr, target);
                if(action.canExecute(context)) {
                    action.execute(context);
                }
            }
        }

        logger.log(() -> "=== END SYMULACJI ===");
    }

    private boolean isCombatOver() {
        String firstAliveFaction = null;
        for (Combatant c: state.getAllCombatants()) {
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

    private Combatant findNearestEnemy(Combatant attacker) {
        Combatant target = null;
        int minDistance = Integer.MAX_VALUE;
        for (Combatant c: state.getAllCombatants()) {
            if (c.getHp()>0 && !c.getFactionId().equals(attacker.getFactionId())) {
                int dist = attacker.getPosition().distance(c.getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    target = c;
                }
            }
        }
        return target;
    }
}
