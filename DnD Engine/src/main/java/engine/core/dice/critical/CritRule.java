package engine.core.dice.critical;

import engine.core.dice.IDiceRoller;

public enum CritRule implements ICritStrategy {
    RAW {           // Rules As Written - double dice roll
        @Override
        public int rollCritDice(int count, int faces, IDiceRoller diceRoller) {
            int doubledCount = count * 2;
            return diceRoller.rollDice(doubledCount, faces, 0);
        }
    },

    DOUBLE_VALUE {  // Swingier than RAW but same avg
        @Override
        public int rollCritDice(int count, int faces, IDiceRoller diceRoller) {
            return 2 * diceRoller.rollDice(count, faces, 0);
        }
    },

    PERKINS_CRIT {  // Max dice roll + regular roll
        @Override
        public int rollCritDice(int count, int faces, IDiceRoller diceRoller) {
            int maxRoll = count * faces;
            int regularRoll = diceRoller.rollDice(count, faces, 0);
            return maxRoll + regularRoll;
        }
    },

    FLAT_MAX {      // Max dice roll
        @Override
        public int rollCritDice(int count, int faces, IDiceRoller diceRoller) {
            return count * faces;
        }
    },

    DOUBLE_FLAT_MAX {
        @Override
        public int rollCritDice(int count, int faces, IDiceRoller diceRoller) {
            return count * faces * 2;
        }
    }
}
