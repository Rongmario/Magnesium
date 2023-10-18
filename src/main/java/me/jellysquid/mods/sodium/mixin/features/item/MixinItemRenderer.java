package me.jellysquid.mods.sodium.mixin.features.item;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.VertexDrain;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import me.jellysquid.mods.sodium.client.util.ModelQuadUtil;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import me.jellysquid.mods.sodium.client.util.color.ColorARGB;
import me.jellysquid.mods.sodium.client.util.rand.XoRoShiRoRandom;
import me.jellysquid.mods.sodium.client.world.biome.ItemColorsExtended;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import me.jellysquid.mods.sodium.compat.util.math.Direction;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RenderItem.class)
public class MixinItemRenderer {
    private final XoRoShiRoRandom random = new XoRoShiRoRandom();

    @Shadow
    @Final
    private ItemColors itemColors;

    /**
     * @reason Avoid allocations
     * @author JellySquid
     */
    @Overwrite
    public void renderModel(IBakedModel model, int color, ItemStack stack) {
        XoRoShiRoRandom random = this.random;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
        for (Direction direction : DirectionUtil.ALL_DIRECTIONS) {
            List<BakedQuad> quads = model.getQuads(null, direction.to(), random.setSeedAndReturn(42L).nextLong());

            if (!quads.isEmpty()) {
                this.renderQuads(bufferbuilder, quads, color, stack);
            }
        }

        List<BakedQuad> quads = model.getQuads(null, null, random.setSeedAndReturn(42L).nextLong());

        if (!quads.isEmpty()) {
            this.renderQuads(bufferbuilder, quads, color, stack);
        }

        tessellator.draw();
    }

    /**
     * @reason Use vertex building intrinsics
     * @author JellySquid
     */
    @Overwrite
    public void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, int c, ItemStack stack) {

        IItemColor colorProvider = null;

        QuadVertexSink drain = VertexDrain.of(renderer)
                .createSink(VanillaVertexTypes.QUADS);
        drain.ensureCapacity(quads.size() * 4);

        for (BakedQuad bakedQuad : quads) {
            int color = 0xFFFFFFFF;

            if (!stack.isEmpty() && bakedQuad.hasTintIndex()) {
                if (colorProvider == null) {
                    colorProvider = ((ItemColorsExtended) this.itemColors).getColorProvider(stack);
                }

                color = ColorARGB.toABGR(colorProvider != null ? colorProvider.colorMultiplier(stack, bakedQuad.getTintIndex()) :
                        this.itemColors.colorMultiplier(stack, bakedQuad.getTintIndex()), 255);
            }

            ModelQuadView quad = ((ModelQuadView) bakedQuad);

            for (int i = 0; i < 4; i++) {
                int fColor = multABGRInts(quad.getColor(i), color);

                drain.writeQuad(quad.getX(i), quad.getY(i), quad.getZ(i), fColor, quad.getTexU(i), quad.getTexV(i),
                        1, 1, ModelQuadUtil.getFacingNormal(Direction.from(bakedQuad.getFace())));
            }

            SpriteUtil.markSpriteActive(quad.rubidium$getSprite());
        }

        drain.flush();
    }

    private int multABGRInts(int colorA, int colorB) {
        // Most common case: Either quad coloring or tint-based coloring, but not both
        if (colorA == -1) {
            return colorB;
        } else if (colorB == -1) {
            return colorA;
        }
        // General case (rare): Both colorings, actually perform the multiplication
        int a = (int) ((ColorABGR.unpackAlpha(colorA) / 255.0f) * (ColorABGR.unpackAlpha(colorB) / 255.0f) * 255.0f);
        int b = (int) ((ColorABGR.unpackBlue(colorA) / 255.0f) * (ColorABGR.unpackBlue(colorB) / 255.0f) * 255.0f);
        int g = (int) ((ColorABGR.unpackGreen(colorA) / 255.0f) * (ColorABGR.unpackGreen(colorB) / 255.0f) * 255.0f);
        int r = (int) ((ColorABGR.unpackRed(colorA) / 255.0f) * (ColorABGR.unpackRed(colorB) / 255.0f) * 255.0f);
        return ColorABGR.pack(r, g, b, a);
    }

}
