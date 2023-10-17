package me.jellysquid.mods.sodium.client.util.color;

import me.jellysquid.mods.sodium.compat.util.CompatVec3d;
import me.jellysquid.mods.sodium.compat.util.math.CompatMathHelper;
import me.jellysquid.mods.sodium.compat.util.math.Vector3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.function.Function;

public class FastCubicSampler {
    private static final double[] DENSITY_CURVE = new double[]{0.0D, 1.0D, 4.0D, 6.0D, 4.0D, 1.0D, 0.0D};
    private static final int DIAMETER = 6;

    public static Vec3d sampleColor(Vec3d pos, ColorFetcher colorFetcher, Function<Vec3d, Vec3d> transformer) {
        int intX = MathHelper.floor(pos.x);
        int intY = MathHelper.floor(pos.y);
        int intZ = MathHelper.floor(pos.z);

        int[] values = new int[DIAMETER * DIAMETER * DIAMETER];

        for (int x = 0; x < DIAMETER; ++x) {
            int blockX = (intX - 2) + x;

            for (int y = 0; y < DIAMETER; ++y) {
                int blockY = (intY - 2) + y;

                for (int z = 0; z < DIAMETER; ++z) {
                    int blockZ = (intZ - 2) + z;

                    values[index(x, y, z)] = colorFetcher.fetch(blockX, blockY, blockZ);
                }
            }
        }

        // Fast path! Skip blending the colors if all inputs are the same
        if (isHomogenousArray(values)) {
            // Take the first color if it's homogenous (all elements are the same...)
            return transformer.apply(CompatVec3d.unpackRgb(values[0]));
        }


        double deltaX = pos.x - (double) intX;
        double deltaY = pos.y - (double) intY;
        double deltaZ = pos.z - (double) intZ;

        Vec3d sum = Vec3d.ZERO;
        double totalFactor = 0.0D;

        for (int x = 0; x < DIAMETER; ++x) {
            double densityX = CompatMathHelper.lerp(deltaX, DENSITY_CURVE[x + 1], DENSITY_CURVE[x]);

            for (int y = 0; y < DIAMETER; ++y) {
                double densityY = CompatMathHelper.lerp(deltaY, DENSITY_CURVE[y + 1], DENSITY_CURVE[y]);

                for (int z = 0; z < DIAMETER; ++z) {
                    double densityZ = CompatMathHelper.lerp(deltaZ, DENSITY_CURVE[z + 1], DENSITY_CURVE[z]);

                    double factor = densityX * densityY * densityZ;
                    totalFactor += factor;
                    Vector3d color = new Vector3d(transformer.apply(CompatVec3d.unpackRgb(values[index(x, y, z)])));
                    color = color.multiply(factor);
                    sum = sum.add(new Vec3d(color.x, color.y, color.z));
                }
            }
        }
        Vector3d temp = new Vector3d(sum).multiply(1.0D / totalFactor);
        sum = new Vec3d(temp.x, temp.y, temp.z);

        return sum;
    }

    private static int index(int x, int y, int z) {
        return (DIAMETER * DIAMETER * z) + (DIAMETER * y) + x;
    }

    private static boolean isHomogenousArray(int[] arr) {
        int val = arr[0];

        for (int i = 1; i < arr.length; i++) {
            if (arr[i] != val) {
                return false;
            }
        }

        return true;
    }

    public interface ColorFetcher {
        int fetch(int x, int y, int z);
    }
}