package engine.core.dice;

import engine.core.dice.advantage.AdvantageRule;
import engine.core.dice.advantage.AdvantageState;
import engine.core.dice.advantage.IAdvantageStrategy;
import engine.core.dice.critical.CritRule;
import engine.core.dice.critical.ICritStrategy;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardDiceRoller implements IDiceRoller {
    private final Random random;
    private final ICritStrategy critStrategy;
    private final IAdvantageStrategy advantageStrategy;

    private static final Pattern ROLL_PATTERN = Pattern.compile("([+-]?)(?:(\\d*)d(\\d+)|(\\d+))", Pattern.CASE_INSENSITIVE);

    public StandardDiceRoller() {
        this(new Random(), CritRule.RAW, AdvantageRule.RAW);
    }
    public StandardDiceRoller(long seed) {
        this(new Random(seed), CritRule.RAW, AdvantageRule.RAW);
    }
    public StandardDiceRoller(ICritStrategy critStrategy) {
        this(new Random(), critStrategy, AdvantageRule.RAW);
    }
    public StandardDiceRoller(IAdvantageStrategy advantageStrategy) {
        this(new Random(), CritRule.RAW, advantageStrategy);
    }
    public StandardDiceRoller(long seed, ICritStrategy critStrategy) {
        this(new Random(seed), critStrategy, AdvantageRule.RAW);
    }
    public StandardDiceRoller(ICritStrategy critStrategy, IAdvantageStrategy advantageStrategy) {
        this(new Random(), critStrategy, advantageStrategy);
    }
    public StandardDiceRoller(long seed, IAdvantageStrategy advantageStrategy) {
        this(new Random(seed), CritRule.RAW, advantageStrategy);
    }
    public StandardDiceRoller(Random random, ICritStrategy critStrategy, IAdvantageStrategy advantageStrategy) {
        this.random = random;
        this.critStrategy = critStrategy;
        this.advantageStrategy = advantageStrategy;
    }

    @Override
    public int roll(int faces) { return roll(faces, AdvantageState.STRAIGHT); }
    @Override
    public int roll(int faces, AdvantageState advantageState) {
        if (faces <= 0) return 0;

        int fst_roll = random.nextInt(faces) + 1;
        if (advantageState == AdvantageState.STRAIGHT) return fst_roll;

        int snd_roll = random.nextInt(faces) + 1;
        if (advantageState == AdvantageState.ADVANTAGE) return Math.max(fst_roll, snd_roll);

        return Math.min(fst_roll, snd_roll);
    }

    @Override
    public int parseRoll(String rolls) { return parseRoll(rolls, false); }
    @Override
    public int parseRoll(String rolls, boolean isCrit) {
        if (rolls == null || rolls.isBlank()) {
            return 0;
        }

        int total = 0;
        String cleanRolls = rolls.replaceAll("\\s+", "");
        Matcher matcher = ROLL_PATTERN.matcher(cleanRolls);

        while (matcher.find()) {
            int sign = "-".equals(matcher.group(1)) ? -1 : 1;

            if (matcher.group(3) != null) {
                // Getting the dice pattern (e.g. "2d6", "d20")
                int numDice = matcher.group(2).isEmpty() ? 1 : Integer.parseInt(matcher.group(2));
                int faces = Integer.parseInt(matcher.group(3));
                int roll;

                if (isCrit) {
                    roll = critStrategy.rollCritDice(numDice, faces, this);
                } else {
                    roll = rollDice(numDice, faces, 0);
                }
                total += sign * roll;

            } else if (matcher.group(4) != null) {
                // Getting the modifier (e.g. "+4", "-2")
                total += sign * Integer.parseInt(matcher.group(4));
            }
        }
        return total;
    }

    @Override
    public int rollDice(int count, int faces, int bonus) {
        if (faces <= 0 || count <= 0) return Math.max(0, bonus);
        int sum = bonus;
        for (int i = 0; i < count; i++) { sum += roll(faces); }

        return sum;
    }

    @Override
    public AdvantageState getAdvantageState(int advantageCount, int disadvantageCount) {
        return advantageStrategy.resolve(advantageCount, disadvantageCount);
    }
}
