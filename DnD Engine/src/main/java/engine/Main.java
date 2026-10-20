package engine;

import ai.decider.AggressiveDecider;
import ai.decider.IDecider;
import ai.movement.AStarPathfinder;
import engine.actions.ActionDefinition;
import engine.core.*;
import engine.events.ConsoleLogger;
import engine.state.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        IDecider decider = new AggressiveDecider(new AStarPathfinder());
        EnumSet<MovementTrait> normalMovement = EnumSet.of(MovementTrait.NORMAL);
        List<ActionDefinition> goblinActions = List.of(new ActionDefinition("Dagger", 1, +4, List.of(new DamageComponent("1d4+2", DamageType.PIERCING))));
        List<ActionDefinition> guardActions = List.of(new ActionDefinition("Spear", 2, +3, List.of(new DamageComponent("1d6+1", DamageType.PIERCING))));

        Combatant goblin1 = new Combatant(
                "g1", "Goblin A", "monsters", 7, 12, new Vector2d(0, 0), goblinActions, 6, 1, normalMovement, decider
        );
        Combatant goblin2 = new Combatant(
                "g2", "Goblin B", "monsters", 7, 12, new Vector2d(0, 1), goblinActions, 6, 1, normalMovement, decider
        );
        Combatant hero = new Combatant(
                "h1", "Hero", "player", 11, 16, new Vector2d(5, 5), guardActions, 6, 1, normalMovement, decider
        );

        List<Combatant> initiative = Arrays.asList(hero, goblin1, goblin2);

        BattleMap map = new BattleMap(10, 10);
        map.addWallTile(new Vector2d(2, 1));
        map.addWallTile(new Vector2d(3, 2));
        map.addWallTile(new Vector2d(3, 3));
        map.addWallTile(new Vector2d(3, 4));
        map.addWallTile(new Vector2d(3, 5));

        GameState state = new GameState(initiative, map);

        ConsoleLogger logger = new ConsoleLogger();
        IDiceRoller diceRoller = new StandardDiceRoller();
        AttackResolver resolver = new AttackResolver(diceRoller);

        GameEngine engine = new GameEngine(state, logger, resolver);

        engine.run();
    }
}
