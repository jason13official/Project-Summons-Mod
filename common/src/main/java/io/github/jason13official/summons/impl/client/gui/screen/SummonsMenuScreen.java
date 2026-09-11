package io.github.jason13official.summons.impl.client.gui.screen;

import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionPartyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

/// Mod hub screen (Curse of Darkness "Player" status page): player stats + this run's owner
/// bonuses from the active companion, with buttons into the companion-specific screens.
public class SummonsMenuScreen extends Screen {

  public SummonsMenuScreen() {
    super(Component.literal("Summons"));
  }

  @Override
  protected void init() {
    int centerX = this.width / 2;
    int y = this.height / 2 + 10;

    AbstractCompanion active = this.activeCompanion();

    this.addRenderableWidget(Button.builder(Component.literal("Companion"), btn -> {
          if (active != null) {
            this.minecraft.setScreen(new CompanionStatsScreen(active));
          }
        }).bounds(centerX - 100, y, 200, 20).build())
        .active = active != null;

    this.addRenderableWidget(Button.builder(Component.literal("Evolution Chart"), btn -> {
          if (active != null) {
            this.minecraft.setScreen(new EvolutionChartScreen(active));
          }
        }).bounds(centerX - 100, y + 24, 200, 20).build())
        .active = active != null;

    this.addRenderableWidget(Button.builder(Component.literal("Close"), btn -> this.onClose())
        .bounds(centerX - 100, y + 48, 200, 20).build());
  }

  @Nullable
  private AbstractCompanion activeCompanion() {
    return this.minecraft.player == null ? null
        : CompanionPartyManager.findActive(this.minecraft.level, this.minecraft.player);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);

    graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

    if (this.minecraft.player == null) {
      return;
    }

    int x = this.width / 2 - 100;
    int y = 40;

    graphics.drawString(this.font, "LV " + this.minecraft.player.experienceLevel, x, y, 0xFFFFFF);
    graphics.drawString(this.font, "HP " + Math.round(this.minecraft.player.getHealth())
        + "/" + Math.round(this.minecraft.player.getMaxHealth()), x, y + 12, 0xFFFFFF);
    graphics.drawString(this.font, "ATK " + Math.round(this.minecraft.player.getAttributeValue(Attributes.ATTACK_DAMAGE)),
        x, y + 24, 0xFFFFFF);
    graphics.drawString(this.font, "DEF " + Math.round(this.minecraft.player.getAttributeValue(Attributes.ARMOR)),
        x, y + 36, 0xFFFFFF);

    AbstractCompanion active = this.activeCompanion();
    int level = active == null ? 1 : active.getLevel();
    double str = active == null ? 0 : active.ownerStatBonus().str(level);
    double con = active == null ? 0 : active.ownerStatBonus().con(level);
    double lck = active == null ? 0 : active.ownerStatBonus().lck(level);

    graphics.drawString(this.font, String.format("+STR %.0f  +CON %.0f  +LCK %.0f", str, con, lck), x, y + 52, 0xAAAAFF);

    graphics.drawString(this.font, active == null ? "No companion summoned"
        : active.getCompanionType().name() + " -> " + active.getEvolutionForm().displayName(), x, y + 68, 0xCCCCCC);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
