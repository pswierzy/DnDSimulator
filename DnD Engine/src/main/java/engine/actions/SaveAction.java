package engine.actions;

import engine.context.TurnContext;
import engine.core.GridGeometry;
import engine.state.Combatant;
import engine.state.RolledDamage;
import engine.state.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class SaveAction implements IAction {
    private final Combatant performer;
    private final Vector2d targetTile;
    private final SaveActionDefinition definition;

    public SaveAction(Combatant performer, Vector2d targetTile, SaveActionDefinition definition) {
        this.performer = performer;
        this.targetTile = targetTile;
        this.definition = definition;
    }

    @Override
    public boolean canExecute(TurnContext turnContext) {
        if (performer.getHp() <= 0 || !isTileInRange()) return false;
        if (!turnContext.getGameState().getBattleMap().isWithinBounds(targetTile)) return false;

        if (definition.aoe() == null || definition.aoe().shape() == AreaShape.SINGLE_TARGET) {
            if (turnContext.getGameState().getAliveCombatantAt(targetTile) == null) {
                return false;
            }
        }

        return switch (definition.cost()) {
            case ACTION -> turnContext.hasAction();
            case BONUS_ACTION -> turnContext.hasBonusAction();
            case REACTION -> performer.hasReaction();
        };
    }

    @Override
    public void execute(TurnContext turnContext) {
        switch (definition.cost()) {
            case ACTION -> turnContext.consumeAction();
            case BONUS_ACTION -> turnContext.consumeBonusAction();
            case REACTION -> performer.useReaction();
        }

        List<Vector2d> affectedTiles = GridGeometry.calculateAoE(
                definition.aoe(), performer.getPosition(), targetTile, performer.getSize()
        );

        List<Combatant> affectedTargets = new ArrayList<>();
        for (Vector2d tile : affectedTiles) {
            if (turnContext.getGameState().getBattleMap().isWithinBounds(tile)) {
                Combatant c = turnContext.getGameState().getAliveCombatantAt(tile);
                if (c != null && !affectedTargets.contains(c)) {
                    affectedTargets.add(c);
                }
            }
        }
        List<RolledDamage> baseDamage = turnContext.getCombatResolver().rollDamage(definition.damageComponents());

        String damageString = baseDamage.stream()
                .map(rd -> rd.amount() + " " + rd.damageType())
                .reduce((a, b) -> a + " + " + b).orElse("0");


        turnContext.getCombatLogger().log(() ->
                performer.getName() + " casts " + definition.name() + " centered at " + targetTile +
                        " affecting " + affectedTargets.size() + " creature(s)!"
        );

        for (Combatant target : affectedTargets) {
            turnContext.getCombatResolver().resolveSavingThrow(
                    performer, target, definition, baseDamage, turnContext.getCombatLogger()
            );
        }
    }

    private boolean isTileInRange() {
        for (Vector2d performerTile : performer.getOccupiedTiles()) {
            if (performerTile.distance(targetTile) <= definition.range()) {
                return true;
            }
        }
        return false;
    }
}
