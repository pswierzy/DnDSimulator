package engine.context;

import engine.core.AttackResolver;
import engine.events.ICombatLogger;
import engine.state.GameState;

public class TurnContext {
    private final GameState gameState;
    private final ICombatLogger combatLogger;
    private final AttackResolver attackResolver;

    private boolean actionUsed = false;
    private boolean bonusActionUsed = false;
    private int movementRemaining;

    public TurnContext(GameState gameState, ICombatLogger combatLogger, AttackResolver attackResolver, int initialMovement) {
        this.gameState = gameState;
        this.combatLogger = combatLogger;
        this.attackResolver = attackResolver;
        this.movementRemaining = initialMovement;
    }

    public boolean hasAction() { return !actionUsed; }
    public void consumeAction() { this.actionUsed = true; }

    public int getMovementRemaining() { return movementRemaining; }
    public void consumeMovement(int cost) { this.movementRemaining -= cost; }

    public GameState getGameState() {
        return gameState;
    }
    public ICombatLogger getCombatLogger() {
        return combatLogger;
    }
    public AttackResolver getCombatResolver() {
        return attackResolver;
    }
}
