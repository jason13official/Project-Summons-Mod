package io.github.jason13official.summons.impl.client.gui;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionMode;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/// bottom-left mode/ability readout, styled after Curse of Darkness Innocent Devil HUD.
/// placeholder using text and flat colors
public class SummonsHUD {

  public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null || mc.level == null || mc.options.hideGui) {
      return;
    }

    AbstractCompanion active = CompanionPartyManager.findActive(mc.level, mc.player);
    if (active == null) {
      return;
    }

    int x = 6;
    int y = mc.getWindow().getGuiScaledHeight() - 40;

    graphics.drawString(mc.font, active.getCompanionType().name(), x, y - 22, 0xFFFFFF);

    CompanionMode mode = active.getMode();
    int modeColor = mode == CompanionMode.DEFEND ? 0x55AAFF : mode == CompanionMode.COMMAND ? 0xFFFF55 : 0xAAAAAA;
    graphics.drawString(mc.font, mode.name(), x, y - 11, modeColor);

    if (mode == CompanionMode.COMMAND) {
      int abilityCount = Math.max(active.getAbilityCount(), 1);
      for (int i = 0; i < abilityCount; i++) {
        int slotX = x + i * 18;
        int color = i == active.getAbilityIndex() ? 0xFFFFFFFF : 0xFF808080;
        graphics.fill(slotX, y, slotX + 16, y + 16, color);
      }
    }
  }
}
