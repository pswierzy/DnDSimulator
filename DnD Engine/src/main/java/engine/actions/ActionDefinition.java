package engine.actions;

import engine.state.DamageComponent;

import java.util.List;

public record ActionDefinition(
        String name,
        int range,
        int toHitBonus,
        List<DamageComponent> damageComponents
) {}