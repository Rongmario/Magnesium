package me.jellysquid.mods.sodium.mixin.features.chunk_rendering;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.jellysquid.mods.sodium.client.world.ChunkStatusListener;

public interface ExtNetHandlerPlayClient {


    ChunkStatusListener getListener();

    void setListener(ChunkStatusListener listener);

    LongOpenHashSet getLoadedChunks();

}
