package io.github.jason13official.summons;

import io.github.jason13official.monolib.MonoLib;
import io.github.jason13official.monolib.impl.common.sailing.Sailing;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.flying.FlyingCompanion;
import io.github.jason13official.summons.impl.common.registry.ModEntities;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class Summons {

  public static void init() {

    Sailing.register(Constants.MOD_ID, MonoLib.createFilename(Constants.MOD_ID, "1.21.1", "0.3.0"));
  }

  public static ResourceLocation identifier(final String path) {
    return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
  }

  public static void createDefaultAttributes(BiConsumer<EntityType, AttributeSupplier> consumer) {

    consumer.accept(ModEntities.CUBE, AbstractCompanion.createAttributes().build());
    consumer.accept(ModEntities.PRISM, AbstractCompanion.createAttributes().build());

    consumer.accept(ModEntities.FLYING_CUBE, FlyingCompanion.createAttributes().build());
    consumer.accept(ModEntities.FLYING_PRISM, FlyingCompanion.createAttributes().build());
  }
}