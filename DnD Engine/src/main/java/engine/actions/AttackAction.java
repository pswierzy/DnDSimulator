package engine.actions;

import engine.context.TurnContext;
import engine.state.Combatant;
import engine.state.Vector2d;

public class AttackAction implements IAction {
    private final Combatant attacker;
    private final Vector2d targetTile;
    private final AttackDefinition definition;

    public AttackAction(Combatant attacker, Vector2d targetTile, AttackDefinition definition) {
        this.attacker = attacker;
        this.targetTile = targetTile;
        this.definition = definition;
    }

    @Override
    public boolean canExecute(TurnContext turnContext) {
        if (attacker.getHp() <= 0 || !isInRange()) {
            return false;
        }
        Combatant target = turnContext.getGameState().getAliveCombatantAt(targetTile);
        if (target == null) {
            return false; // Na tym kafelku nie ma żywego celu
        }
        return switch (definition.cost()) {
            case ACTION -> turnContext.hasAction();
            case BONUS_ACTION -> turnContext.hasBonusAction();
            case REACTION -> attacker.hasReaction();
        };
    }

    @Override
    public void execute(TurnContext turnContext) {
        Combatant target = turnContext.getGameState().getAliveCombatantAt(targetTile);
        if (target == null) return;

        switch (definition.cost()) {
            case ACTION -> turnContext.consumeAction();
            case BONUS_ACTION -> turnContext.consumeBonusAction();
            case REACTION -> attacker.useReaction();
        }

        turnContext.getCombatResolver().resolveAttack(
                attacker, target, definition, turnContext.getCombatLogger()
        );
    }

    private boolean isInRange() {
        for (Vector2d attackerTile : attacker.getOccupiedTiles()) {
            if (attackerTile.distance(targetTile) <= definition.range()) {
                return true;
            }
        }
        return false;
    }
}
