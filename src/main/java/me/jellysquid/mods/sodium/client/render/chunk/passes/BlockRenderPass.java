package me.jellysquid.mods.sodium.client.render.chunk.passes;

import me.jellysquid.mods.sodium.compat.client.renderer.CompatRenderLayer;

// TODO: Move away from using an enum, make this extensible
public enum BlockRenderPass {
    SOLID(CompatRenderLayer.getSolid(), false),
    CUTOUT(CompatRenderLayer.getCutout(), false),
    CUTOUT_MIPPED(CompatRenderLayer.getCutoutMipped(), false),
    TRANSLUCENT(CompatRenderLayer.getTranslucent(), true);

    public static final BlockRenderPass[] VALUES = BlockRenderPass.values();
    public static final int COUNT = VALUES.length;

    private final CompatRenderLayer layer;
    private final boolean translucent;

    BlockRenderPass(CompatRenderLayer layer, boolean translucent) {
        this.layer = layer;
        this.translucent = translucent;
    }

    public boolean isTranslucent() {
        return this.translucent;
    }

    public void endDrawing() {
        //this.layer.endDrawing();
    }

    public void startDrawing() {
        //this.layer.startDrawing();
    }
}
