package me.jellysquid.mods.sodium.client.render.pipeline;

import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.blender.BiomeColorBlender;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import me.jellysquid.mods.sodium.client.util.rand.XoRoShiRoRandom;
import me.jellysquid.mods.sodium.client.world.biome.BlockColorsExtended;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import me.jellysquid.mods.sodium.compat.util.math.Direction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;

import java.util.List;
import java.util.Random;

public class BlockRenderer {
    private final Random random = new XoRoShiRoRandom();

    private final BlockColorsExtended blockColors;


    private final QuadLightData cachedQuadLightData = new QuadLightData();

    private final BiomeColorBlender biomeColorBlender;
    private final LightPipelineProvider lighters;

    private final boolean useAmbientOcclusion;

    private final boolean experimentalForgeLightPipelineEnabled;
    private ForgeBlockRenderer forgeBlockRenderer;

    public BlockRenderer(Minecraft client, LightPipelineProvider lighters, BiomeColorBlender biomeColorBlender) {
        this.blockColors = (BlockColorsExtended) client.getBlockColors();
        this.biomeColorBlender = biomeColorBlender;

        this.lighters = lighters;

        this.useAmbientOcclusion = Minecraft.isAmbientOcclusionEnabled();

        this.experimentalForgeLightPipelineEnabled = false;
    }

    private ForgeBlockRenderer getForgeBlockRenderer() {
        if (forgeBlockRenderer == null) {
            return forgeBlockRenderer = new ForgeBlockRenderer();
        }
        return forgeBlockRenderer;
    }

    public boolean renderModel(IBlockAccess world, IBlockState state, BlockPos pos, IBakedModel model, ChunkModelBuffers buffers, boolean cull, long seed) {
        LightMode lightMode = this.getLightingMode(state, model, world, pos);
        LightPipeline lighter = this.lighters.getLighter(lightMode);
        //Vec3d offset = state.getModelOffset(world, pos);
        boolean rendered = false;

        for (Direction dir : DirectionUtil.ALL_DIRECTIONS) {
            this.random.setSeed(seed);

            List<BakedQuad> sided = model.getQuads(state, dir.to(), this.random.nextLong());

            if (sided.isEmpty()) {
                continue;
            }

            if (!cull) {
                this.renderQuadList(world, state, pos, lighter,new Vec3d(0,0,0), buffers, sided, dir);

                rendered = true;
            }
        }

        this.random.setSeed(seed);

        List<BakedQuad> all = model.getQuads(state, null, this.random.nextLong());

        if (!all.isEmpty()) {
            this.renderQuadList(world, state, pos, lighter,new Vec3d(0,0,0), buffers, all, null);

            rendered = true;
        }

        return rendered;
    }

    private void renderQuadList(IBlockAccess world, IBlockState state, BlockPos pos, LightPipeline lighter, Vec3d offset,
                                ChunkModelBuffers buffers, List<BakedQuad> quads, Direction cullFace) {
        ModelQuadFacing facing = cullFace == null ? ModelQuadFacing.UNASSIGNED : ModelQuadFacing.fromDirection(cullFace);
        IBlockColor colorizer = null;

        ModelVertexSink sink = buffers.getSink(facing);
        sink.ensureCapacity(quads.size() * 4);

        ChunkRenderData.Builder renderData = buffers.getRenderData();

        // This is a very hot allocation, iterate over it manually
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0, quadsSize = quads.size(); i < quadsSize; i++) {
            BakedQuad quad = quads.get(i);

            QuadLightData light = this.cachedQuadLightData;
            lighter.calculate((ModelQuadView) quad, pos, light, cullFace, Direction.from(quad.getFace()), quad.shouldApplyDiffuseLighting());

            if (quad.hasTintIndex() && colorizer == null) {
                colorizer = this.blockColors.getColorProvider(state);
            }

            this.renderQuad(world, state, pos, sink, offset, colorizer, quad, light, renderData);
        }

        sink.flush();
    }

    private void renderQuad(IBlockAccess world, IBlockState state, BlockPos pos, ModelVertexSink sink, Vec3d offset,
                            IBlockColor colorProvider, BakedQuad bakedQuad, QuadLightData light, ChunkRenderData.Builder renderData) {
        ModelQuadView src = (ModelQuadView) bakedQuad;

        ModelQuadOrientation order = ModelQuadOrientation.orient(light.br);

        int[] colors = null;

        if (bakedQuad.hasTintIndex()) {
            colors = this.biomeColorBlender.getColors(colorProvider, world, state, pos, src);
        }

        for (int dstIndex = 0; dstIndex < 4; dstIndex++) {
            int srcIndex = order.getVertexIndex(dstIndex);

            float x = src.getX(srcIndex) + (float) offset.x;
            float y = src.getY(srcIndex) + (float) offset.y;
            float z = src.getZ(srcIndex) + (float) offset.z;

            int color = ColorABGR.mul(colors != null ? colors[srcIndex] : src.getColor(srcIndex), light.br[srcIndex]);

            float u = src.getTexU(srcIndex);
            float v = src.getTexV(srcIndex);

            int lm = light.lm[srcIndex];

            sink.writeQuad(x, y, z, color, u, v, lm);
        }

        addSpriteData(src, renderData);
    }

    private void addSpriteData(ModelQuadView src, ChunkRenderData.Builder renderData) {
        TextureAtlasSprite sprite = src.rubidium$getSprite();

        if (sprite != null) {
            renderData.addSprite(sprite);
        }
    }

    private LightMode getLightingMode(IBlockState state, IBakedModel model, IBlockAccess world, BlockPos pos) {
        if (this.useAmbientOcclusion && model.isAmbientOcclusion(state) && state.getLightValue(world, pos) == 0) {
            return LightMode.SMOOTH;
        } else {
            return LightMode.FLAT;
        }
    }

    /*
     * This code is derived from the MinecraftForge project,
     * licensed under the GNU Lesser General Public License, version 2.1.
     * See https://github.com/MinecraftForge/MinecraftForge/blob/1.16.x/LICENSE.txt for more information.
     *
     * This code is also distributed under the terms of the GNU Lesser General Public License, version 3
     * See https://github.com/Asek3/Rubidium/blob/1.16/dev/LICENSE for a copy of the LGPL-3 license.
     */
    private class ForgeBlockRenderer {

        private final ThreadLocal<VertexLighterFlat> lighterFlat;
        private final ThreadLocal<VertexLighterSmoothAo> lighterSmooth;
        private final ThreadLocal<VertexBufferConsumer> consumerFlat = ThreadLocal.withInitial(VertexBufferConsumer::new);
        private final ThreadLocal<VertexBufferConsumer> consumerSmooth = ThreadLocal.withInitial(VertexBufferConsumer::new);

        public ForgeBlockRenderer() {
            BlockColors colors = Minecraft.getMinecraft().getBlockColors();
            lighterFlat = ThreadLocal.withInitial(() -> new VertexLighterFlat(colors));
            lighterSmooth = ThreadLocal.withInitial(() -> new VertexLighterSmoothAo(colors));
        }

        private VertexLighterFlat getLighter(LightMode lightMode, BufferBuilder vertexConsumer) {
            VertexBufferConsumer consumer = null;
            VertexLighterFlat lighter = null;
            switch (lightMode) {
                case SMOOTH:
                    consumer = this.consumerSmooth.get();
                    lighter = this.lighterSmooth.get();
                    break;
                case FLAT:
                    consumer = this.consumerFlat.get();
                    lighter = this.lighterFlat.get();
                    break;
            }
            consumer.setBuffer(vertexConsumer);
            lighter.setParent(consumer);
            //lighter.setTransform(entry);
            return lighter;
        }

        private boolean renderForgePipeline(VertexLighterFlat lighter, IBlockAccess world, IBakedModel model, IBlockState state, BlockPos pos, boolean checkSides, Random rand, long seed, ChunkRenderData.Builder renderData) {
            lighter.setWorld(world);
            lighter.setState(state);
            lighter.setBlockPos(pos);
            boolean empty = true;
            rand.setSeed(seed);
            List<BakedQuad> quads = model.getQuads(state, null, rand.nextLong());
            if (!quads.isEmpty()) {
                lighter.updateBlockInfo();
                empty = false;
                for (BakedQuad quad : quads) {
                    quad.pipe(lighter);
                    addSpriteData((ModelQuadView) quad, renderData);
                }
            }
            for (Direction side : DirectionUtil.ALL_DIRECTIONS) {
                rand.setSeed(seed);
                quads = model.getQuads(state, side.to(), rand.nextLong());
                if (!quads.isEmpty()) {
                    if (!checkSides) {
                        if (empty) lighter.updateBlockInfo();
                        empty = false;
                        for (BakedQuad quad : quads) {
                            quad.pipe(lighter);
                            addSpriteData((ModelQuadView) quad, renderData);
                        }
                    }
                }
            }
            lighter.resetBlockInfo();
            return !empty;
        }
    }
}
