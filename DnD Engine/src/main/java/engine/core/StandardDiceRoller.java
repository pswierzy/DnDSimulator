package engine.core;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardDiceRoller implements IDiceRoller {
    private final Random random;

    private static final Pattern ROLL_PATTERN = Pattern.compile("([+-]?)(?:(\\d*)d(\\d+)|(\\d+))", Pattern.CASE_INSENSITIVE);

    public StandardDiceRoller() {
        this.random = new Random();
    }
    public StandardDiceRoller(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public int roll(int faces) {
        if (faces <= 0) return 0;

        return random.nextInt(faces) + 1;
    }

    @Override
    public int parseRoll(String rolls) {
        if (rolls == null || rolls.isBlank()) {
            return 0;
        }

        int total = 0;
        String cleanRolls = rolls.replaceAll("\\s+", "");
        Matcher matcher = ROLL_PATTERN.matcher(cleanRolls);

        while (matcher.find()) {
            int sign = "-".equals(matcher.group(1)) ? -1 : 1;

            if (matcher.group(3) != null) {
                // Trafienie w schemat kości (np. "2d6", "d20")
                int numDice = matcher.group(2).isEmpty() ? 1 : Integer.parseInt(matcher.group(2));
                int faces = Integer.parseInt(matcher.group(3));

                for (int i = 0; i < numDice; i++) {
                    total += sign * roll(faces);
                }
            } else if (matcher.group(4) != null) {
                // Trafienie w płaski modyfikator (np. "+4", "-2")
                total += sign * Integer.parseInt(matcher.group(4));
            }
        }

        return total;
    }
}
