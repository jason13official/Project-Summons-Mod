package io.github.jason13official.summons.impl.common.party;

/// Mirrors the six Innocent Devil types from Curse of Darkness (our Summon types).
/// A player's [CompanionParty] holds at most one companion per type
/// @see CompanionPartyManager
public enum CompanionType {

  FAIRY(false, false),
  BATTLE(true, true),
  BIRD(false, true),
  MAGE(false, true),
  DEVIL(true, true),
  PUMPKIN(false, true),
  ;

  private final boolean defendCapable;
  private final boolean chainAttackCapable;

  CompanionType(boolean defendCapable, boolean chainAttackCapable) {

    this.defendCapable = defendCapable;
    this.chainAttackCapable = chainAttackCapable;
  }

  /// Battle and Devil Type Summons/Innocent Devils additionally have a Defend mode
  public boolean isDefendCapable() {

    return this.defendCapable;
  }

  /// every type but Fairy can Chain Attack
  public boolean isChainAttackCapable() {

    return this.chainAttackCapable;
  }
}
