package me.jellysquid.mods.sodium.mixin.features.fast_biome_colors;

import me.jellysquid.mods.sodium.client.model.quad.blender.BlockColorSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import org.spongepowered.asm.mixin.Mixin;

//Fluid does not work in 1.12.2
@Mixin(Fluid.class)
public class MixinFluid implements BlockColorSettings<IFluidBlock> {
    @Override
    public boolean useSmoothColorBlending(IBlockAccess view, IFluidBlock state, BlockPos pos) {
        return true;
    }
}