package io.github.jason13official.summons.impl.common.party;

import java.util.Optional;

/// Mirrors the six Innocent Devil types from Curse of Darkness (our Summon types). A player's [CompanionParty] holds at most one companion per type
///
/// @see CompanionPartyManager
public enum CompanionType {

  FAIRY(false, false, "The first Innocent Devil Hector forges. Weak in a fight, but its "
      + "healing and support magic keep him alive."),
  BATTLE(true, true, "A hulking brawler built for melee. High HP and raw attack power, "
      + "at the cost of speed and finesse."),
  BIRD(false, true, "A swift flier that can lift Hector into the air to clear gaps. Agile "
      + "against light foes, but struggles against armor."),
  MAGE(false, true, "A frail spellcaster wielding a rod of arcane gadgets. Its elemental "
      + "magic covers nearly every enemy's weakness."),
  DEVIL(true, true, "A balance between Battle- and Bird-Type: fast, aggressive, and able "
      + "to fly and Chain Attack alike."),
  PUMPKIN(false, true, "Nearly useless in combat, but grants Hector the largest stat "
      + "boost of any Innocent Devil."),
  ;

  private final boolean defendCapable;
  private final boolean chainAttackCapable;
  private final String description;

  CompanionType(boolean defendCapable, boolean chainAttackCapable, String description) {

    this.defendCapable = defendCapable;
    this.chainAttackCapable = chainAttackCapable;
    this.description = description;
  }

  /// canonical Summon Gate unlock order (declaration order: Fairy, Battle, Bird, Mage, Devil, Pumpkin); the first not-yet-unlocked type in a party, or empty once all six are unlocked
  public static Optional<CompanionType> nextLocked(CompanionParty party) {
    for (CompanionType type : values()) {
      if (!party.isUnlocked(type)) {
        return Optional.of(type);
      }
    }

    return Optional.empty();
  }

  /// one-line lore blurb for the Summons menu/stats screens
  public String description() {
    return this.description;
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
