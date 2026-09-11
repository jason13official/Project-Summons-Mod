package io.github.jason13official.summons.impl.client.gui.screen;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/// Curse of Darkness Innocent Devil status page: level/HP/EXP, evo crystal counts, and the
/// unlocked Command-mode ability list for the active companion.
public class CompanionStatsScreen extends Screen {

  private static final int PANEL_WIDTH = 260;
  private static final int BANNER_HEIGHT = 20;
  private static final int STATS_PANEL_HEIGHT = 62;
  private static final int MAX_VISIBLE_ABILITIES = 5;
  private static final int ROW_HEIGHT = 10;
  private static final int GAP = 4;
  private static final int BUTTON_HEIGHT = 20;
  private static final int BUTTON_GAP = 2;

  private final AbstractCompanion companion;

  /// the whole layout is computed once in #init (top offset included, so the block is
  /// always vertically centered and never runs off a short window) and reused by #render
  private int bannerY;
  private int statsPanelY;
  private int listY;
  private int listHeight;
  private int descY;
  private int descHeight;
  private String selectedDescription = "";

  public CompanionStatsScreen(AbstractCompanion companion) {
    super(Component.literal("Companion"));
    this.companion = companion;
  }

  @Override
  protected void init() {
    int x = this.width / 2 - PANEL_WIDTH / 2;

    List<?> abilities = this.companion.abilities();
    int visible = Math.min(abilities.size(), MAX_VISIBLE_ABILITIES);
    this.listHeight = 18 + Math.max(visible, 1) * ROW_HEIGHT + 4;

    this.selectedDescription = abilities.isEmpty() ? "Level up and evolve to unlock Command-mode abilities."
        : this.companion.getAbilityDescription(this.companion.getAbilityIndex());
    this.descHeight = SummonsScreenStyle.textBoxHeight(this.font, PANEL_WIDTH, this.selectedDescription);

    int totalHeight = BANNER_HEIGHT + GAP + STATS_PANEL_HEIGHT + GAP + this.listHeight + GAP + this.descHeight + GAP
        + (BUTTON_HEIGHT + BUTTON_GAP) * 2;

    this.bannerY = SummonsScreenStyle.centeredTop(this.height, totalHeight);
    this.statsPanelY = this.bannerY + BANNER_HEIGHT + GAP;
    this.listY = this.statsPanelY + STATS_PANEL_HEIGHT + GAP;
    this.descY = this.listY + this.listHeight + GAP;

    int buttonY = this.descY + this.descHeight + GAP;

    this.addRenderableWidget(Button.builder(Component.literal("Evolution Chart"),
            btn -> this.minecraft.setScreen(new EvolutionChartScreen(this.companion)))
        .bounds(x, buttonY, PANEL_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.literal("See this companion's evolution line and current form")))
        .build());

    this.addRenderableWidget(Button.builder(Component.literal("Back"),
            btn -> this.minecraft.setScreen(new SummonsMenuScreen()))
        .bounds(x, buttonY + BUTTON_HEIGHT + BUTTON_GAP, PANEL_WIDTH, BUTTON_HEIGHT).build());
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Screen#render itself draws the blurred/dimmed background before widgets -> call it
    // once, first, then draw our content on top (a second call would blur over our content)
    super.render(graphics, mouseX, mouseY, partialTick);

    int x = this.width / 2 - PANEL_WIDTH / 2;

    SummonsScreenStyle.banner(graphics, this.font, x, this.bannerY, PANEL_WIDTH,
        "Innocent Devil  " + this.companion.getCompanionType().name(), this.companion.getEvolutionForm().displayName());

    SummonsScreenStyle.panel(graphics, x, this.statsPanelY, PANEL_WIDTH, STATS_PANEL_HEIGHT);

    int textX = x + 8;
    int textY = this.statsPanelY + 8;

    graphics.drawString(this.font, "LV " + this.companion.getLevel(), textX, textY, SummonsScreenStyle.TEXT_PRIMARY);
    int xpToNext = AbstractCompanion.experienceToNextLevel(this.companion.getLevel());
    graphics.drawString(this.font, "EXP " + this.companion.getExperience() + "/" + xpToNext,
        textX + 90, textY, SummonsScreenStyle.TEXT_MUTED);

    SummonsScreenStyle.bar(graphics, this.font, textX, textY + 14, PANEL_WIDTH - 16, 12,
        this.companion.getHealth(), this.companion.getMaxHealth(), 0xFF3366CC,
        Math.round(this.companion.getHealth()) + "/" + Math.round(this.companion.getMaxHealth()));

    graphics.drawString(this.font, "Mode: " + this.companion.getMode().name(), textX, textY + 32, SummonsScreenStyle.TEXT_PRIMARY);

    int crystalX = textX;
    for (EvoCrystalColor color : EvoCrystalColor.values()) {
      String label = color.name().charAt(0) + color.name().substring(1).toLowerCase() + " " + this.companion.getCrystalPoints(color);
      graphics.drawString(this.font, label, crystalX, textY + 44, crystalColor(color));
      crystalX += this.font.width(label) + 10;
    }

    SummonsScreenStyle.panel(graphics, x, this.listY, PANEL_WIDTH, this.listHeight);
    graphics.drawString(this.font, "Abilities", x + 8, this.listY + 6, SummonsScreenStyle.TEXT_PRIMARY);

    List<?> abilities = this.companion.abilities();
    int abilityY = this.listY + 18;
    String hoveredDescription = null;

    for (int i = 0; i < abilities.size() && i < MAX_VISIBLE_ABILITIES; i++) {
      boolean selected = i == this.companion.getAbilityIndex();
      int color = selected ? SummonsScreenStyle.TEXT_ACCENT : SummonsScreenStyle.TEXT_MUTED;
      String prefix = selected ? "> " : "  ";
      graphics.drawString(this.font, prefix + this.companion.getAbilityName(i), x + 8, abilityY, color);

      if (mouseX >= x + 8 && mouseX < x + PANEL_WIDTH - 8 && mouseY >= abilityY - 1 && mouseY < abilityY + ROW_HEIGHT - 1) {
        hoveredDescription = this.companion.getAbilityDescription(i);
      }

      abilityY += ROW_HEIGHT;
    }

    if (abilities.isEmpty()) {
      graphics.drawString(this.font, "None unlocked yet", x + 8, abilityY, SummonsScreenStyle.TEXT_MUTED);
    }

    SummonsScreenStyle.textBox(graphics, this.font, x, this.descY, PANEL_WIDTH, this.descHeight, this.selectedDescription, SummonsScreenStyle.TEXT_MUTED);

    if (hoveredDescription != null && !hoveredDescription.isEmpty()) {
      graphics.renderTooltip(this.font, Component.literal(hoveredDescription), mouseX, mouseY);
    }
  }

  private static int crystalColor(EvoCrystalColor color) {
    return switch (color) {
      case RED -> 0xFFFF5555;
      case BLUE -> 0xFF5599FF;
      case GREEN -> 0xFF55CC55;
      case YELLOW -> 0xFFDDDD55;
      case WHITE -> 0xFFEEEEEE;
    };
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
