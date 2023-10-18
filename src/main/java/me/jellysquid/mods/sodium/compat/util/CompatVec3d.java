package me.jellysquid.mods.sodium.compat.util;

import net.minecraft.util.math.Vec3d;

public final class CompatVec3d {
    public static Vec3d unpackRgb(int rgb) {
        double d0 = (double) (rgb >> 16 & 255) / 255.0;
        double d1 = (double) (rgb >> 8 & 255) / 255.0;
        double d2 = (double) (rgb & 255) / 255.0;
        return new Vec3d(d0, d1, d2);
    }
}
