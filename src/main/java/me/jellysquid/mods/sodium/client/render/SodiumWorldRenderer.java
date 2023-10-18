package me.jellysquid.mods.sodium.client.render;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderBackend;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderManager;
import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.MultidrawChunkRenderBackend;
import me.jellysquid.mods.sodium.client.render.chunk.backends.oneshot.ChunkRenderBackendOneshot;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.format.DefaultModelVertexFormats;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheShared;
import me.jellysquid.mods.sodium.client.util.math.FrustumExtended;
import me.jellysquid.mods.sodium.client.world.ChunkStatusListener;
import me.jellysquid.mods.sodium.client.world.ChunkStatusListenerManager;
import me.jellysquid.mods.sodium.common.util.ListUtil;
import me.jellysquid.mods.sodium.compat.client.renderer.CompatRenderLayer;
import me.jellysquid.mods.sodium.compat.client.renderer.CompatRenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;

import java.util.Set;
import java.util.SortedSet;

/**
 * Provides an extension to vanilla's {@link WorldRenderer}.
 */
public class SodiumWorldRenderer implements ChunkStatusListener {
    // We'll keep it to have compatibility with Oculus' older versions
    public static boolean hasChanges = false;
    private static SodiumWorldRenderer instance;
    private final Minecraft client;
    private final LongSet loadedChunkPositions = new LongOpenHashSet();
    private final Set<TileEntity> globalBlockEntities = new ObjectOpenHashSet<>();
    private WorldClient world;
    private int renderDistance;
    private double lastCameraX, lastCameraY, lastCameraZ;
    private double lastCameraPitch, lastCameraYaw;
    private boolean useEntityCulling;
    private ClippingHelper frustum;
    private ChunkRenderManager<?> chunkRenderManager;
    private BlockRenderPassManager renderPassManager;
    private ChunkRenderBackend<?> chunkRenderBackend;

    private SodiumWorldRenderer(Minecraft client) {
        this.client = client;
    }

    /**
     * Instantiates Sodium's world renderer. This should be called at the time of the world renderer initialization.
     */
    public static SodiumWorldRenderer create() {
        if (instance == null) {
            instance = new SodiumWorldRenderer(Minecraft.getMinecraft());
        }

        return instance;
    }

    /**
     * @return The current instance of this type
     * @throws IllegalStateException If the renderer has not yet been created
     */
    public static SodiumWorldRenderer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Renderer not initialized");
        }

        return instance;
    }

    private static ChunkRenderBackend<?> createChunkRenderBackend(RenderDevice device,
                                                                  SodiumGameOptions options,
                                                                  ChunkVertexType vertexFormat) {
        boolean disableBlacklist = SodiumClientMod.options().advanced.ignoreDriverBlacklist;

        if (options.advanced.useChunkMultidraw && MultidrawChunkRenderBackend.isSupported(disableBlacklist)) {
            return new MultidrawChunkRenderBackend(device, vertexFormat);
        } else {
            return new ChunkRenderBackendOneshot(vertexFormat);
        }
    }

    public void setWorld(WorldClient world) {
        // Check that the world is actually changing
        if (this.world == world) {
            return;
        }

        // If we have a world is already loaded, unload the renderer
        if (this.world != null) {
            this.unloadWorld();
        }

        // If we're loading a new world, load the renderer
        if (world != null) {
            this.loadWorld(world);
        }
    }

    private void loadWorld(WorldClient world) {
        this.world = world;

        ChunkRenderCacheShared.createRenderContext(this.world);

        this.initRenderer();

        ((ChunkStatusListenerManager) world.getChunkProvider()).setListener(this);
    }

    private void unloadWorld() {
        ChunkRenderCacheShared.destroyRenderContext(this.world);

        if (this.chunkRenderManager != null) {
            this.chunkRenderManager.destroy();
            this.chunkRenderManager = null;
        }

        if (this.chunkRenderBackend != null) {
            this.chunkRenderBackend.delete();
            this.chunkRenderBackend = null;
        }

        this.loadedChunkPositions.clear();
        this.globalBlockEntities.clear();

        this.world = null;
    }

    /**
     * @return The number of chunk renders which are visible in the current camera's frustum
     */
    public int getVisibleChunkCount() {
        return this.chunkRenderManager.getVisibleChunkCount();
    }

    /**
     * Notifies the chunk renderer that the graph scene has changed and should be re-computed.
     */
    public void scheduleTerrainUpdate() {
        // BUG: seems to be called before init
        if (this.chunkRenderManager != null) {
            this.chunkRenderManager.markDirty();
        }
    }

    /**
     * @return True if no chunks are pending rebuilds
     */
    public boolean isTerrainRenderComplete() {
        return this.chunkRenderManager.isBuildComplete();
    }

    /**
     * Called prior to any chunk rendering in order to update necessary state.
     */
    public void updateChunks(ActiveRenderInfo camera, ClippingHelper frustum, boolean hasForcedFrustum, int frame, boolean spectator) {
        this.frustum = frustum;

        this.useEntityCulling = SodiumClientMod.options().advanced.useEntityCulling;
        Profiler profiler = this.client.profiler;
        profiler.startSection("camera_setup");

        AbstractClientPlayer player = this.client.player;

        if (player == null) {
            throw new IllegalStateException("Client instance has no active player entity");
        }

        Vec3d pos = ActiveRenderInfo.getCameraPosition();
        float pitch = ActiveRenderInfo.getRotationXY();
        float yaw = ActiveRenderInfo.getRotationXZ();

        boolean dirty = pos.x != this.lastCameraX || pos.y != this.lastCameraY || pos.z != this.lastCameraZ ||
                pitch != this.lastCameraPitch || yaw != this.lastCameraYaw;

        if (dirty) {
            this.chunkRenderManager.markDirty();
        }

        this.lastCameraX = pos.x;
        this.lastCameraY = pos.y;
        this.lastCameraZ = pos.z;
        this.lastCameraPitch = pitch;
        this.lastCameraYaw = yaw;

        // profiler.startSection("chunk_update");

        this.chunkRenderManager.updateChunks();

        if (!hasForcedFrustum && this.chunkRenderManager.isDirty()) {
            //profiler.("chunk_graph_rebuild");

            this.chunkRenderManager.update(camera, (FrustumExtended) frustum, frame, spectator);
        }

        // profiler.startSection("visible_chunk_tick");

        this.chunkRenderManager.tickVisibleRenders();

        profiler.endSection();

        Entity.setRenderDistanceWeight(MathHelper.clamp((double) this.client.gameSettings.renderDistanceChunks / 8.0D, 1.0D, 2.5D));
    }

    /**
     * Performs a render pass for the given {@link CompatRenderLayer} and draws all visible chunks for it.
     */
    public void drawChunkLayer(CompatRenderLayer renderLayer, double x, double y, double z) {
        BlockRenderPass pass = this.renderPassManager.getRenderPassForLayer(renderLayer);
        pass.startDrawing();

        this.chunkRenderManager.renderLayer(pass, x, y, z);

        pass.endDrawing();

        CompatRenderSystem.clearCurrentColor();
    }

    public void reload() {
        if (this.world == null) {
            return;
        }

        this.initRenderer();
    }

    private void initRenderer() {
        if (this.chunkRenderManager != null) {
            this.chunkRenderManager.destroy();
            this.chunkRenderManager = null;
        }

        if (this.chunkRenderBackend != null) {
            this.chunkRenderBackend.delete();
            this.chunkRenderBackend = null;
        }

        RenderDevice device = RenderDevice.INSTANCE;

        SodiumGameOptions opts = SodiumClientMod.options();

        this.renderPassManager = BlockRenderPassManager.createDefaultMappings();

        final ChunkVertexType vertexFormat;

        if (opts.advanced.useCompactVertexFormat) {
            vertexFormat = DefaultModelVertexFormats.MODEL_VERTEX_HFP;
        } else {
            vertexFormat = DefaultModelVertexFormats.MODEL_VERTEX_SFP;
        }

        this.chunkRenderBackend = createChunkRenderBackend(device, opts, vertexFormat);
        this.chunkRenderBackend.createShaders(device);

        this.chunkRenderManager = new ChunkRenderManager<>(this, this.chunkRenderBackend, this.renderPassManager, this.world, this.client.gameSettings.renderDistanceChunks);
        this.chunkRenderManager.restoreChunks(this.loadedChunkPositions);
    }

    public void renderTileEntities(Long2ObjectMap<SortedSet<DestroyBlockProgress>> blockBreakingProgressions,
                                   ActiveRenderInfo camera, float tickDelta) {
        // VertexConsumerProvider.Immediate immediate = bufferBuilders.getEntityVertexConsumers();
        Vec3d cameraPos = ActiveRenderInfo.getCameraPosition();
        double x = cameraPos.x;
        double y = cameraPos.y;
        double z = cameraPos.z;
        for (TileEntity blockEntity : this.chunkRenderManager.getVisibleBlockEntities()) {
            BlockPos pos = blockEntity.getPos();

            GlStateManager.pushMatrix();
            GlStateManager.translate((double) pos.getX() - x, (double) pos.getY() - y, (double) pos.getZ() - z);

            SortedSet<DestroyBlockProgress> breakingInfos = blockBreakingProgressions.get(pos.toLong());

            if (breakingInfos != null && !breakingInfos.isEmpty()) {
                int stage = breakingInfos.last().getPartialBlockDamage();

                if (stage >= 0) {

                    //BufferBuilder transformer = new BufferBuilder(bufferBuilders.getEffectVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(stage)), entry.getModel(), entry.getNormal());
                    //consumer = (layer) -> layer.hasCrumbling() ? VertexConsumers.union(transformer, immediate.getBuffer(layer)) : immediate.getBuffer(layer);
                }
                TileEntityRendererDispatcher.instance.render(blockEntity, tickDelta, stage);
            }


            //BlockEntityRenderDispatcher.INSTANCE.render(blockEntity, tickDelta, matrices, consumer);

            GlStateManager.popMatrix();
        }

        for (TileEntity blockEntity : this.globalBlockEntities) {
            BlockPos pos = blockEntity.getPos();

            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translate((double) pos.getX() - x, (double) pos.getY() - y, (double) pos.getZ() - z);
            TileEntityRendererDispatcher.instance.render(blockEntity, tickDelta, 1);
            //BlockEntityRenderDispatcher.INSTANCE.render(blockEntity, tickDelta, matrices, immediate);

            GlStateManager.popMatrix();
        }
    }

    @Override
    public void onChunkAdded(int x, int z) {
        this.loadedChunkPositions.add(ChunkPos.asLong(x, z));
        this.chunkRenderManager.onChunkAdded(x, z);
    }

    @Override
    public void onChunkRemoved(int x, int z) {
        this.loadedChunkPositions.remove(ChunkPos.asLong(x, z));
        this.chunkRenderManager.onChunkRemoved(x, z);
    }

    public void onChunkRenderUpdated(int x, int y, int z, ChunkRenderData meshBefore, ChunkRenderData meshAfter) {
        ListUtil.updateList(this.globalBlockEntities, meshBefore.getGlobalBlockEntities(), meshAfter.getGlobalBlockEntities());

        // FlywheelCompat.filterBlockEntityList(this.globalBlockEntities);

        this.chunkRenderManager.onChunkRenderUpdates(x, y, z, meshAfter);
    }

    /**
     * Returns whether or not the entity intersects with any visible chunks in the graph.
     *
     * @return True if the entity is visible, otherwise false
     */
    public boolean isEntityVisible(Entity entity) {
        if (!this.useEntityCulling) {
            return true;
        }

        AxisAlignedBB box = entity.getRenderBoundingBox();

        // Entities outside the valid world height will never map to a rendered chunk
        // Always render these entities or they'll be culled incorrectly!
        if (box.maxY < 0.5D || box.minY > 255.5D) {
            return true;
        }

        // Ensure entities with outlines or nametags are always visible
        if (entity.getAlwaysRenderNameTagForRender()) {
            return true;
        }

        int minX = MathHelper.floor(box.minX - 0.5D) >> 4;
        int minY = MathHelper.floor(box.minY - 0.5D) >> 4;
        int minZ = MathHelper.floor(box.minZ - 0.5D) >> 4;

        int maxX = MathHelper.floor(box.maxX + 0.5D) >> 4;
        int maxY = MathHelper.floor(box.maxY + 0.5D) >> 4;
        int maxZ = MathHelper.floor(box.maxZ + 0.5D) >> 4;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (this.chunkRenderManager.isChunkVisible(x, y, z)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * @return The frustum of the current player's camera used to cull chunks
     */
    public ClippingHelper getFrustum() {
        return this.frustum;
    }

    public String getChunksDebugString() {
        // C: visible/total
        // TODO: add dirty and queued counts
        return String.format("C: %s/%s", this.chunkRenderManager.getVisibleChunkCount(), this.chunkRenderManager.getTotalSections());
    }

    /**
     * Schedules chunk rebuilds for all chunks in the specified block region.
     */
    public void scheduleRebuildForBlockArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        this.scheduleRebuildForChunks(minX >> 4, minY >> 4, minZ >> 4, maxX >> 4, maxY >> 4, maxZ >> 4, important);
    }

    /**
     * Schedules chunk rebuilds for all chunks in the specified chunk region.
     */
    public void scheduleRebuildForChunks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkY = minY; chunkY <= maxY; chunkY++) {
                for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                    this.scheduleRebuildForChunk(chunkX, chunkY, chunkZ, important);
                }
            }
        }
    }

    /**
     * Schedules a chunk rebuild for the render belonging to the given chunk section position.
     */
    public void scheduleRebuildForChunk(int x, int y, int z, boolean important) {
        this.chunkRenderManager.scheduleRebuild(x, y, z, important);
    }

    public ChunkRenderBackend<?> getChunkRenderer() {
        return this.chunkRenderBackend;
    }
}
