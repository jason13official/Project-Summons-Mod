package io.github.jason13official.summons.impl.client.gui.screen;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

/// Mod hub screen (Curse of Darkness "Player" status page): player stats + this run's owner bonuses from the active companion, with buttons into the companion-specific screens.
public class SummonsMenuScreen extends Screen {

  private static final int PANEL_WIDTH = 240;
  private static final int BANNER_HEIGHT = 20;
  private static final int STATS_PANEL_HEIGHT = 70;
  private static final int GAP = 4;
  private static final int BUTTON_HEIGHT = 20;
  private static final int BUTTON_GAP = 2;

  /// the whole layout is computed once in #init (top offset included, so the block is always vertically centered and never runs off a short window) and reused by #render
  private int bannerY;
  private int statsPanelY;
  private int flavorY;
  private int flavorHeight;
  private String flavorText = "";

  public SummonsMenuScreen() {
    super(Component.literal("Summons"));
  }

  @Override
  protected void init() {
    int x = this.width / 2 - PANEL_WIDTH / 2;
    AbstractCompanion active = this.activeCompanion();

    this.flavorText = active == null
        ? "Summon an Innocent Devil to fight alongside you, boost your stats, and unlock new abilities."
        : active.getCompanionType().description();
    this.flavorHeight = SummonsScreenStyle.textBoxHeight(this.font, PANEL_WIDTH, this.flavorText);

    int totalHeight = BANNER_HEIGHT + GAP + STATS_PANEL_HEIGHT + GAP + this.flavorHeight + GAP
        + (BUTTON_HEIGHT + BUTTON_GAP) * 3;

    this.bannerY = SummonsScreenStyle.centeredTop(this.height, totalHeight);
    this.statsPanelY = this.bannerY + BANNER_HEIGHT + GAP;
    this.flavorY = this.statsPanelY + STATS_PANEL_HEIGHT + GAP;

    int y = this.flavorY + this.flavorHeight + GAP;

    this.addRenderableWidget(Button.builder(Component.literal("Companion"), btn -> {
          if (active != null) {
            this.minecraft.setScreen(new CompanionStatsScreen(active));
          }
        }).bounds(x, y, PANEL_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.literal("View this companion's level, HP, evo crystals, and abilities")))
        .build())
        .active = active != null;

    this.addRenderableWidget(Button.builder(Component.literal("Evolution Chart"), btn -> {
          if (active != null) {
            this.minecraft.setScreen(new EvolutionChartScreen(active));
          }
        }).bounds(x, y + BUTTON_HEIGHT + BUTTON_GAP, PANEL_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.literal("See this companion's evolution line and current form")))
        .build())
        .active = active != null;

    this.addRenderableWidget(Button.builder(Component.literal("Close"), btn -> this.onClose())
        .bounds(x, y + (BUTTON_HEIGHT + BUTTON_GAP) * 2, PANEL_WIDTH, BUTTON_HEIGHT).build());
  }

  @Nullable
  private AbstractCompanion activeCompanion() {
    return this.minecraft.player == null ? null
        : CompanionPartyManager.findActive(this.minecraft.level, this.minecraft.player);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Screen#render itself draws the blurred/dimmed background before widgets -> call it
    // once, first, then draw our content on top (a second call would blur over our content)
    super.render(graphics, mouseX, mouseY, partialTick);

    int x = this.width / 2 - PANEL_WIDTH / 2;

    SummonsScreenStyle.banner(graphics, this.font, x, this.bannerY, PANEL_WIDTH, "Summons", null);

    if (this.minecraft.player == null) {
      return;
    }

    SummonsScreenStyle.panel(graphics, x, this.statsPanelY, PANEL_WIDTH, STATS_PANEL_HEIGHT);

    int textX = x + 8;
    int textY = this.statsPanelY + 8;

    graphics.drawString(this.font, "LV " + this.minecraft.player.experienceLevel, textX, textY, SummonsScreenStyle.TEXT_PRIMARY);
    graphics.drawString(this.font, "ATK " + Math.round(this.minecraft.player.getAttributeValue(Attributes.ATTACK_DAMAGE)),
        textX + 90, textY, SummonsScreenStyle.TEXT_PRIMARY);
    graphics.drawString(this.font, "DEF " + Math.round(this.minecraft.player.getAttributeValue(Attributes.ARMOR)),
        textX + 160, textY, SummonsScreenStyle.TEXT_PRIMARY);

    SummonsScreenStyle.bar(graphics, this.font, textX, textY + 14, PANEL_WIDTH - 16, 12,
        this.minecraft.player.getHealth(), this.minecraft.player.getMaxHealth(), 0xFFCC3333,
        Math.round(this.minecraft.player.getHealth()) + "/" + Math.round(this.minecraft.player.getMaxHealth()));

    AbstractCompanion active = this.activeCompanion();
    int level = active == null ? 1 : active.getLevel();
    double str = active == null ? 0 : active.ownerStatBonus().str(level);
    double con = active == null ? 0 : active.ownerStatBonus().con(level);
    double lck = active == null ? 0 : active.ownerStatBonus().lck(level);

    graphics.drawString(this.font, String.format("+STR %.0f   +CON %.0f   +LCK %.0f", str, con, lck),
        textX, textY + 32, SummonsScreenStyle.TEXT_ACCENT);

    String companionLine = active == null ? "No companion summoned"
        : active.getCompanionType().name() + " -> " + active.getEvolutionForm().displayName();
    graphics.drawString(this.font, companionLine, textX, textY + 48, SummonsScreenStyle.TEXT_MUTED);

    SummonsScreenStyle.textBox(graphics, this.font, x, this.flavorY, PANEL_WIDTH, this.flavorHeight, this.flavorText, SummonsScreenStyle.TEXT_MUTED);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
