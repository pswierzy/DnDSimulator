package engine.core;

import engine.state.DamageComponent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DiceMath {
    private static final Pattern DICE_PATTERN = Pattern.compile("([+-]?)(?:(\\d*)d(\\d+)|(\\d+))", Pattern.CASE_INSENSITIVE);

    private DiceMath() {}

    public static double calculateAverageDamage(List<DamageComponent> components) {
        if (components == null || components.isEmpty()) return 0.0;
        double total = 0.0;
        for (DamageComponent comp : components) {
            total += averageFromExpression(comp.dice());
        }
        return Math.max(0.0, total);
    }

    public static double averageFromExpression(String expr) {
        if (expr == null || expr.isBlank()) return 0.0;
        double sum = 0.0;
        Matcher matcher = DICE_PATTERN.matcher(expr.replaceAll("\\s+", ""));
        while (matcher.find()) {
            int sign = "-".equals(matcher.group(1)) ? -1 : 1;
            if (matcher.group(3) != null) {
                int count = matcher.group(2).isEmpty() ? 1 : Integer.parseInt(matcher.group(2));
                int faces = Integer.parseInt(matcher.group(3));
                sum += sign * count * ((faces + 1) / 2.0);
            } else if (matcher.group(4) != null) {
                sum += sign * Integer.parseInt(matcher.group(4));
            }
        }
        return sum;
    }
}
