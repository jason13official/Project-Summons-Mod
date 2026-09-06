package io.github.jason13official.summons.impl.common.party;

/// Mirrors the six Innocent Devil types from Curse of Darkness (our Summon types).
/// A player's [CompanionParty] holds at most one companion per type
/// @see CompanionPartyManager
public enum CompanionType {

  FAIRY(false),
  BATTLE(true),
  BIRD(false),
  MAGE(false),
  DEVIL(true),
  PUMPKIN(false),
  ;

  private final boolean defendCapable;

  CompanionType(boolean defendCapable) {

    this.defendCapable = defendCapable;
  }

  /// Battle and Devil Type Summons/Innocent Devils additionally have a Defend mode
  public boolean isDefendCapable() {

    return this.defendCapable;
  }
}
