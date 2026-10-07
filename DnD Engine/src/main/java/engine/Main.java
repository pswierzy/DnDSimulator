package engine;

import engine.core.*;
import engine.events.ConsoleLogger;
import engine.state.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        EnumSet<MovementTrait> normalMovement = EnumSet.of(MovementTrait.NORMAL);
        Combatant goblin1 = new Combatant("g1", "Goblin A", "monsters", 3, 15, new Vector2d(0, 0), new ArrayList<>(), 6, 1, normalMovement);
        Combatant goblin2 = new Combatant("g2", "Goblin B", "monsters", 3, 15, new Vector2d(0, 1), new ArrayList<>(), 6, 1, normalMovement);
        Combatant hero = new Combatant("h1", "Hero", "player", 15, 16, new Vector2d(5, 5), new ArrayList<>(), 6, 1, normalMovement);

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
        CombatResolver resolver = new CombatResolver(diceRoller);
        IPathfinder pathfinder = new AStarPathfinder();

        GameEngine engine = new GameEngine(state, logger, resolver, pathfinder);

        engine.run();
    }
}
