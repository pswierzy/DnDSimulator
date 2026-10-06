package engine.context;

import engine.core.CombatResolver;
import engine.events.ICombatLogger;
import engine.state.GameState;

public class TurnContext {
    private final GameState gameState;
    private final ICombatLogger combatLogger;
    private final CombatResolver combatResolver;

    public TurnContext(GameState gameState, ICombatLogger combatLogger, CombatResolver combatResolver) {
        this.gameState = gameState;
        this.combatLogger = combatLogger;
        this.combatResolver = combatResolver;
    }

    public GameState getGameState() {
        return gameState;
    }
    public ICombatLogger getCombatLogger() {
        return combatLogger;
    }
    public CombatResolver getCombatResolver() {
        return combatResolver;
    }
}
