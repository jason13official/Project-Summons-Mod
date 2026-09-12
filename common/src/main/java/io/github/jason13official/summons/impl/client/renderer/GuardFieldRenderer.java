package io.github.jason13official.summons.impl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jason13official.summons.impl.common.entity.AbstractCompanion;
import io.github.jason13official.summons.impl.common.party.CompanionMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

/// Draws the Guard Field ring/disc under a companion in DEFEND mode. Reuses vanilla's lightning render type; no new texture needed.
public final class GuardFieldRenderer {

  private static final int SEGMENTS = 32;
  private static final float RING_THICKNESS = 0.15F;
  private static final float RED = 0.6F;
  private static final float GREEN = 0.85F;
  private static final float BLUE = 1.0F;
  private static final float RING_ALPHA = 0.55F;
  private static final float FILL_ALPHA = 0.18F;

  public static void render(AbstractCompanion companion, PoseStack poseStack, MultiBufferSource bufferSource) {
    if (companion.getMode() != CompanionMode.DEFEND) {
      return;
    }

    float outer = companion.getGuardFieldRadius();
    float inner = Math.max(0.0F, outer - RING_THICKNESS);

    poseStack.pushPose();
    poseStack.translate(0.0, 0.02, 0.0); // avoid z-fighting with the ground
    Matrix4f matrix = poseStack.last().pose();
    VertexConsumer buffer = bufferSource.getBuffer(RenderType.lightning());

    // filled disc: triangle fan via degenerate quads; winding matters (cull is on)
    for (int i = 0; i < SEGMENTS; i++) {
      double a0 = Math.PI * 2 * i / SEGMENTS;
      double a1 = Math.PI * 2 * (i + 1) / SEGMENTS;

      float x0 = (float) (Math.cos(a0) * outer);
      float z0 = (float) (Math.sin(a0) * outer);
      float x1 = (float) (Math.cos(a1) * outer);
      float z1 = (float) (Math.sin(a1) * outer);

      buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(RED, GREEN, BLUE, FILL_ALPHA);
      buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(RED, GREEN, BLUE, FILL_ALPHA);

      buffer.addVertex(matrix, x1, 0.0F, z1).setColor(RED, GREEN, BLUE, FILL_ALPHA);
      buffer.addVertex(matrix, x0, 0.0F, z0).setColor(RED, GREEN, BLUE, FILL_ALPHA);
    }

    poseStack.translate(0.0, 0.02, 0.0); // avoid z-fighting with the disc

    // brighter ring on top, for the edge
    for (int i = 0; i < SEGMENTS; i++) {
      poseStack.translate(0.0, 0.02, 0.0); // avoid z-fighting with the disc
      double a0 = Math.PI * 2 * i / SEGMENTS;
      double a1 = Math.PI * 2 * (i + 1) / SEGMENTS;

      float x0i = (float) (Math.cos(a0) * inner);
      float z0i = (float) (Math.sin(a0) * inner);
      float x1i = (float) (Math.cos(a1) * inner);
      float z1i = (float) (Math.sin(a1) * inner);
      float x0o = (float) (Math.cos(a0) * outer);
      float z0o = (float) (Math.sin(a0) * outer);
      float x1o = (float) (Math.cos(a1) * outer);
      float z1o = (float) (Math.sin(a1) * outer);

      buffer.addVertex(matrix, x0i, 0.0F, z0i).setColor(RED, GREEN, BLUE, RING_ALPHA);
      buffer.addVertex(matrix, x1i, 0.0F, z1i).setColor(RED, GREEN, BLUE, RING_ALPHA);
      buffer.addVertex(matrix, x1o, 0.0F, z1o).setColor(RED, GREEN, BLUE, RING_ALPHA);
      buffer.addVertex(matrix, x0o, 0.0F, z0o).setColor(RED, GREEN, BLUE, RING_ALPHA);
    }

    poseStack.popPose();
  }
}
