package me.jellysquid.mods.sodium.compat.client.world;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ChunkSection {

    //private static final IBlockStatePalette GLOBAL_BLOCKSTATE_PALETTE = new IdentityPalette<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState());
    private final int bottomBlockY;
    private short nonEmptyBlockCount;
    private short tickingBlockCount;
    private short tickingFluidCount;
    //private final PalettedContainer<BlockState> states;

    public ChunkSection(int p_i49943_1_) {
        this(p_i49943_1_, (short) 0, (short) 0, (short) 0);
    }

    public ChunkSection(int p_i49944_1_, short p_i49944_2_, short p_i49944_3_, short p_i49944_4_) {
        this.bottomBlockY = p_i49944_1_;
        this.nonEmptyBlockCount = p_i49944_2_;
        this.tickingBlockCount = p_i49944_3_;
        this.tickingFluidCount = p_i49944_4_;
        //   this.states = new PalettedContainer<>(GLOBAL_BLOCKSTATE_PALETTE, Block.BLOCK_STATE_REGISTRY, NBTUtil::readBlockState, NBTUtil::writeBlockState, Blocks.AIR.defaultBlockState());
    }

    public static boolean isEmpty(@Nullable ChunkSection p_222628_0_) {
        return p_222628_0_.isEmpty();
    }

    public boolean isEmpty() {
        return this.nonEmptyBlockCount == 0;
    }

    public boolean isRandomlyTicking() {
        return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
    }

    public boolean isRandomlyTickingBlocks() {
        return this.tickingBlockCount > 0;
    }

    public boolean isRandomlyTickingFluids() {
        return this.tickingFluidCount > 0;
    }

    public int bottomBlockY() {
        return this.bottomBlockY;
    }

    public void recalcBlockCounts() {
        this.nonEmptyBlockCount = 0;
        this.tickingBlockCount = 0;
        this.tickingFluidCount = 0;
        /*
        this.states.count((p_225496_1_, p_225496_2_) -> {
            FluidState fluidstate = p_225496_1_.getFluidState();
            if (!p_225496_1_.isAir()) {
                this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + p_225496_2_);
                if (p_225496_1_.isRandomlyTicking()) {
                    this.tickingBlockCount = (short)(this.tickingBlockCount + p_225496_2_);
                }
            }

            if (!fluidstate.isEmpty()) {
                this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + p_225496_2_);
                if (fluidstate.isRandomlyTicking()) {
                    this.tickingFluidCount = (short)(this.tickingFluidCount + p_225496_2_);
                }
            }

        });
         */
    }

    /*
    public PalettedContainer<BlockState> getStates() {
        return this.states;
    }


     */
    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer p_222634_1_) {
        this.nonEmptyBlockCount = p_222634_1_.readShort();
        //this.states.read(p_222634_1_);
    }

    public void write(PacketBuffer p_222630_1_) {
        p_222630_1_.writeShort(this.nonEmptyBlockCount);
        //this.states.write(p_222630_1_);
    }
    /*
    public int getSerializedSize() {
        return 2 + this.states.getSerializedSize();
    }

    public boolean maybeHas(Predicate<BlockState> p_235962_1_) {
        return this.states.maybeHas(p_235962_1_);
    }

     */
}
