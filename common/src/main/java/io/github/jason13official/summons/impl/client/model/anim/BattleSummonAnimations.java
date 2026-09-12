package io.github.jason13official.summons.impl.client.model.anim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class BattleSummonAnimations {

  public static final AnimationDefinition ATTACK = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("arm0", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-56.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-56.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  /// DEFEND pose: braced/blocking stance -> arms raised and crossed in front, knees bent, slight forward lean
  public static final AnimationDefinition GUARD = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(8.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm0", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 25.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, -25.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("leg0", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("leg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  public static final AnimationDefinition MOVE = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("arm0", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  public static final AnimationDefinition MOVE_TO_TARGET = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("arm0", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(43.3333F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("arm1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-43.3333F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("leg0", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("leg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();

  public static final AnimationDefinition WALK_TO_TARGET = AnimationDefinition.Builder.withLength(0.0F).looping()
      .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.5F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -5.5F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("leg0", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .addAnimation("leg1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
      ))
      .build();
}