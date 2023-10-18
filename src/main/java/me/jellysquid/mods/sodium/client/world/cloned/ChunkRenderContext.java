package me.jellysquid.mods.sodium.client.world.cloned;

import me.jellysquid.mods.sodium.compat.util.math.ChunkSectionPos;
import net.minecraft.util.math.AxisAlignedBB;

public class ChunkRenderContext {
    private final ChunkSectionPos origin;
    private final ClonedChunkSection[] sections;
    private final AxisAlignedBB volume;

    public ChunkRenderContext(ChunkSectionPos origin, ClonedChunkSection[] sections, AxisAlignedBB volume) {
        this.origin = origin;
        this.sections = sections;
        this.volume = volume;
    }

    public ClonedChunkSection[] getSections() {
        return this.sections;
    }

    public ChunkSectionPos getOrigin() {
        return this.origin;
    }

    public AxisAlignedBB getVolume() {
        return this.volume;
    }

    public void releaseResources() {
        for (ClonedChunkSection section : sections) {
            if (section != null) {
                section.getBackingCache()
                        .release(section);
            }
        }
    }
}
