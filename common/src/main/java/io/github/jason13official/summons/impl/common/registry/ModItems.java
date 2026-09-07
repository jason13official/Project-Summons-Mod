package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.github.jason13official.summons.impl.common.item.EvoCrystalItem;
import io.github.jason13official.summons.platform.Services;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ModItems {

  public static Item FAIRY_SPAWN_EGG;
  public static Item BATTLE_SPAWN_EGG;
  public static Item BIRD_SPAWN_EGG;
  public static Item MAGE_SPAWN_EGG;
  public static Item DEVIL_SPAWN_EGG;
  public static Item PUMPKIN_SPAWN_EGG;

  public static Item EVO_CRYSTAL_RED;
  public static Item EVO_CRYSTAL_BLUE;
  public static Item EVO_CRYSTAL_GREEN;
  public static Item EVO_CRYSTAL_YELLOW;
  public static Item EVO_CRYSTAL_WHITE;

  public static void register(BiConsumer<Item, ResourceLocation> consumer) {

    FAIRY_SPAWN_EGG = Services.PLATFORM.createSpawnEggItem(() -> ModEntities.FAIRY, 0xFFAEE2, 0xFFFFFF, new Properties());
    BATTLE_SPAWN_EGG = Services.PLATFORM.createSpawnEggItem(() -> ModEntities.BATTLE, 0x8C8C8C, 0xB22222, new Properties());
    BIRD_SPAWN_EGG = Services.PLATFORM.createSpawnEggItem(() -> ModEntities.BIRD, 0x4AA8E0, 0xFFFFFF, new Properties());
    MAGE_SPAWN_EGG = Services.PLATFORM.createSpawnEggItem(() -> ModEntities.MAGE, 0x5A2A8C, 0xE0C040, new Properties());
    DEVIL_SPAWN_EGG = Services.PLATFORM.createSpawnEggItem(() -> ModEntities.DEVIL, 0x8B0000, 0x1A1A1A, new Properties());
    PUMPKIN_SPAWN_EGG = Services.PLATFORM.createSpawnEggItem(() -> ModEntities.PUMPKIN, 0xE8790C, 0x3A7D22, new Properties());

    EVO_CRYSTAL_RED = new EvoCrystalItem(EvoCrystalColor.RED, new Properties());
    EVO_CRYSTAL_BLUE = new EvoCrystalItem(EvoCrystalColor.BLUE, new Properties());
    EVO_CRYSTAL_GREEN = new EvoCrystalItem(EvoCrystalColor.GREEN, new Properties());
    EVO_CRYSTAL_YELLOW = new EvoCrystalItem(EvoCrystalColor.YELLOW, new Properties());
    EVO_CRYSTAL_WHITE = new EvoCrystalItem(EvoCrystalColor.WHITE, new Properties());

    consumer.accept(FAIRY_SPAWN_EGG, Summons.identifier("fairy_spawn_egg"));
    consumer.accept(BATTLE_SPAWN_EGG, Summons.identifier("battle_spawn_egg"));
    consumer.accept(BIRD_SPAWN_EGG, Summons.identifier("bird_spawn_egg"));
    consumer.accept(MAGE_SPAWN_EGG, Summons.identifier("mage_spawn_egg"));
    consumer.accept(DEVIL_SPAWN_EGG, Summons.identifier("devil_spawn_egg"));
    consumer.accept(PUMPKIN_SPAWN_EGG, Summons.identifier("pumpkin_spawn_egg"));

    consumer.accept(EVO_CRYSTAL_RED, Summons.identifier("evo_crystal_red"));
    consumer.accept(EVO_CRYSTAL_BLUE, Summons.identifier("evo_crystal_blue"));
    consumer.accept(EVO_CRYSTAL_GREEN, Summons.identifier("evo_crystal_green"));
    consumer.accept(EVO_CRYSTAL_YELLOW, Summons.identifier("evo_crystal_yellow"));
    consumer.accept(EVO_CRYSTAL_WHITE, Summons.identifier("evo_crystal_white"));
  }

  public static Item forColor(EvoCrystalColor color) {
    return switch (color) {
      case RED -> EVO_CRYSTAL_RED;
      case BLUE -> EVO_CRYSTAL_BLUE;
      case GREEN -> EVO_CRYSTAL_GREEN;
      case YELLOW -> EVO_CRYSTAL_YELLOW;
      case WHITE -> EVO_CRYSTAL_WHITE;
    };
  }
}
