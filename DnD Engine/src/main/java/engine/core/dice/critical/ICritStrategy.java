package engine.core.dice.critical;

import engine.core.dice.IDiceRoller;

@FunctionalInterface
public interface ICritStrategy {
    int rollCritDice(int count, int faces, IDiceRoller diceRoller);
}
