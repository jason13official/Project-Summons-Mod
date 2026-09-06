package io.github.jason13official.summons.impl.common.party;

/// UP/DOWN cycles Auto <-> Command (<-> Defend, if [CompanionType#isDefendCapable]).
/// LEFT/RIGHT cycles the selected ability while in Command mode; the COMMAND keybind uses it.
public enum CompanionMode {
  AUTO,
  COMMAND,
  DEFEND
}
