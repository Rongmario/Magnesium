package me.jellysquid.mods.sodium.client.render.chunk.passes;

import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import me.jellysquid.mods.sodium.compat.client.renderer.CompatRenderLayer;

/**
 * Maps vanilla render layers to render passes used by Sodium. This provides compatibility with the render layers already
 * used by the base game.
 */
public class BlockRenderPassManager {
    private final Reference2IntArrayMap<CompatRenderLayer> mappingsId = new Reference2IntArrayMap<>();

    public BlockRenderPassManager() {
        this.mappingsId.defaultReturnValue(-1);
    }

    /**
     * Creates a set of render pass mappings to vanilla render layers which closely mirrors the rendering
     * behavior of vanilla.
     */
    public static BlockRenderPassManager createDefaultMappings() {
        BlockRenderPassManager mapper = new BlockRenderPassManager();
        mapper.addMapping(CompatRenderLayer.getSolid(), BlockRenderPass.SOLID);
        mapper.addMapping(CompatRenderLayer.getCutoutMipped(), BlockRenderPass.CUTOUT_MIPPED);
        mapper.addMapping(CompatRenderLayer.getCutout(), BlockRenderPass.CUTOUT);
        mapper.addMapping(CompatRenderLayer.getTranslucent(), BlockRenderPass.TRANSLUCENT);

        return mapper;
    }

    public int getRenderPassId(CompatRenderLayer layer) {
        int pass = this.mappingsId.getInt(layer);

        if (pass < 0) {
            throw new NullPointerException("No render pass exists for layer: " + layer);
        }

        return pass;
    }

    private void addMapping(CompatRenderLayer layer, BlockRenderPass type) {
        if (this.mappingsId.put(layer, type.ordinal()) >= 0) {
            throw new IllegalArgumentException("Layer target already defined for " + layer);
        }
    }

    public BlockRenderPass getRenderPassForLayer(CompatRenderLayer layer) {
        return this.getRenderPass(this.getRenderPassId(layer));
    }

    public BlockRenderPass getRenderPass(int i) {
        return BlockRenderPass.VALUES[i];
    }
}
