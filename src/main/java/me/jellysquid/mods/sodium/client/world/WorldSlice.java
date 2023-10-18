package me.jellysquid.mods.sodium.client.world;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.jellysquid.mods.sodium.client.world.biome.BiomeCache;
import me.jellysquid.mods.sodium.client.world.biome.BiomeColorCache;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import me.jellysquid.mods.sodium.client.world.cloned.ClonedChunkSection;
import me.jellysquid.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import me.jellysquid.mods.sodium.client.world.cloned.PackedIntegerArrayExtended;
import me.jellysquid.mods.sodium.client.world.cloned.palette.ClonedPalette;
import me.jellysquid.mods.sodium.compat.util.math.ChunkSectionPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BitArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.level.ColorResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Takes a slice of world state (block states, biome and light data arrays) and copies the data for use in off-thread
 * operations. This allows chunk build tasks to see a consistent snapshot of chunk data at the exact moment the task was
 * created.
 * <p>
 * World slices are not safe to use from multiple threads at once, but the data they contain is safe from modification
 * by the main client thread.
 * <p>
 * Object pooling should be used to avoid huge allocations as this class contains many large arrays.
 */
public class WorldSlice implements IBlockAccess {
    // The number of blocks on each axis in a section.
    private static final int SECTION_BLOCK_LENGTH = 16;

    // The number of blocks in a section.
    private static final int SECTION_BLOCK_COUNT = SECTION_BLOCK_LENGTH * SECTION_BLOCK_LENGTH * SECTION_BLOCK_LENGTH;

    // The radius of blocks around the origin chunk that should be copied.
    private static final int NEIGHBOR_BLOCK_RADIUS = 2;

    // The radius of chunks around the origin chunk that should be copied.
    private static final int NEIGHBOR_CHUNK_RADIUS = MathHelper.roundUpToMultiple(NEIGHBOR_BLOCK_RADIUS, 16) >> 4;

    // The number of sections on each axis of this slice.
    private static final int SECTION_LENGTH = 1 + (NEIGHBOR_CHUNK_RADIUS * 2);

    // The size of the lookup tables used for mapping values to coordinate int pairs. The lookup table size is always
    // a power of two so that multiplications can be replaced with simple bit shifts in hot code paths.
    private static final int TABLE_LENGTH = MathHelper.smallestEncompassingPowerOfTwo(SECTION_LENGTH);

    // The number of bits needed for each X/Y/Z component in a lookup table.
    private static final int TABLE_BITS = Integer.bitCount(TABLE_LENGTH - 1);

    // The array size for the section lookup table.
    private static final int SECTION_TABLE_ARRAY_SIZE = TABLE_LENGTH * TABLE_LENGTH * TABLE_LENGTH;

    // Fallback BlockState to use if none were available in the array
    private static final IBlockState NULL_BLOCK_STATE = Blocks.AIR.getDefaultState();

    // The world this slice has copied data from
    private final World world;

    // Local Section->BlockState table.
    private final IBlockState[][] blockStatesArrays;
    // The biome blend caches for each color resolver type
    // This map is always re-initialized, but the caches themselves are taken from an object pool
    private final Map<ColorResolver, BiomeColorCache> biomeColorCaches = new Reference2ObjectOpenHashMap<>();
    // Local section copies. Read-only.
    private ClonedChunkSection[] sections;
    // Biome caches for each chunk section
    private final BiomeCache[] biomeCaches;
    // The previously accessed and cached color resolver, used in conjunction with the cached color cache field
    private ColorResolver prevColorResolver;

    // The cached lookup result for the previously accessed color resolver to avoid excess hash table accesses
    // for vertex color blending
    private BiomeColorCache prevColorCache;

    // The starting point from which this slice captures blocks
    private int baseX, baseY, baseZ;

    // The chunk origin of this slice
    private ChunkSectionPos origin;

    public WorldSlice(World world) {
        this.world = world;

        this.sections = new ClonedChunkSection[SECTION_TABLE_ARRAY_SIZE];
        this.blockStatesArrays = new IBlockState[SECTION_TABLE_ARRAY_SIZE][];
        this.biomeCaches = new BiomeCache[SECTION_TABLE_ARRAY_SIZE];

        for (int x = 0; x < SECTION_LENGTH; x++) {
            for (int y = 0; y < SECTION_LENGTH; y++) {
                for (int z = 0; z < SECTION_LENGTH; z++) {
                    int i = getLocalSectionIndex(x, y, z);

                    this.blockStatesArrays[i] = new IBlockState[SECTION_BLOCK_COUNT];
                    this.biomeCaches[i] = new BiomeCache(this.world);
                }
            }
        }
    }

    public static ChunkRenderContext prepare(World world, ChunkSectionPos origin, ClonedChunkSectionCache sectionCache) {
        Chunk chunk = world.getChunk(origin.getX(), origin.getZ());
        ExtendedBlockStorage section = chunk.getBlockStorageArray()[origin.getY()];


        // If the chunk section is absent or empty, simply terminate now. There will never be anything in this chunk
        // section to render, so we need to signal that a chunk render task shouldn't created. This saves a considerable
        // amount of time in queueing instant build tasks and greatly accelerates how quickly the world can be loaded.
        if (section.isEmpty()) {
            return null;
        }

        AxisAlignedBB volume = new AxisAlignedBB(origin.getMinX() - NEIGHBOR_BLOCK_RADIUS,
                origin.getMinY() - NEIGHBOR_BLOCK_RADIUS,
                origin.getMinZ() - NEIGHBOR_BLOCK_RADIUS,
                origin.getMaxX() + NEIGHBOR_BLOCK_RADIUS,
                origin.getMaxY() + NEIGHBOR_BLOCK_RADIUS,
                origin.getMaxZ() + NEIGHBOR_BLOCK_RADIUS);

        // The min/max bounds of the chunks copied by this slice
        final int minChunkX = origin.getX() - NEIGHBOR_CHUNK_RADIUS;
        final int minChunkY = origin.getY() - NEIGHBOR_CHUNK_RADIUS;
        final int minChunkZ = origin.getZ() - NEIGHBOR_CHUNK_RADIUS;

        final int maxChunkX = origin.getX() + NEIGHBOR_CHUNK_RADIUS;
        final int maxChunkY = origin.getY() + NEIGHBOR_CHUNK_RADIUS;
        final int maxChunkZ = origin.getZ() + NEIGHBOR_CHUNK_RADIUS;

        ClonedChunkSection[] sections = new ClonedChunkSection[SECTION_TABLE_ARRAY_SIZE];

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
                    sections[getLocalSectionIndex(chunkX - minChunkX, chunkY - minChunkY, chunkZ - minChunkZ)] =
                            sectionCache.acquire(chunkX, chunkY, chunkZ);
                }
            }
        }

        return new ChunkRenderContext(origin, sections, volume);
    }

    /**
     * Helper function to ensure a valid BlockState is always returned (air is returned
     * in place of null).
     */
    private static IBlockState nullableState(IBlockState state) {
        if (state != null) {
            return state;
        } else
            return NULL_BLOCK_STATE;
    }

    // [VanillaCopy] PalettedContainer#toIndex
    public static int getLocalBlockIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    public static int getLocalSectionIndex(int x, int y, int z) {
        return y << TABLE_BITS << TABLE_BITS | z << TABLE_BITS | x;
    }

    public static int getLocalChunkIndex(int x, int z) {
        return z << TABLE_BITS | x;
    }

    public void copyData(ChunkRenderContext context) {
        this.origin = context.getOrigin();
        this.sections = context.getSections();

        this.prevColorCache = null;
        this.prevColorResolver = null;

        this.biomeColorCaches.clear();

        this.baseX = (this.origin.getX() - NEIGHBOR_CHUNK_RADIUS) << 4;
        this.baseY = (this.origin.getY() - NEIGHBOR_CHUNK_RADIUS) << 4;
        this.baseZ = (this.origin.getZ() - NEIGHBOR_CHUNK_RADIUS) << 4;

        for (int x = 0; x < SECTION_LENGTH; x++) {
            for (int y = 0; y < SECTION_LENGTH; y++) {
                for (int z = 0; z < SECTION_LENGTH; z++) {
                    int idx = getLocalSectionIndex(x, y, z);

                    this.biomeCaches[idx].reset();

                    this.unpackBlockData(this.blockStatesArrays[idx], this.sections[idx], context.getVolume());
                }
            }
        }
    }

    private void unpackBlockData(IBlockState[] states, ClonedChunkSection section, AxisAlignedBB box) {
        if (this.origin.equals(section.getPosition())) {
            this.unpackBlockDataZ(states, section);
        } else {
            this.unpackBlockDataR(states, section, box);
        }
    }

    private void unpackBlockDataR(IBlockState[] states, ClonedChunkSection section, AxisAlignedBB box) {
        BitArray intArray = section.getBlockData();
        ClonedPalette<IBlockState> palette = section.getBlockPalette();

        ChunkSectionPos pos = section.getPosition();

        int minBlockX = (int) Math.max(box.minX, pos.getMinX());
        int maxBlockX = (int) Math.min(box.maxX, pos.getMaxX());

        int minBlockY = (int) Math.max(box.minY, pos.getMinY());
        int maxBlockY = (int) Math.min(box.maxY, pos.getMaxY());

        int minBlockZ = (int) Math.max(box.minZ, pos.getMinZ());
        int maxBlockZ = (int) Math.min(box.maxZ, pos.getMaxZ());

        for (int y = minBlockY; y <= maxBlockY; y++) {
            for (int z = minBlockZ; z <= maxBlockZ; z++) {
                for (int x = minBlockX; x <= maxBlockX; x++) {
                    int blockIdx = getLocalBlockIndex(x & 15, y & 15, z & 15);
                    int value = intArray.getAt(blockIdx);

                    states[blockIdx] = palette.get(value);
                }
            }
        }
    }

    private void unpackBlockDataZ(IBlockState[] states, ClonedChunkSection section) {
        ((PackedIntegerArrayExtended) section.getBlockData())
                .copyUsingPalette(states, section.getBlockPalette());
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return this.getBlockEntity(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return false;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return null;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return null;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return false;
    }

    public IBlockState getBlockState(int x, int y, int z) {
        int relX = x - this.baseX;
        int relY = y - this.baseY;
        int relZ = z - this.baseZ;

        return nullableState(this.blockStatesArrays[getLocalSectionIndex(relX >> 4, relY >> 4, relZ >> 4)]
                [getLocalBlockIndex(relX & 15, relY & 15, relZ & 15)]);
    }

    public IBlockState getBlockStateRelative(int x, int y, int z) {
        return nullableState(this.blockStatesArrays[getLocalSectionIndex(x >> 4, y >> 4, z >> 4)]
                [getLocalBlockIndex(x & 15, y & 15, z & 15)]);
    }

    public TileEntity getBlockEntity(BlockPos pos) {
        return this.getBlockEntity(pos.getX(), pos.getY(), pos.getZ());
    }

    public TileEntity getBlockEntity(int x, int y, int z) {
        int relX = x - this.baseX;
        int relY = y - this.baseY;
        int relZ = z - this.baseZ;

        return this.sections[getLocalSectionIndex(relX >> 4, relY >> 4, relZ >> 4)]
                .getBlockEntity(relX & 15, relY & 15, relZ & 15);
    }

    public int getBaseLightLevel(BlockPos pos, int ambientDarkness) {
        return 0;
    }

    public boolean isSkyVisible(BlockPos pos) {
        return false;
    }

    /**
     * Gets or computes the biome at the given global coordinates.
     */
    public Biome getBiome(int x, int y, int z) {
        int relX = x - this.baseX;
        int relY = y - this.baseY;
        int relZ = z - this.baseZ;

        int index = getLocalSectionIndex(relX >> 4, relY >> 4, relZ >> 4);

        index = index >= biomeCaches.length ? biomeCaches.length - 1 : index;

        BiomeCache cache = this.biomeCaches[index];
        return cache != null ? cache
                .getBiome(x, relY >> 4, z) : Minecraft.getMinecraft().world.getBiome(new BlockPos(x, y, z));
    }

    public ChunkSectionPos getOrigin() {
        return this.origin;
    }
}
