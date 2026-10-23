package engine.core.dice;

import engine.core.dice.advantage.AdvantageState;

public interface IDiceRoller {
    int roll(int faces);
    int roll(int faces, AdvantageState advantageState);
    int parseRoll(String rolls);
    int parseRoll(String rolls, boolean isCrit);
    int rollDice(int count, int faces, int bonus);

    AdvantageState getAdvantageState(int advantageCount, int disadvantageCount);
}
