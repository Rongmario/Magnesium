package me.jellysquid.mods.sodium.mixin.features.fast_biome_colors;

import me.jellysquid.mods.sodium.client.util.color.FastCubicSampler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.event.entity.EntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinBackgroundRenderer<T extends Entity> {
    /*
    @Redirect(method = "updateFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getFogColor(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
    private static Vec3d redirectSampleColor(Block instance, World world, BlockPos pos, IBlockState p_getFogColor_3_, Entity p_getFogColor_4_, Vec3d p_getFogColor_5_, float tickDelta) {
        float u = MathHelper.clamp(MathHelper.cos(world.getCelestialAngle(tickDelta) * 6.2831855F) * 2.0F + 0.5F, 0.0F, 1.0F);


        return FastCubicSampler.sampleColor(pos,
                (x, y, z) -> world.getBiomeProvider().getBiome(new BlockPos(x,y,z)).get,
                (v) -> world.getSkyProperties().adjustFogColor(v, u));
    }

     */
}