package io.github.jason13official.summons.impl.client;

import io.github.jason13official.summons.impl.client.gui.screen.SummonsMenuScreen;
import io.github.jason13official.summons.impl.common.network.SummonsNetworking.Action;
import io.github.jason13official.summons.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/// Mirrors Curse of Darkness Innocent Devil controls: UP/DOWN cycles Auto/Command/Defend, LEFT/RIGHT cycles the selected ability, COMMAND activates it, TOGGLE_SUMMON/CYCLE_PARTY dismiss/switch the
/// active companion.
public class SummonsKeyBindings {

  private static final String CATEGORY = "key.categories.summons";

  public static final KeyMapping TOGGLE_SUMMON = new KeyMapping("key.summons.toggle_summon", GLFW.GLFW_KEY_G, CATEGORY);
  public static final KeyMapping CYCLE_PARTY = new KeyMapping("key.summons.cycle_party", GLFW.GLFW_KEY_H, CATEGORY);
  public static final KeyMapping MODE_UP = new KeyMapping("key.summons.mode_up", GLFW.GLFW_KEY_UP, CATEGORY);
  public static final KeyMapping MODE_DOWN = new KeyMapping("key.summons.mode_down", GLFW.GLFW_KEY_DOWN, CATEGORY);
  public static final KeyMapping ABILITY_LEFT = new KeyMapping("key.summons.ability_left", GLFW.GLFW_KEY_LEFT, CATEGORY);
  public static final KeyMapping ABILITY_RIGHT = new KeyMapping("key.summons.ability_right", GLFW.GLFW_KEY_RIGHT, CATEGORY);
  public static final KeyMapping COMMAND = new KeyMapping("key.summons.command", GLFW.GLFW_KEY_J, CATEGORY);
  public static final KeyMapping ABILITY_SCROLL_MODIFIER = new KeyMapping("key.summons.ability_scroll_modifier", GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
  public static final KeyMapping OPEN_MENU = new KeyMapping("key.summons.open_menu", GLFW.GLFW_KEY_N, CATEGORY);

  public static KeyMapping[] all() {

    return new KeyMapping[]{
        TOGGLE_SUMMON,
        CYCLE_PARTY,
        MODE_UP,
        MODE_DOWN,
        ABILITY_LEFT,
        ABILITY_RIGHT,
        COMMAND,
        ABILITY_SCROLL_MODIFIER,
        OPEN_MENU
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
    while (OPEN_MENU.consumeClick()) {
      if (Minecraft.getInstance().screen == null) {
        Minecraft.getInstance().setScreen(new SummonsMenuScreen());
      }
    }
  }
}
