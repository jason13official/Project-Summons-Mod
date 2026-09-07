package io.github.jason13official.summons.impl.common.evolution;

/// One evolution requirement: reaching `amount` points of `color` alone (colors are not
/// combined) triggers it. A companion can list several as alternate routes to the same stage.
public record EvolutionThreshold(EvoCrystalColor color, int amount) {
}
