package me.jellysquid.mods.sodium.client.render.chunk.tasks;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderContainer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkMeshData;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;

/**
 * Rebuilds all the meshes of a chunk for each given render pass with non-occluded blocks. The result is then uploaded
 * to graphics memory on the main thread.
 * <p>
 * This task takes a slice of the world from the thread it is created on. Since these slices require rather large
 * array allocations, they are pooled to ensure that the garbage collector doesn't become overloaded.
 */
public class ChunkRenderRebuildTask<T extends ChunkGraphicsState> extends ChunkRenderBuildTask<T> {
    private final ChunkRenderContainer<T> render;

    private final BlockPos offset;

    private final ChunkRenderContext context;

    // private final Map<BlockPos, IModelData> modelData;

    public ChunkRenderRebuildTask(ChunkRenderContainer<T> render, ChunkRenderContext context, BlockPos offset) {
        this.render = render;
        this.offset = offset;
        this.context = context;

        // this.modelData = ModelDataManager.getModelData(MinecraftClient.getInstance().world, new ChunkPos(ChunkSectionPos.getSectionCoord(this.render.getOriginX()), ChunkSectionPos.getSectionCoord(this.render.getOriginZ())));
    }

    /*
    public IModelData getModelData(BlockPos pos) {
        return modelData.getOrDefault(pos, EmptyModelData.INSTANCE);
    }
     */

    @Override
    public ChunkBuildResult<T> performBuild(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers, CancellationSource cancellationSource) {
        ChunkRenderData.Builder renderData = new ChunkRenderData.Builder();
        VisGraph occluder = new VisGraph();
        ChunkRenderBounds.Builder bounds = new ChunkRenderBounds.Builder();

        buffers.init(renderData);

        cache.init(this.context);

        WorldSlice slice = cache.getWorldSlice();

        int baseX = this.render.getOriginX();
        int baseY = this.render.getOriginY();
        int baseZ = this.render.getOriginZ();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos renderOffset = this.offset;

        for (int relY = 0; relY < 16; relY++) {
            if (cancellationSource.isCancelled()) {
                return null;
            }

            for (int relZ = 0; relZ < 16; relZ++) {
                for (int relX = 0; relX < 16; relX++) {
                    IBlockState blockState = slice.getBlockStateRelative(relX + 16, relY + 16, relZ + 16);

                    if (blockState.getBlock() == Blocks.AIR) {
                        continue;
                    }

                    // TODO: commit this separately
                    pos.setPos(baseX + relX, baseY + relY, baseZ + relZ);
                    buffers.setRenderOffset(pos.getX() - renderOffset.getX(), pos.getY() - renderOffset.getY(), pos.getZ() - renderOffset.getZ());
                    /*
                    FluidState fluidState = blockState.getFluidState();
                    IModelData modelData = getModelData(pos);
                    for (RenderLayer layer : RenderLayer.getBlockLayers()) {
                        ForgeHooksClient.setRenderLayer(layer);

                        if (!fluidState.isEmpty() && RenderLayers.canRenderInLayer(fluidState, layer)) {
                            if (cache.getFluidRenderer().render(slice, fluidState, pos, buffers.get(layer))) {
                                bounds.addBlock(relX, relY, relZ);
                            }
                        }

                        if (blockState.getRenderType() == BlockRenderType.MODEL && RenderLayers.canRenderInLayer(blockState, layer)) {
                            BakedModel model = cache.getBlockModels()
                                    .getModel(blockState);

                            long seed = blockState.getRenderingSeed(pos);

                            if (cache.getBlockRenderer().renderModel(slice, blockState, pos, model, buffers.get(layer), true, seed, modelData)) {
                                bounds.addBlock(relX, relY, relZ);
                            }
                        }
                    }
                     */

                    if (slice.getTileEntity(pos) != null) {
                        TileEntity entity = slice.getBlockEntity(pos);

                        if (entity != null) {

                            TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(entity);
                            if (renderer != null) {
                                renderData.addBlockEntity(entity, !renderer.isGlobalRenderer(entity));

                                bounds.addBlock(relX, relY, relZ);
                            }
                        }
                    }

                    if (blockState.isOpaqueCube()) {
                        //occluder.markClosed(pos);
                    }
                }
            }
        }

        ForgeHooksClient.setRenderLayer(null);

        for (BlockRenderPass pass : BlockRenderPass.VALUES) {
            ChunkMeshData mesh = buffers.createMesh(pass);

            if (mesh != null) {
                renderData.setMesh(pass, mesh);
            }
        }

        renderData.setOcclusionData(occluder.computeVisibility());
        renderData.setBounds(bounds.build(this.render.getChunkPos()));

        return new ChunkBuildResult<>(this.render, renderData.build());
    }

    @Override
    public void releaseResources() {
        this.context.releaseResources();
    }
}
