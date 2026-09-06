package io.github.jason13official.summons.impl.client;

import io.github.jason13official.summons.impl.common.network.SummonsNetworking.Action;
import io.github.jason13official.summons.platform.Services;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/// mirrors Curse of Darkness Innocent Devil controls:
/// - UP/DOWN cycles Auto <-> Command <-> Defend,
/// - LEFT/RIGHT cycles the selected ability while in Command mode,
/// - COMMAND activates it the selected ability in Command mode,
/// - TOGGLE_SUMMON/CYCLE_PARTY condense current summon back to party/switch to next member
public class SummonsKeyBindings {

  private static final String CATEGORY = "key.categories.summons";

  public static final KeyMapping TOGGLE_SUMMON = new KeyMapping("key.summons.toggle_summon", GLFW.GLFW_KEY_G, CATEGORY);
  public static final KeyMapping CYCLE_PARTY = new KeyMapping("key.summons.cycle_party", GLFW.GLFW_KEY_H, CATEGORY);
  public static final KeyMapping MODE_UP = new KeyMapping("key.summons.mode_up", GLFW.GLFW_KEY_UP, CATEGORY);
  public static final KeyMapping MODE_DOWN = new KeyMapping("key.summons.mode_down", GLFW.GLFW_KEY_DOWN, CATEGORY);
  public static final KeyMapping ABILITY_LEFT = new KeyMapping("key.summons.ability_left", GLFW.GLFW_KEY_LEFT, CATEGORY);
  public static final KeyMapping ABILITY_RIGHT = new KeyMapping("key.summons.ability_right", GLFW.GLFW_KEY_RIGHT, CATEGORY);
  public static final KeyMapping COMMAND = new KeyMapping("key.summons.command", GLFW.GLFW_KEY_J, CATEGORY);

  public static KeyMapping[] all() {

    return new KeyMapping[]{
        TOGGLE_SUMMON,
        CYCLE_PARTY,
        MODE_UP,
        MODE_DOWN,
        ABILITY_LEFT,
        ABILITY_RIGHT,
        COMMAND
    };
  }

  public static void tickKeyBindings() {
    tick();
  }

  /// once per client tick; consume and dispatch any buffered key presses
  private static void tick() {
    while (TOGGLE_SUMMON.consumeClick()) {
      Services.network().sendCompanionInput(Action.TOGGLE_SUMMON);
    }
    while (CYCLE_PARTY.consumeClick()) {
      Services.network().sendCompanionInput(Action.CYCLE_PARTY);
    }
    while (MODE_UP.consumeClick()) {
      Services.network().sendCompanionInput(Action.MODE_UP);
    }
    while (MODE_DOWN.consumeClick()) {
      Services.network().sendCompanionInput(Action.MODE_DOWN);
    }
    while (ABILITY_LEFT.consumeClick()) {
      Services.network().sendCompanionInput(Action.ABILITY_LEFT);
    }
    while (ABILITY_RIGHT.consumeClick()) {
      Services.network().sendCompanionInput(Action.ABILITY_RIGHT);
    }
    while (COMMAND.consumeClick()) {
      Services.network().sendCompanionInput(Action.COMMAND);
    }
  }
}
