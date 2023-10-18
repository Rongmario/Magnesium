package me.jellysquid.mods.sodium.mixin.features.world_ticking;

import me.jellysquid.mods.sodium.client.util.rand.XoRoShiRoRandom;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

// Priority for compatibility with AbnormalsCore/BetterFoliage
@Mixin(value = WorldClient.class, priority = 990)
public abstract class MixinClientWorld extends World {


    protected MixinClientWorld(ISaveHandler p_i45749_1_, WorldInfo p_i45749_2_, WorldProvider p_i45749_3_, Profiler p_i45749_4_, boolean p_i45749_5_) {
        super(p_i45749_1_, p_i45749_2_, p_i45749_3_, p_i45749_4_, p_i45749_5_);
    }


    @Redirect(method = "doVoidFogParticles", at = @At(value = "NEW", target = "()Ljava/util/Random;"))
    private Random redirectRandomTickRandom() {
        return new XoRoShiRoRandom();
    }

    /**
     * @reason Avoid allocations, branch code out, early-skip some code
     * @author JellySquid
     */
    @Overwrite
    public void showBarrierParticles(int xCenter, int yCenter, int zCenter, int radius, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {
        int x = xCenter + (random.nextInt(radius) - random.nextInt(radius));
        int y = yCenter + (random.nextInt(radius) - random.nextInt(radius));
        int z = zCenter + (random.nextInt(radius) - random.nextInt(radius));

        pos.setPos(x, y, z);

        IBlockState blockState = this.getBlockState(pos);

        if (!isAirBlock(pos)) {
            blockState.getBlock().randomDisplayTick(blockState, this, pos, random);
            this.performBarrierTick(blockState, pos, holdingBarrier);
        }

        if (!blockState.isFullCube()) {
            this.performBiomeParticleDisplayTick(pos, random);
        }

        if (blockState.getBlock() instanceof BlockLiquid) {
            BlockLiquid blockLiquid = (BlockLiquid) blockState.getBlock();
            blockLiquid.randomDisplayTick(blockState, this, pos, random);
            //this.performFluidParticles(blockState, fluidState, pos, random); //Moved to MixinBlockLiquid
        }
    }

    private void performBarrierTick(IBlockState blockState, BlockPos pos, boolean spawnBarrierParticles) {
        if (spawnBarrierParticles) {
            this.performBarrierDisplayTick(pos);
        }
    }

    private void performBarrierDisplayTick(BlockPos pos) {
        this.spawnParticle(EnumParticleTypes.BARRIER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                0.0D, 0.0D, 0.0D);
    }

    private void performBiomeParticleDisplayTick(BlockPos pos, Random random) {
        /*
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

         */
    }
    /*
    private void performFluidParticles(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        ParticleEffect particleEffect = fluidState.getParticle();
        if (particleEffect != null && random.nextInt(10) == 0) {
            boolean solid = blockState.isSideSolidFullSquare(this, pos, Direction.DOWN);

            pos.setY(pos.getY() - 1);

            this.addParticle(pos, this.getBlockState(pos), particleEffect, solid);
        }
    }

     */
}
