package me.jellysquid.mods.sodium.compat.util.math;

public final class CompatChunkPos {

    public static int getX(long p_212578_0_) {
        return (int) (p_212578_0_ & 4294967295L);
    }

    public static int getZ(long p_212579_0_) {
        return (int) (p_212579_0_ >>> 32 & 4294967295L);
    }
}
