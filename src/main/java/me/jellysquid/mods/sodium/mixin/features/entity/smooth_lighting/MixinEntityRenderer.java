package me.jellysquid.mods.sodium.mixin.features.entity.smooth_lighting;

import me.jellysquid.mods.sodium.client.render.entity.EntityLightSampler;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> implements EntityLightSampler<T> {
    //Moved to Entity


    @Override
    public int bridge$getBlockLight(T entity, BlockPos pos) {

        return this.getBlockLightLevel(entity, pos);
    }

    @Override
    public int bridge$getSkyLight(T entity, BlockPos pos) {
        return this.getSkyLightLevel(entity, pos);
    }

    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return entity.isBurning() ? 15 : entity.world.getLightFor(EnumSkyBlock.BLOCK, pos);
    }

    protected int getSkyLightLevel(T entity, BlockPos pos) {
        return entity.world.getLightFor(EnumSkyBlock.SKY, pos);
    }

}
