package engine;

import ai.decider.AggressiveDecider;
import ai.decider.IDecider;
import ai.movement.AStarPathfinder;
import engine.actions.ActionCost;
import engine.actions.AttackDefinition;
import engine.actions.AttackType;
import engine.actions.IActionDefinition;
import engine.core.*;
import engine.events.ConsoleLogger;
import engine.state.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        IDecider decider = new AggressiveDecider(new AStarPathfinder());
        EnumSet<MovementTrait> normalMovement = EnumSet.of(MovementTrait.NORMAL);
        List<IActionDefinition> goblinActions = List.of(
                new AttackDefinition("Dagger", ActionCost.ACTION, AttackType.MELEE, 1, +4, List.of(new DamageComponent("1d4+2", DamageType.PIERCING))),
                new AttackDefinition("Dagger Throw", ActionCost.ACTION, AttackType.RANGED, 8, +4, List.of(new DamageComponent("1d6+2", DamageType.PIERCING)))
        );
        List<IActionDefinition> guardActions = List.of(
                new AttackDefinition("Spear", ActionCost.ACTION, AttackType.MELEE, 1, +3, List.of(new DamageComponent("1d6+1", DamageType.PIERCING)))
               ,new AttackDefinition("Polearm Master", ActionCost.BONUS_ACTION, AttackType.MELEE, 1, +3, List.of(new DamageComponent("1d4", DamageType.BLUDGEONING)))
        );

        Combatant goblin1 = new Combatant(
                "g1", "Goblin A", "monsters", 7, 12, new Vector2d(0, 0), goblinActions, 6, 1, normalMovement, decider
        );
        Combatant goblin2 = new Combatant(
                "g2", "Goblin B", "monsters", 7, 12, new Vector2d(0, 1), goblinActions, 6, 1, normalMovement, decider
        );
        Combatant hero = new Combatant(
                "h1", "Guard", "player", 16, 17, new Vector2d(10, 10), guardActions, 6, 1, normalMovement, decider
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
