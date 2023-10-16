package me.jellysquid.mods.sodium.client.gl.compat;

import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL20;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class FogHelper {
    private static final float FAR_PLANE_THRESHOLD_EXP = (float) Math.log(1.0f / 0.0019f);
    private static final float FAR_PLANE_THRESHOLD_EXP2 = MathHelper.sqrt(FAR_PLANE_THRESHOLD_EXP);

    public static float getFogEnd() {
        return GlStateManager.fogState.end;
    }

    public static float getFogStart() {
        return GlStateManager.fogState.start;
    }

    public static float getFogDensity() {
        return GlStateManager.fogState.density;
    }

    /**
     * Retrieves the current fog mode from the fixed-function pipeline.
     */
    public static ChunkFogMode getFogMode() {
        int mode = GlStateManager.fogState.mode;

        if (mode == 0 || !GlStateManager.fogState.fog.currentState)
            return ChunkFogMode.NONE;

        switch (mode) {
            case CompatGL20.GL_EXP2:
            case CompatGL20.GL_EXP:
                return ChunkFogMode.EXP2;
            case CompatGL20.GL_LINEAR:
                return ChunkFogMode.LINEAR;
            default:
                throw new UnsupportedOperationException("Unknown fog mode: " + mode);
        }
    }

    public static float getFogCutoff() {
        int mode = GlStateManager.fogState.mode;

        switch (mode) {
            case CompatGL20.GL_LINEAR:
                return getFogEnd();
            case CompatGL20.GL_EXP:
                return FAR_PLANE_THRESHOLD_EXP / getFogDensity();
            case CompatGL20.GL_EXP2:
                return FAR_PLANE_THRESHOLD_EXP2 / getFogDensity();
            default:
                return 0.0f;
        }
    }

    public static float[] getFogColor() {
        return new float[]{BackgroundRenderer.red, BackgroundRenderer.green, BackgroundRenderer.blue, 1.0F};
    }
}
