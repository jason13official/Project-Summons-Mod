package io.github.jason13official.summons.impl.client.gui.screen;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvolutionForm;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/// Curse of Darkness Innocent Devil Chart: every form in this companion's line, grouped by
/// stage; reached forms are named, unreached ones show as "???" same as the in-game chart.
public class EvolutionChartScreen extends Screen {

  private final AbstractCompanion companion;

  public EvolutionChartScreen(AbstractCompanion companion) {
    super(Component.literal("Innocent Devil Chart"));
    this.companion = companion;
  }

  @Override
  protected void init() {
    this.addRenderableWidget(Button.builder(Component.literal("Back"),
            btn -> this.minecraft.setScreen(new CompanionStatsScreen(this.companion)))
        .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);

    graphics.drawCenteredString(this.font, this.companion.getCompanionType().name() + " Type", this.width / 2, 20, 0xFFFFFF);

    EvolutionForm[] forms = this.companion.allForms();
    EvolutionForm current = this.companion.getEvolutionForm();

    int maxStage = 0;
    for (EvolutionForm form : forms) {
      maxStage = Math.max(maxStage, form.stage());
    }

    int y = 44;
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

      int x = this.width / 2 - totalWidth / 2;
      for (int i = 0; i < row.length; i++) {
        EvolutionForm form = row[i];
        boolean reached = this.companion.hasReachedForm(form);
        boolean isCurrent = form.id().equals(current.id());
        int color = isCurrent ? 0xFFFF55 : reached ? 0xFFFFFF : 0x666666;
        graphics.drawString(this.font, this.labelFor(form), x, y, color);
        x += widths[i] + 16;
      }

      y += 16;
    }
  }

  private String labelFor(EvolutionForm form) {
    return this.companion.hasReachedForm(form) ? form.displayName() : "???";
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
