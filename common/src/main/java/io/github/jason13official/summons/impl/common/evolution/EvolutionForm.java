package io.github.jason13official.summons.impl.common.evolution;

/// One named form in a companion type's evolution chart (e.g. `FairyForm.LEAFFLE`).
/// Implementors should be enums; `id()` should just return `name()`.
public interface EvolutionForm {
  String id();

  String displayName();

  int stage();
}
