package io.github.jason13official.summons.impl.client.model.anim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class PumpkinSummonAnimations {

  public static final AnimationDefinition MOVE = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("piece1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  public static final AnimationDefinition SHEARED = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("pumpkin", new AnimationChannel(AnimationChannel.Targets.SCALE,
          new Keyframe(0.0F, KeyframeAnimations.scaleVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("piece1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  /// little punches -> Pumpkin's basic direct attack
  public static final AnimationDefinition ATTACK = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  /// "Pose" -> Pumpkin's only Command-mode ability, per the wiki
  public static final AnimationDefinition POSE = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("piece1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, 20.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-60.0F, 0.0F, -20.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();
}