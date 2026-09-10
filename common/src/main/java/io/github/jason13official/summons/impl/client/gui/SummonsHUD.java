package io.github.jason13official.summons.impl.client.gui;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionMode;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/// top-left HP gauges + bottom-left mode/ability readout, styled after Curse of Darkness'
/// Innocent Devil HUD. placeholder using text and flat colors
public class SummonsHUD {

  private static final int BAR_WIDTH = 140;
  private static final int BAR_HEIGHT = 10;
  private static final int BAR_GAP = 20;

  public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null || mc.level == null || mc.options.hideGui) {
      return;
    }

    renderHealthBar(graphics, mc.font, 6, 10, "Player", mc.player.getHealth(), mc.player.getMaxHealth(), 0xFFCC3333);

    AbstractCompanion active = CompanionPartyManager.findActive(mc.level, mc.player);
    if (active == null) {
      return;
    }

    String companionLabel = active.isWisp() ? active.getCompanionType().name() + " (wisp)" : active.getCompanionType().name();
    int companionColor = active.isWisp() ? 0xFF777777 : 0xFF3366CC;
    renderHealthBar(graphics, mc.font, 6, 10 + BAR_GAP, companionLabel, active.getHealth(), active.getMaxHealth(), companionColor);

    if (active.isChainArmed()) {
      renderChainPopup(graphics, mc.font, mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2 - 30);
    }

    int x = 6;
    int y = mc.getWindow().getGuiScaledHeight() - 40;

    int xpToNext = AbstractCompanion.experienceToNextLevel(active.getLevel());
    String header = active.getCompanionType().name() + "  Lv." + active.getLevel()
        + " (" + active.getExperience() + "/" + xpToNext + ")";
    graphics.drawString(mc.font, header, x, y - 22, 0xFFFFFF);

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

      if (active.getAbilityCount() > 0) {
        String label = active.getAbilityName(active.getAbilityIndex());
        boolean busy = active.isAbilityBusy();
        graphics.drawString(mc.font, busy ? label + " (busy)" : label, x, y - 33, busy ? 0xFF5555 : 0xFFFFFF);
      }
    }
  }

  /// flat-color HP gauge: label above, a dark backing bar, a colored fill scaled to
  /// current/max, and the "current/max" text over it
  private static void renderHealthBar(GuiGraphics graphics, Font font, int x, int y, String label,
                                       float current, float max, int fillColor) {
    graphics.drawString(font, label, x, y - 9, 0xFFFFFF);

    graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF202020);

    float fraction = max > 0.0F ? Math.max(0.0F, Math.min(1.0F, current / max)) : 0.0F;
    int filledWidth = Math.round(BAR_WIDTH * fraction);
    if (filledWidth > 0) {
      graphics.fill(x, y, x + filledWidth, y + BAR_HEIGHT, fillColor);
    }

    String text = Math.round(current) + "/" + Math.round(max);
    graphics.drawCenteredString(font, text, x + BAR_WIDTH / 2, y + 1, 0xFFFFFF);
  }

  /// "the word 'Chain!' is flashed in an orange bubble" (wiki) -> small centered popup
  /// while the active companion's Chain Attack window is open
  private static void renderChainPopup(GuiGraphics graphics, Font font, int centerX, int centerY) {
    String text = "Chain!";
    int halfWidth = font.width(text) / 2;
    graphics.fill(centerX - halfWidth - 6, centerY - 6, centerX + halfWidth + 6, centerY + 10, 0xAA331A00);
    graphics.drawCenteredString(font, text, centerX, centerY, 0xFFA500);
  }

  private SummonsHUD() {
  }
}
