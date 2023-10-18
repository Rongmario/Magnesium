package me.jellysquid.mods.sodium.mixin.features.render_layer.leaves;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockLeaves.class)
public class MixinLeavesBlock extends Block {
    public MixinLeavesBlock() {
        super(Material.AIR);
        throw new AssertionError("Mixin constructor called!");
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess stateFrom, BlockPos pos, EnumFacing direction) {
        if (SodiumClientMod.options().quality.leavesQuality.isFancy(Minecraft.getMinecraft().gameSettings)) {
            return super.shouldCheckWeakPower(state, stateFrom, pos, direction);
        } else {
            return stateFrom.getBlockState(pos.offset(direction)).getBlock() instanceof BlockLeaves || super.shouldSideBeRendered(state, stateFrom, pos, direction);
        }
    }

}