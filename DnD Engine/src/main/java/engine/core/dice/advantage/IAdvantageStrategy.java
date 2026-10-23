package engine.core.dice.advantage;

@FunctionalInterface
public interface IAdvantageStrategy {
    AdvantageState resolve(int advantageCount, int disadvantageCount);
}
