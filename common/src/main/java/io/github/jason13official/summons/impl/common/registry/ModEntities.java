package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.entity.CompanionCube;
import io.github.jason13official.summons.impl.common.entity.CompanionPrism;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

  public static EntityType<CompanionCube> CUBE;
  public static EntityType<CompanionPrism> PRISM;

  public static void register(BiConsumer<EntityType<?>, ResourceLocation> consumer) {

    CUBE = EntityType.Builder.<CompanionCube>of(CompanionCube::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(32).build("summons:cube");
    consumer.accept(CUBE, Summons.identifier("cube"));

    PRISM = EntityType.Builder.<CompanionPrism>of(CompanionPrism::new, MobCategory.MISC).sized(0.5f, 1.0f).clientTrackingRange(32).build("summons:prism");
    consumer.accept(PRISM, Summons.identifier("prism"));
  }
}
