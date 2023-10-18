package me.jellysquid.mods.sodium.client.world.cloned;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.jellysquid.mods.sodium.client.world.cloned.palette.ClonedPalette;
import me.jellysquid.mods.sodium.client.world.cloned.palette.ClonedPalleteArray;
import me.jellysquid.mods.sodium.compat.util.math.ChunkSectionPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BitArray;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClonedChunkSection {
    private static final EnumSkyBlock[] LIGHT_TYPES = EnumSkyBlock.values();
    private static final ExtendedBlockStorage EMPTY_SECTION = new ExtendedBlockStorage(0, true);

    private final AtomicInteger referenceCount = new AtomicInteger(0);
    private final ClonedChunkSectionCache backingCache;

    private final Short2ObjectMap<TileEntity> blockEntities;
    private final NibbleArray[] lightDataArrays;
    private final World world;

    private ChunkSectionPos pos;

    private BitArray blockStateData;
    private ClonedPalette<IBlockState> blockStatePalette;

    private byte[] biomeData;

    ClonedChunkSection(ClonedChunkSectionCache backingCache, World world) {
        this.backingCache = backingCache;
        this.world = world;
        this.blockEntities = new Short2ObjectOpenHashMap<>();
        this.lightDataArrays = new NibbleArray[LIGHT_TYPES.length];
    }

    private static ClonedPalette<IBlockState> copyPalette(PalettedContainerExtended<IBlockState> container) {
        IBlockStatePalette palette = container.getPalette();

        IBlockState[] array = new IBlockState[1 << container.getPaletteSize()];

        for (int i = 0; i < array.length; i++) {
            array[i] = palette.getBlockState(i);

            if (array[i] == null) {
                break;
            }
        }

        return new ClonedPalleteArray<>(array, container.getDefaultValue());
    }

    private static BitArray copyBlockData(PalettedContainerExtended<IBlockState> container) {
        BitArray array = container.getDataArray();
        long[] storage = array.getBackingLongArray();

        return new BitArray(container.getPaletteSize(), array.size());
    }

    private static ExtendedBlockStorage getChunkSection(Chunk chunk, ChunkSectionPos pos) {
        ExtendedBlockStorage section = null;

        if (!chunk.getWorld().isOutsideBuildHeight(new BlockPos(pos.getX(), pos.getY(), pos.getZ()))) {
            section = chunk.getBlockStorageArray()[pos.getY()];
        }

        return section;
    }

    /**
     * @param x The local x-coordinate
     * @param y The local y-coordinate
     * @param z The local z-coordinate
     * @return An index which can be used to key entities or blocks within a chunk
     */
    private static short packLocal(int x, int y, int z) {
        return (short) (x << 8 | z << 4 | y);
    }

    public void init(ChunkSectionPos pos) {
        Chunk chunk = world.getChunk(pos.getX(), pos.getZ());

        if (chunk == null) {
            throw new RuntimeException("Couldn't retrieve chunk at " + pos.toChunkPos());
        }

        ExtendedBlockStorage section = getChunkSection(chunk, pos);

        if (section == Chunk.NULL_BLOCK_STORAGE /*ChunkSection.isEmpty(section)*/) {
            section = EMPTY_SECTION;
        }

        this.pos = pos;

        PalettedContainerExtended<IBlockState> container = PalettedContainerExtended.cast(section.getContainer());

        this.blockStateData = copyBlockData(container);
        this.blockStatePalette = copyPalette(container);

        for (EnumSkyBlock type : LIGHT_TYPES) {
            BlockPos blockPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());

            this.lightDataArrays[type.ordinal()] = world.getChunk(blockPos.getX(), blockPos.getZ()).getBlockStorageArray()[pos.getSectionY()].getBlockLight();
        }

        this.biomeData = chunk.getBiomeArray();

        AxisAlignedBB box = new AxisAlignedBB(pos.getMinX(), pos.getMinY(), pos.getMinZ(), pos.getMaxX(), pos.getMaxY(), pos.getMaxZ());

        this.blockEntities.clear();

        for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
            BlockPos entityPos = entry.getKey();

            if (box.contains(new Vec3d(entityPos))) {
                //this.blockEntities.put(BlockPos.asLong(entityPos.getX() & 15, entityPos.getY() & 15, entityPos.getZ() & 15), entry.getValue());
                this.blockEntities.put(ChunkSectionPos.packLocal(entityPos), entry.getValue());
            }
        }
    }

    public IBlockState getBlockState(int x, int y, int z) {
        return this.blockStatePalette.get(this.blockStateData.getAt(y << 8 | z << 4 | x));
    }

    public int getLightLevel(EnumSkyBlock type, int x, int y, int z) {
        NibbleArray array = this.lightDataArrays[type.ordinal()];

        if (array != null) {
            return array.get(x, y, z);
        }

        return 0;
    }

    public Biome getBiomeForNoiseGen(int x, int y, int z) {
        return world.getBiome(new BlockPos(x, y, z));
    }

    public TileEntity getBlockEntity(int x, int y, int z) {
        return this.blockEntities.get(packLocal(x, y, z));
    }

    public BitArray getBlockData() {
        return this.blockStateData;
    }

    public ClonedPalette<IBlockState> getBlockPalette() {
        return this.blockStatePalette;
    }

    public ChunkSectionPos getPosition() {
        return this.pos;
    }

    public void acquireReference() {
        this.referenceCount.incrementAndGet();
    }

    public boolean releaseReference() {
        return this.referenceCount.decrementAndGet() <= 0;
    }

    public ClonedChunkSectionCache getBackingCache() {
        return this.backingCache;
    }
}
