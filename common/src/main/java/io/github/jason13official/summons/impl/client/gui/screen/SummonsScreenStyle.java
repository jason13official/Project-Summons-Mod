package io.github.jason13official.summons.impl.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

/// Shared chrome for the Summons screens, built entirely from vanilla textures/APIs so the screens read as part of the game rather than a mod overlay: the same tiled panel background + separators the
/// in-world pause menu list uses, and real hover [Tooltip]s.
final class SummonsScreenStyle {

  static final int BANNER_LEFT = 0xFFCC6600;
  static final int BANNER_RIGHT = 0xFF991A1A;
  static final int TEXT_PRIMARY = 0xFFF0F0F0;
  static final int TEXT_MUTED = 0xFFA0A0B0;
  static final int TEXT_ACCENT = 0xFFFFD24A;
  /// same texture AbstractSelectionList uses for in-world (pause menu) list panels
  private static final ResourceLocation PANEL_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

  private SummonsScreenStyle() {
  }

  /// top offset for a `totalHeight`-tall content block: centered when it fits, otherwise pinned so its bottom stays an 8px margin above the screen edge (never past it) rather than centering blindly
  /// and pushing the bottom row of buttons off-screen
  static int centeredTop(int screenHeight, int totalHeight) {
    int ideal = (screenHeight - totalHeight) / 2;
    int maxTop = screenHeight - totalHeight - 8;
    return maxTop < 8 ? 8 : Math.max(8, Math.min(ideal, maxTop));
  }

  /// the orange->red gradient bar with a title, sitting above a name subtitle line
  static void banner(GuiGraphics graphics, Font font, int x, int y, int width, String title, String subtitle) {
    graphics.fillGradient(x, y, x + width, y + 20, BANNER_LEFT, BANNER_RIGHT);
    graphics.blit(Screen.INWORLD_FOOTER_SEPARATOR, x, y + 19, 0.0F, 0.0F, width, 2, 32, 2);
    graphics.drawString(font, title, x + 8, y + 6, TEXT_PRIMARY);

    if (subtitle != null && !subtitle.isEmpty()) {
      graphics.drawString(font, subtitle, x + width - 8 - font.width(subtitle), y + 6, TEXT_PRIMARY);
    }
  }

  /// the vanilla in-world list panel: tiled noise texture between a light header separator and a dark footer separator, same as the pause menu's button list
  static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
    RenderSystem.enableBlend();
    graphics.blit(PANEL_BACKGROUND, x, y, 0.0F, 0.0F, width, height, 32, 32);
    graphics.blit(Screen.INWORLD_HEADER_SEPARATOR, x, y - 2, 0.0F, 0.0F, width, 2, 32, 2);
    graphics.blit(Screen.INWORLD_FOOTER_SEPARATOR, x, y + height, 0.0F, 0.0F, width, 2, 32, 2);
    RenderSystem.disableBlend();
  }

  /// a labeled, colored gauge (HP/EXP-style); `current/max` text centered over the fill
  static void bar(GuiGraphics graphics, Font font, int x, int y, int width, int height,
      float current, float max, int fillColor, String centerText) {
    graphics.fill(x, y, x + width, y + height, 0xFF000000);

    float fraction = max > 0.0F ? Math.max(0.0F, Math.min(1.0F, current / max)) : 0.0F;
    int filledWidth = Math.round((width - 2) * fraction);
    if (filledWidth > 0) {
      graphics.fill(x + 1, y + 1, x + 1 + filledWidth, y + height - 1, fillColor);
    }

    graphics.drawCenteredString(font, centerText, x + width / 2, y + (height - 8) / 2, TEXT_PRIMARY);
  }

  /// wraps `text` to `width` and returns the panel height [#textBox] would use, so callers can lay out whatever comes after it without duplicating the wrap math
  static int textBoxHeight(Font font, int width, String text) {
    List<FormattedCharSequence> lines = font.split(Component.literal(text), width - 12);
    return Math.max(20, lines.size() * 10 + 10);
  }

  /// word-wrapped flavor/description text inside a panel; height must come from [#textBoxHeight] (call it during layout, not here, so button positions can't drift)
  static void textBox(GuiGraphics graphics, Font font, int x, int y, int width, int height, String text, int color) {
    panel(graphics, x, y, width, height);

    List<FormattedCharSequence> lines = font.split(Component.literal(text), width - 12);
    int lineY = y + 6;
    for (FormattedCharSequence line : lines) {
      graphics.drawString(font, line, x + 6, lineY, color);
      lineY += 10;
    }
  }
}
