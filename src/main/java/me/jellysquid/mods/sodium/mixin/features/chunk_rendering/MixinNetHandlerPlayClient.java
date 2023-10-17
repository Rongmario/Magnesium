package me.jellysquid.mods.sodium.mixin.features.chunk_rendering;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.jellysquid.mods.sodium.client.world.ChunkStatusListener;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements ExtNetHandlerPlayClient {
    private final LongOpenHashSet loadedChunks = new LongOpenHashSet();
    private ChunkStatusListener listener;

    @Inject(method = "handleChunkData", at = @At(value = "TAIL"))
    private void afterLoadChunkFromPacket(SPacketChunkData packet, CallbackInfo ci) {
        if (this.listener != null) {
            this.listener.onChunkAdded(packet.getChunkX(), packet.getChunkZ());

            this.loadedChunks.add(ChunkPos.asLong(packet.getChunkX(), packet.getChunkZ()));
        }
    }

    @Override
    public ChunkStatusListener getListener() {
        return listener;
    }

    @Override
    public void setListener(ChunkStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public LongOpenHashSet getLoadedChunks() {
        return loadedChunks;
    }

}
