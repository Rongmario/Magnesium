package me.jellysquid.mods.sodium.mixin.features.chunk_rendering;

public interface ExtChunkProviderClient {

    boolean needsTrackingUpdate();

    void setNeedsTrackingUpdate(boolean flag);
}
