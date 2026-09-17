package io.github.jason13official.summons.impl.client.gui.screen;

import io.github.jason13official.summons.impl.common.item.DevilShardItem;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import io.github.jason13official.summons.platform.Services;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/// Julia-esque shopkeeper's forge menu (see ShardMerchant): pick a Devil Shard from the player's own inventory, forge it into a new level-1(ish) companion parked at the shop
/// (CompanionShopRoster); not yet placeable into the active party, see that class's own note. Reads the client's own inventory (no C2S needed to populate the list, same as the other three
/// Summons screens); the actual consume+forge round-trips through ForgeShardPayload, re-validated server-side.
public class ShardForgeScreen extends Screen {

  private static final int PANEL_WIDTH = 260;
  private static final int BANNER_HEIGHT = 20;
  private static final int ROW_HEIGHT = 12;
  private static final int MAX_VISIBLE_SHARDS = 6;
  private static final int GAP = 4;
  private static final int BUTTON_HEIGHT = 20;
  private static final int BUTTON_GAP = 2;

  private record ShardRow(int inventorySlot, CompanionType type, int level, int generation) {
  }

  private final List<ShardRow> shards = new ArrayList<>();
  private final List<int[]> rowBounds = new ArrayList<>(); // {y0, y1} per visible row, index-aligned with shards
  private int selectedIndex = -1;

  private int bannerY;
  private int listPanelY;
  private int listPanelHeight;
  private int infoY;
  private int infoHeight;
  private Button forgeButton;

  public ShardForgeScreen() {
    super(Component.literal("Devil Shard Forge"));
  }

  @Override
  protected void init() {
    this.shards.clear();
    if (this.minecraft != null && this.minecraft.player != null) {
      var items = this.minecraft.player.getInventory().items;
      for (int slot = 0; slot < items.size(); slot++) {
        int finalSlot = slot;
        ItemStack stack = items.get(slot);
        DevilShardItem.type(stack).ifPresent(type ->
            this.shards.add(new ShardRow(finalSlot, type, DevilShardItem.parentLevel(stack), DevilShardItem.generation(stack))));
      }
    }

    int x = this.width / 2 - PANEL_WIDTH / 2;

    int visible = Math.max(1, Math.min(this.shards.size(), MAX_VISIBLE_SHARDS));
    this.listPanelHeight = 18 + visible * ROW_HEIGHT + 4;

    String infoText = this.infoTextFor(this.selectedIndex);
    this.infoHeight = SummonsScreenStyle.textBoxHeight(this.font, PANEL_WIDTH, infoText);

    int totalHeight = BANNER_HEIGHT + GAP + this.listPanelHeight + GAP + this.infoHeight + GAP + (BUTTON_HEIGHT + BUTTON_GAP) * 2;

    this.bannerY = SummonsScreenStyle.centeredTop(this.height, totalHeight);
    this.listPanelY = this.bannerY + BANNER_HEIGHT + GAP;
    this.infoY = this.listPanelY + this.listPanelHeight + GAP;

    int buttonY = this.infoY + this.infoHeight + GAP;

    this.forgeButton = this.addRenderableWidget(Button.builder(Component.literal("Forge"), btn -> this.forgeSelected())
        .bounds(x, buttonY, PANEL_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.literal("Consume the selected Devil Shard, forging a new companion parked at the shop")))
        .build());
    this.forgeButton.active = this.selectedIndex >= 0 && this.selectedIndex < this.shards.size();

    this.addRenderableWidget(Button.builder(Component.literal("Close"), btn -> this.onClose())
        .bounds(x, buttonY + BUTTON_HEIGHT + BUTTON_GAP, PANEL_WIDTH, BUTTON_HEIGHT).build());
  }

  private String infoTextFor(int index) {
    if (index < 0 || index >= this.shards.size()) {
      return this.shards.isEmpty() ? "No Devil Shards in your inventory." : "Select a Devil Shard to forge.";
    }

    ShardRow row = this.shards.get(index);
    int seededLevel = Math.max(1, Math.round(row.level() * 0.1F));
    return row.type().name() + "-Type, Gen." + row.generation() + " - forges as a new level-" + seededLevel + " companion.";
  }

  private void forgeSelected() {
    if (this.selectedIndex < 0 || this.selectedIndex >= this.shards.size()) {
      return;
    }

    Services.network().sendForgeShard(this.shards.get(this.selectedIndex).inventorySlot());
    this.onClose();
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // Screen#render itself draws the blurred/dimmed background before widgets -> call it
    // once, first, then draw our content on top (a second call would blur over our content)
    super.render(graphics, mouseX, mouseY, partialTick);

    int x = this.width / 2 - PANEL_WIDTH / 2;

    SummonsScreenStyle.banner(graphics, this.font, x, this.bannerY, PANEL_WIDTH, "Devil Shard Forge", "Julia's Shop");

    SummonsScreenStyle.panel(graphics, x, this.listPanelY, PANEL_WIDTH, this.listPanelHeight);
    graphics.drawString(this.font, "Your Devil Shards (click one to select)", x + 8, this.listPanelY + 6, SummonsScreenStyle.TEXT_PRIMARY);

    this.rowBounds.clear();
    int rowY = this.listPanelY + 18;
    for (int i = 0; i < this.shards.size() && i < MAX_VISIBLE_SHARDS; i++) {
      ShardRow row = this.shards.get(i);
      boolean selected = i == this.selectedIndex;
      boolean hovered = mouseX >= x + 5 && mouseX < x + PANEL_WIDTH - 5 && mouseY >= rowY - 1 && mouseY < rowY + ROW_HEIGHT - 1;

      if (selected) {
        graphics.fill(x + 5, rowY - 1, x + PANEL_WIDTH - 5, rowY + ROW_HEIGHT - 1, 0x4066FF88);
      } else if (hovered) {
        graphics.fill(x + 5, rowY - 1, x + PANEL_WIDTH - 5, rowY + ROW_HEIGHT - 1, 0x40FFFFFF);
      }

      int color = selected ? SummonsScreenStyle.TEXT_ACCENT : SummonsScreenStyle.TEXT_MUTED;
      String prefix = selected ? "> " : "  ";
      graphics.drawString(this.font, prefix + row.type().name() + "-Type (Lv." + row.level() + ", Gen." + row.generation() + ")",
          x + 8, rowY, color);

      this.rowBounds.add(new int[]{rowY - 1, rowY + ROW_HEIGHT - 1});
      rowY += ROW_HEIGHT;
    }

    if (this.shards.isEmpty()) {
      graphics.drawString(this.font, "None; kill something with your companion active.", x + 8, rowY, SummonsScreenStyle.TEXT_MUTED);
    }

    SummonsScreenStyle.textBox(graphics, this.font, x, this.infoY, PANEL_WIDTH, this.infoHeight,
        this.infoTextFor(this.selectedIndex), SummonsScreenStyle.TEXT_MUTED);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0) {
      int x = this.width / 2 - PANEL_WIDTH / 2;
      for (int i = 0; i < this.rowBounds.size(); i++) {
        int[] bounds = this.rowBounds.get(i);
        if (mouseX >= x + 5 && mouseX < x + PANEL_WIDTH - 5 && mouseY >= bounds[0] && mouseY < bounds[1]) {
          this.selectedIndex = i;
          this.forgeButton.active = true;
          return true;
        }
      }
    }

    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
