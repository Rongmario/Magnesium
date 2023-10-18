package me.jellysquid.mods.sodium.compat.client.renderer;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;

public final class CompatRenderLayer {
    private static final CompatRenderLayer SOLID = create(BlockRenderLayer.SOLID, DefaultVertexFormats.BLOCK, 7, 2097152, true, false);
    private static final CompatRenderLayer CUTOUT_MIPPED = create(BlockRenderLayer.CUTOUT_MIPPED, DefaultVertexFormats.BLOCK, 7, 131072, true, false);
    private static final CompatRenderLayer CUTOUT = create(BlockRenderLayer.CUTOUT, DefaultVertexFormats.BLOCK, 7, 131072, true, false);
    private static final CompatRenderLayer TRANSLUCENT = create(BlockRenderLayer.TRANSLUCENT, DefaultVertexFormats.BLOCK, 7, 262144, true, true);
    private final BlockRenderLayer type;
    private final VertexFormat format;
    private final int mode;
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;
    private CompatRenderLayer(BlockRenderLayer type, VertexFormat format, int mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload) {
        this.type = type;
        this.format = format;
        this.mode = mode;
        this.bufferSize = bufferSize;
        this.affectsCrumbling = affectsCrumbling;
        this.sortOnUpload = sortOnUpload;
    }

    public static CompatRenderLayer getSolid() {
        return SOLID;
    }

    public static CompatRenderLayer getCutoutMipped() {
        return CUTOUT_MIPPED;
    }

    public static CompatRenderLayer getCutout() {
        return CUTOUT;
    }

    public static CompatRenderLayer getTranslucent() {
        return TRANSLUCENT;
    }

    public static CompatRenderLayer from(BlockRenderLayer renderLayer) {
        switch (renderLayer) {
            case SOLID:
                return getSolid();
            case CUTOUT:
                return getCutout();
            case TRANSLUCENT:
                return getTranslucent();
            case CUTOUT_MIPPED:
                return getCutoutMipped();
        }
        return null;
    }

    public static CompatRenderLayer create(BlockRenderLayer type, VertexFormat format, int mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload) {
        return new CompatRenderLayer(type, format, mode, bufferSize, affectsCrumbling, sortOnUpload);
    }


}
