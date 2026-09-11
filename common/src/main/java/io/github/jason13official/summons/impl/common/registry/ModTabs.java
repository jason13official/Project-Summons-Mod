package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Constants;
import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.platform.Services;
import java.util.function.BiConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModTabs {

  public static CreativeModeTab SUMMONS;

  public static void register(BiConsumer<CreativeModeTab, ResourceLocation> consumer) {

    SUMMONS = Services.PLATFORM.tabBuilder()
        .icon(() -> new ItemStack(ModItems.FAIRY_SPAWN_EGG))
        .title(Component.translatable("itemGroup.summons"))
        .displayItems((itemDisplayParameters, output) -> {
          output.accept(ModItems.FAIRY_SPAWN_EGG);
          output.accept(ModItems.BATTLE_SPAWN_EGG);
          output.accept(ModItems.BIRD_SPAWN_EGG);
          output.accept(ModItems.MAGE_SPAWN_EGG);
          output.accept(ModItems.DEVIL_SPAWN_EGG);
          output.accept(ModItems.PUMPKIN_SPAWN_EGG);
          output.accept(ModItems.EVO_CRYSTAL_RED);
          output.accept(ModItems.EVO_CRYSTAL_BLUE);
          output.accept(ModItems.EVO_CRYSTAL_GREEN);
          output.accept(ModItems.EVO_CRYSTAL_YELLOW);
          output.accept(ModItems.EVO_CRYSTAL_WHITE);
          output.accept(ModItems.HEART);
          output.accept(ModItems.SUMMON_GATE);
          output.accept(ModItems.DECK_BRUSH);
          output.accept(ModItems.BAMBOO_LANCE);
          output.accept(ModItems.DUNG);
          output.accept(ModItems.CHAUVE_SOURIS);
        }).build();

    consumer.accept(SUMMONS, Summons.identifier(Constants.MOD_ID));
  }
}
