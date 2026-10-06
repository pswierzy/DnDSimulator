package engine;

import engine.core.CombatResolver;
import engine.core.GameEngine;
import engine.core.IDiceRoller;
import engine.core.StandardDiceRoller;
import engine.events.ConsoleLogger;
import engine.state.Combatant;
import engine.state.GameState;
import engine.state.Vector2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Combatant goblin1 = new Combatant("g1", "Goblin A", "monsters", 3, 15, new Vector2d(0, 0), new ArrayList<>());
        Combatant goblin2 = new Combatant("g2", "Goblin B", "monsters", 3, 15, new Vector2d(0, 1), new ArrayList<>());
        Combatant hero = new Combatant("h1", "Hero", "player", 7, 16, new Vector2d(5, 5), new ArrayList<>());

        List<Combatant> initiative = Arrays.asList(hero, goblin1, goblin2);

        GameState state = new GameState(initiative);
        ConsoleLogger logger = new ConsoleLogger();

        IDiceRoller diceRoller = new StandardDiceRoller();
        CombatResolver resolver = new CombatResolver(diceRoller);

        GameEngine engine = new GameEngine(state, logger, resolver);
        engine.run();
    }
}
