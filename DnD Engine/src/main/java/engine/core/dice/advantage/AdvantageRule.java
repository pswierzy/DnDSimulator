package engine.core.dice.advantage;

public enum AdvantageRule implements IAdvantageStrategy {
    RAW {
        @Override
        public AdvantageState resolve(int advantageCount, int disadvantageCount) {
            if (advantageCount > 0 &&  disadvantageCount > 0) return AdvantageState.STRAIGHT;
            if (advantageCount > 0) return AdvantageState.ADVANTAGE;
            if (disadvantageCount > 0) return AdvantageState.DISADVANTAGE;
            return AdvantageState.STRAIGHT;
        }
    },
    CUMULATIVE {
        @Override
        public AdvantageState resolve(int advantageCount, int disadvantageCount) {
            int net = advantageCount - disadvantageCount;
            if (net > 0) return AdvantageState.ADVANTAGE;
            if (net < 0) return AdvantageState.DISADVANTAGE;
            return AdvantageState.STRAIGHT;
        }
    }
}
