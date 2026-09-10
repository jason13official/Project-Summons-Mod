package io.github.jason13official.summons.impl.common.entity;

import io.github.jason13official.summons.impl.common.evolution.EvoCrystalColor;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/// Evo Crystal point totals, one per color, as a single value instead of five independent
/// int fields. Own `StreamCodec`, same shape as vanilla's per-type serializers (SnifferState,
/// ArmadilloState); not a generic `EntityDataSerializer`, since mods can't register those.
public record CrystalPoints(int red, int blue, int green, int yellow, int white) {
  public static final CrystalPoints ZERO = new CrystalPoints(0, 0, 0, 0, 0);

  public static final StreamCodec<ByteBuf, CrystalPoints> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, CrystalPoints::red,
      ByteBufCodecs.VAR_INT, CrystalPoints::blue,
      ByteBufCodecs.VAR_INT, CrystalPoints::green,
      ByteBufCodecs.VAR_INT, CrystalPoints::yellow,
      ByteBufCodecs.VAR_INT, CrystalPoints::white,
      CrystalPoints::new);

  public int get(EvoCrystalColor color) {
    return switch (color) {
      case RED -> this.red;
      case BLUE -> this.blue;
      case GREEN -> this.green;
      case YELLOW -> this.yellow;
      case WHITE -> this.white;
    };
  }

  /// returns a copy with `color` set to `points` (floored at 0)
  public CrystalPoints with(EvoCrystalColor color, int points) {
    int value = Math.max(0, points);
    return switch (color) {
      case RED -> new CrystalPoints(value, this.blue, this.green, this.yellow, this.white);
      case BLUE -> new CrystalPoints(this.red, value, this.green, this.yellow, this.white);
      case GREEN -> new CrystalPoints(this.red, this.blue, value, this.yellow, this.white);
      case YELLOW -> new CrystalPoints(this.red, this.blue, this.green, value, this.white);
      case WHITE -> new CrystalPoints(this.red, this.blue, this.green, this.yellow, value);
    };
  }

  public int total() {
    return this.red + this.blue + this.green + this.yellow + this.white;
  }
}
