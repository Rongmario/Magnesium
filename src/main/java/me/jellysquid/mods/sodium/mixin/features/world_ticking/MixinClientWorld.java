package me.jellysquid.mods.sodium.mixin.features.world_ticking;

import me.jellysquid.mods.sodium.client.util.rand.XoRoShiRoRandom;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;
import java.util.function.Supplier;

// Priority for compatibility with AbnormalsCore/BetterFoliage
@Mixin(value = ClientWorld.class, priority = 990)
public abstract class MixinClientWorld extends World {
    @Shadow
    protected abstract void addParticle(BlockPos pos, BlockState state, ParticleEffect parameters, boolean bl);

    protected MixinClientWorld(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey,
                               DimensionType dimensionType, Supplier<Profiler> profiler, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, registryKey, dimensionType, profiler, bl, bl2, l);
    }

    @Redirect(method = "doRandomBlockDisplayTicks", at = @At(value = "NEW", target = "java/util/Random"))
    private Random redirectRandomTickRandom() {
        return new XoRoShiRoRandom();
    }

    /**
     * @reason Avoid allocations, branch code out, early-skip some code
     * @author JellySquid
     */
    @Overwrite
    public void randomBlockDisplayTick(int xCenter, int yCenter, int zCenter, int radius, Random random, boolean spawnBarrierParticles, BlockPos.Mutable pos) {
        int x = xCenter + (random.nextInt(radius) - random.nextInt(radius));
        int y = yCenter + (random.nextInt(radius) - random.nextInt(radius));
        int z = zCenter + (random.nextInt(radius) - random.nextInt(radius));

        pos.set(x, y, z);

        BlockState blockState = this.getBlockState(pos);

        if (!blockState.isAir()) {
            blockState.getBlock().randomDisplayTick(blockState, this, pos, random);
            this.performBarrierTick(blockState, pos, spawnBarrierParticles);
        }

        if (!blockState.isFullCube(this, pos)) {
            this.performBiomeParticleDisplayTick(pos, random);
        }

        FluidState fluidState = blockState.getFluidState();

        if (!fluidState.isEmpty()) {
            fluidState.randomDisplayTick(this, pos, random);
            this.performFluidParticles(blockState, fluidState, pos, random);
        }
    }

    private void performBarrierTick(BlockState blockState, BlockPos pos, boolean spawnBarrierParticles) {
        if (spawnBarrierParticles && blockState.isOf(Blocks.BARRIER)) {
            this.performBarrierDisplayTick(pos);
        }
    }

    private void performBarrierDisplayTick(BlockPos pos) {
        this.addParticle(ParticleTypes.BARRIER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                0.0D, 0.0D, 0.0D);
    }

    private void performBiomeParticleDisplayTick(BlockPos pos, Random random) {
        BiomeParticleConfig config = this.getBiome(pos)
                .getParticleConfig()
                .orElse(null);

        if (config != null && config.shouldAddParticle(random)) {
            this.addParticle(config.getParticle(),
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    private void performFluidParticles(BlockState blockState, FluidState fluidState, BlockPos.Mutable pos, Random random) {
        ParticleEffect particleEffect = fluidState.getParticle();

        if (particleEffect != null && random.nextInt(10) == 0) {
            boolean solid = blockState.isSideSolidFullSquare(this, pos, Direction.DOWN);

            pos.setY(pos.getY() - 1);

            this.addParticle(pos, this.getBlockState(pos), particleEffect, solid);
        }
    }
}
