package io.github.jason13official.summons.impl.client.gui.screen;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/// Curse of Darkness Innocent Devil Chart: every form in this companion's line, grouped by stage; reached forms are named, unreached ones show as "???" same as the in-game chart.
public class EvolutionChartScreen extends Screen {

  private static final int PANEL_WIDTH = 260;
  private static final int BANNER_HEIGHT = 20;
  private static final int STAGE_ROW_HEIGHT = 16;
  private static final int GAP = 4;
  private static final int BUTTON_HEIGHT = 20;

  private final AbstractCompanion companion;

  /// the whole layout is computed once in #init (top offset included, so the block is always vertically centered and never runs off a short window) and reused by #render
  private int bannerY;
  private int chartPanelY;
  private int chartPanelHeight;
  private int flavorY;
  private int flavorHeight;
  private String flavorText = "";

  public EvolutionChartScreen(AbstractCompanion companion) {
    super(Component.literal("Innocent Devil Chart"));
    this.companion = companion;
  }

  @Override
  protected void init() {
    int x = this.width / 2 - PANEL_WIDTH / 2;

    int maxStage = 0;
    for (EvolutionForm form : this.companion.allForms()) {
      maxStage = Math.max(maxStage, form.stage());
    }
    this.chartPanelHeight = (maxStage + 1) * STAGE_ROW_HEIGHT + 10;

    this.flavorText = this.companion.getCompanionType().description();
    this.flavorHeight = SummonsScreenStyle.textBoxHeight(this.font, PANEL_WIDTH, this.flavorText);

    int totalHeight = BANNER_HEIGHT + GAP + this.chartPanelHeight + GAP + this.flavorHeight + GAP + BUTTON_HEIGHT;

    this.bannerY = SummonsScreenStyle.centeredTop(this.height, totalHeight);
    this.chartPanelY = this.bannerY + BANNER_HEIGHT + GAP;
    this.flavorY = this.chartPanelY + this.chartPanelHeight + GAP;

    int buttonY = this.flavorY + this.flavorHeight + GAP;

    this.addRenderableWidget(Button.builder(Component.literal("Back"),
            btn -> this.minecraft.setScreen(new CompanionStatsScreen(this.companion)))
        .bounds(x, buttonY, PANEL_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.literal("Return to the Companion screen")))
        .build());
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Screen#render itself draws the blurred/dimmed background before widgets -> call it
    // once, first, then draw our content on top (a second call would blur over our content)
    super.render(graphics, mouseX, mouseY, partialTick);

    int x = this.width / 2 - PANEL_WIDTH / 2;

    SummonsScreenStyle.banner(graphics, this.font, x, this.bannerY, PANEL_WIDTH, "Innocent Devil Chart",
        this.companion.getCompanionType().name() + " Type");

    EvolutionForm[] forms = this.companion.allForms();
    EvolutionForm current = this.companion.getEvolutionForm();

    int maxStage = 0;
    for (EvolutionForm form : forms) {
      maxStage = Math.max(maxStage, form.stage());
    }

    SummonsScreenStyle.panel(graphics, x, this.chartPanelY, PANEL_WIDTH, this.chartPanelHeight);

    int rowY = this.chartPanelY + 6;
    for (int stage = 0; stage <= maxStage; stage++) {
      int finalStage = stage;
      EvolutionForm[] row = Arrays.stream(forms).filter(f -> f.stage() == finalStage)
          .sorted(Comparator.comparing(EvolutionForm::id)).toArray(EvolutionForm[]::new);
      if (row.length == 0) {
        continue;
      }

      int totalWidth = 0;
      int[] widths = new int[row.length];
      for (int i = 0; i < row.length; i++) {
        widths[i] = this.font.width(this.labelFor(row[i]));
        totalWidth += widths[i] + 16;
      }
      totalWidth -= 16;

      int cellX = this.width / 2 - totalWidth / 2;
      for (int i = 0; i < row.length; i++) {
        EvolutionForm form = row[i];
        boolean reached = this.companion.hasReachedForm(form);
        boolean isCurrent = form.id().equals(current.id());
        int color = isCurrent ? SummonsScreenStyle.TEXT_ACCENT : reached ? SummonsScreenStyle.TEXT_PRIMARY : 0x66666666;

        if (isCurrent) {
          int labelWidth = widths[i];
          graphics.fill(cellX - 3, rowY - 2, cellX + labelWidth + 3, rowY + 10, 0x40FFD24A);
        }

        graphics.drawString(this.font, this.labelFor(form), cellX, rowY, color);
        cellX += widths[i] + 16;
      }

      rowY += STAGE_ROW_HEIGHT;
    }

    SummonsScreenStyle.textBox(graphics, this.font, x, this.flavorY, PANEL_WIDTH, this.flavorHeight, this.flavorText, SummonsScreenStyle.TEXT_MUTED);
  }

  private String labelFor(EvolutionForm form) {
    return this.companion.hasReachedForm(form) ? form.displayName() : "???";
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
