package io.github.jason13official.summons.impl.common.registry;

import io.github.jason13official.summons.Summons;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.entity.flying.BirdSummon;
import io.github.jason13official.summons.impl.common.entity.flying.DevilSummon;
import io.github.jason13official.summons.impl.common.entity.flying.FairySummon;
import io.github.jason13official.summons.impl.common.entity.flying.FlyingCompanionCube;
import io.github.jason13official.summons.impl.common.entity.flying.FlyingCompanionPrism;
import io.github.jason13official.summons.impl.common.entity.flying.MageSummon;
import io.github.jason13official.summons.impl.common.entity.ground.BattleSummon;
import io.github.jason13official.summons.impl.common.entity.ground.CompanionCube;
import io.github.jason13official.summons.impl.common.entity.ground.CompanionPrism;
import io.github.jason13official.summons.impl.common.entity.ground.PumpkinSummon;
import io.github.jason13official.summons.impl.common.party.CompanionType;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

  public static EntityType<CompanionCube> CUBE;
  public static EntityType<CompanionPrism> PRISM;

  public static EntityType<FlyingCompanionCube> FLYING_CUBE;
  public static EntityType<FlyingCompanionPrism> FLYING_PRISM;

  public static EntityType<FairySummon> FAIRY;
  public static EntityType<BattleSummon> BATTLE;
  public static EntityType<BirdSummon> BIRD;
  public static EntityType<MageSummon> MAGE;
  public static EntityType<DevilSummon> DEVIL;
  public static EntityType<PumpkinSummon> PUMPKIN;

  public static void register(BiConsumer<EntityType<?>, ResourceLocation> consumer) {

    registerGround(consumer);
    registerFlying(consumer);
  }

  public static void registerGround(BiConsumer<EntityType<?>, ResourceLocation> consumer) {

    CUBE = EntityType.Builder.of(CompanionCube::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(32).build("summons:cube");
    consumer.accept(CUBE, Summons.identifier("cube"));

    PRISM = EntityType.Builder.of(CompanionPrism::new, MobCategory.MISC).sized(0.5f, 1.0f).clientTrackingRange(32).build("summons:prism");
    consumer.accept(PRISM, Summons.identifier("prism"));

    // EntityType.class IRON_GOLEM sized
    BATTLE = EntityType.Builder.of(BattleSummon::new, MobCategory.MISC).sized(1.4F, 2.7F).clientTrackingRange(32).build("summons:battle");
    consumer.accept(BATTLE, Summons.identifier("battle"));

    // EntityType.class PUMPKIN_GOLEM sized
    PUMPKIN = EntityType.Builder.of(PumpkinSummon::new, MobCategory.MISC).sized(0.7F, 1.9F).clientTrackingRange(32).build("summons:pumpkin");
    consumer.accept(PUMPKIN, Summons.identifier("pumpkin"));
  }

  public static void registerFlying(BiConsumer<EntityType<?>, ResourceLocation> consumer) {

    FLYING_CUBE = EntityType.Builder.of(FlyingCompanionCube::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(32).build("summons:flying_cube");
    consumer.accept(FLYING_CUBE, Summons.identifier("flying_cube"));

    FLYING_PRISM = EntityType.Builder.of(FlyingCompanionPrism::new, MobCategory.MISC).sized(0.5f, 1.0f).clientTrackingRange(32).build("summons:flying_prism");
    consumer.accept(FLYING_PRISM, Summons.identifier("flying_prism"));

    // EntityType.class BAT sized
    FAIRY = EntityType.Builder.of(FairySummon::new, MobCategory.MISC).sized(0.5F, 0.9F).clientTrackingRange(32).build("summons:fairy");
    consumer.accept(FAIRY, Summons.identifier("fairy"));

    // EntityType.class PARROT sized
    BIRD = EntityType.Builder.of(BirdSummon::new, MobCategory.MISC).sized(0.5F, 0.9F).clientTrackingRange(32).build("summons:bird");
    consumer.accept(BIRD, Summons.identifier("bird"));

    // EntityType.class ALLAY sized
    MAGE = EntityType.Builder.of(MageSummon::new, MobCategory.MISC).sized(0.35F, 0.6F).clientTrackingRange(32).build("summons:mage");
    consumer.accept(MAGE, Summons.identifier("mage"));

    // EntityType.class ENDERMAN sized
    DEVIL = EntityType.Builder.of(DevilSummon::new, MobCategory.MISC).sized(0.6F, 2.9F).clientTrackingRange(32).build("summons:devil");
    consumer.accept(DEVIL, Summons.identifier("devil"));
  }

  /// map companion type to raw entity type for party system
  public static EntityType<? extends AbstractCompanion> forType(CompanionType type) {
    return switch (type) {
      case FAIRY -> FAIRY;
      case BATTLE -> BATTLE;
      case BIRD -> BIRD;
      case MAGE -> MAGE;
      case DEVIL -> DEVIL;
      case PUMPKIN -> PUMPKIN;
    };
  }
}
