package io.github.jason13official.summons.impl.client.gui.screen;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/// Curse of Darkness Innocent Devil status page: level/HP/EXP, evo crystal counts, and the
/// unlocked Command-mode ability list for the active companion.
public class CompanionStatsScreen extends Screen {

  private final AbstractCompanion companion;

  public CompanionStatsScreen(AbstractCompanion companion) {
    super(Component.literal("Companion"));
    this.companion = companion;
  }

  @Override
  protected void init() {
    this.addRenderableWidget(Button.builder(Component.literal("Evolution Chart"),
            btn -> this.minecraft.setScreen(new EvolutionChartScreen(this.companion)))
        .bounds(this.width / 2 - 100, this.height - 52, 200, 20).build());

    this.addRenderableWidget(Button.builder(Component.literal("Back"),
            btn -> this.minecraft.setScreen(new SummonsMenuScreen()))
        .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);

    int x = this.width / 2 - 120;
    int y = 24;

    graphics.drawCenteredString(this.font, this.companion.getCompanionType().name()
        + "  " + this.companion.getEvolutionForm().displayName(), this.width / 2, y, 0xFFFFFF);

    y += 20;
    graphics.drawString(this.font, "LV " + this.companion.getLevel(), x, y, 0xFFFFFF);
    graphics.drawString(this.font, "HP " + Math.round(this.companion.getHealth())
        + "/" + Math.round(this.companion.getMaxHealth()), x + 80, y, 0xFFFFFF);

    y += 12;
    int xpToNext = AbstractCompanion.experienceToNextLevel(this.companion.getLevel());
    graphics.drawString(this.font, "EXP " + this.companion.getExperience() + "/" + xpToNext, x, y, 0xCCCCCC);

    y += 20;
    graphics.drawString(this.font, "Evo Crystals", x, y, 0xFFFFFF);
    y += 12;
    for (EvoCrystalColor color : EvoCrystalColor.values()) {
      graphics.drawString(this.font, color.name() + " " + this.companion.getCrystalPoints(color), x, y, 0xAAAAAA);
      y += 11;
    }

    y += 8;
    graphics.drawString(this.font, "Mode: " + this.companion.getMode().name(), x, y, 0xFFFFFF);

    y += 16;
    graphics.drawString(this.font, "Abilities", x, y, 0xFFFFFF);
    y += 12;

    List<?> abilities = this.companion.abilities();
    for (int i = 0; i < abilities.size(); i++) {
      int color = i == this.companion.getAbilityIndex() ? 0xFFFF55 : 0xCCCCCC;
      graphics.drawString(this.font, this.companion.getAbilityName(i), x, y, color);
      y += 11;
    }

    if (abilities.isEmpty()) {
      graphics.drawString(this.font, "None unlocked yet", x, y, 0x888888);
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
