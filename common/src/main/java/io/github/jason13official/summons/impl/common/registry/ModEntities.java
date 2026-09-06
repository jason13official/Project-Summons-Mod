package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.entity.flying.FlyingCompanionCube;
import io.github.jason13official.summons.impl.common.entity.flying.FlyingCompanionPrism;
import io.github.jason13official.summons.impl.common.entity.ground.BattleSummon;
import io.github.jason13official.summons.impl.common.entity.ground.CompanionCube;
import io.github.jason13official.summons.impl.common.entity.ground.CompanionPrism;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

  public static EntityType<CompanionCube> CUBE;
  public static EntityType<CompanionPrism> PRISM;

  public static EntityType<FlyingCompanionCube> FLYING_CUBE;
  public static EntityType<FlyingCompanionPrism> FLYING_PRISM;

  public static EntityType<BattleSummon> BATTLE;

  public static void register(BiConsumer<EntityType<?>, ResourceLocation> consumer) {

    registerGround(consumer);
    registerFlying(consumer);
  }

  public static void registerGround(BiConsumer<EntityType<?>, ResourceLocation> consumer) {

    CUBE = EntityType.Builder.<CompanionCube>of(CompanionCube::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(32).build("summons:cube");
    consumer.accept(CUBE, Summons.identifier("cube"));

    PRISM = EntityType.Builder.<CompanionPrism>of(CompanionPrism::new, MobCategory.MISC).sized(0.5f, 1.0f).clientTrackingRange(32).build("summons:prism");
    consumer.accept(PRISM, Summons.identifier("prism"));

    // EntityType.class IRON_GOLEM sized
    BATTLE = EntityType.Builder.<BattleSummon>of(BattleSummon::new, MobCategory.MISC).sized(1.4F, 2.7F).clientTrackingRange(32).build("summons:battle");
    consumer.accept(BATTLE, Summons.identifier("battle"));
  }

  public static void registerFlying(BiConsumer<EntityType<?>, ResourceLocation> consumer) {

    FLYING_CUBE = EntityType.Builder.<FlyingCompanionCube>of(FlyingCompanionCube::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(32).build("summons:flying_cube");
    consumer.accept(FLYING_CUBE, Summons.identifier("flying_cube"));

    FLYING_PRISM = EntityType.Builder.<FlyingCompanionPrism>of(FlyingCompanionPrism::new, MobCategory.MISC).sized(0.5f, 1.0f).clientTrackingRange(32).build("summons:flying_prism");
    consumer.accept(FLYING_PRISM, Summons.identifier("flying_prism"));
  }
}
