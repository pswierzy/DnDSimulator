package engine;

import ai.decider.AggressiveDecider;
import ai.decider.IDecider;
import ai.movement.AStarPathfinder;
import engine.actions.*;
import engine.core.*;
import engine.core.dice.IDiceRoller;
import engine.core.dice.StandardDiceRoller;
import engine.events.ConsoleLogger;
import engine.state.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        IDecider decider = new AggressiveDecider(new AStarPathfinder());
        EnumSet<MovementTrait> normalMovement = EnumSet.of(MovementTrait.NORMAL);
        List<IActionDefinition> goblinActions = List.of(
                new AttackDefinition("Dagger", ActionCost.ACTION, AttackType.MELEE, 1, +4, List.of(new DamageComponent("1d4+2", DamageType.PIERCING))),
                new AttackDefinition("Dagger Throw", ActionCost.ACTION, AttackType.RANGED, 8, +4, List.of(new DamageComponent("1d6+2", DamageType.PIERCING)))
        );
        List<IActionDefinition> guardActions = List.of(
                new AttackDefinition("Spear", ActionCost.ACTION, AttackType.MELEE, 1, +4, List.of(new DamageComponent("1d6+2", DamageType.PIERCING))),
                //new AttackDefinition("Polearm Master", ActionCost.BONUS_ACTION, AttackType.MELEE, 1, +3, List.of(new DamageComponent("1d4", DamageType.BLUDGEONING))),
                new SaveActionDefinition("Acid Vial", ActionCost.ACTION, 10, new AreaOfEffect(AreaShape.SPHERE, 1, 1), Ability.DEX, 14, true, List.of(new DamageComponent("1d4", DamageType.ACID)))
                );
        Map<Ability, Integer> goblinStats = Map.of(
                Ability.STR, -1, Ability.DEX, 2, Ability.CON, 0,
                Ability.INT, 0, Ability.WIS, -1, Ability.CHA, -1
        );
        Map<Ability, Integer> guardStats = Map.of(
                Ability.STR, 1, Ability.DEX, 2, Ability.CON, 1,
                Ability.INT, 0, Ability.WIS, 0, Ability.CHA, 0
        );

        Combatant goblin1 = new Combatant(
                "g1", "Goblin A", "monsters", 7, 12,
                new Vector2d(0, 0), goblinActions, 6, 1, normalMovement, decider,
                goblinStats, goblinStats
        );
        Combatant goblin2 = new Combatant(
                "g2", "Goblin B", "monsters", 7, 12,
                new Vector2d(0, 1), goblinActions, 6, 1, normalMovement, decider,
                goblinStats, goblinStats
        );
        Combatant hero = new Combatant(
                "h1", "Guard", "player", 16, 17,
                new Vector2d(9, 9), guardActions, 6, 1, normalMovement, decider,
                guardStats, guardStats
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
