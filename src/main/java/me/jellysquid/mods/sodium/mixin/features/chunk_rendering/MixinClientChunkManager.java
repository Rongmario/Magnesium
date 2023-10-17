package me.jellysquid.mods.sodium.mixin.features.chunk_rendering;

import it.unimi.dsi.fastutil.longs.LongIterator;
import me.jellysquid.mods.sodium.client.world.ChunkStatusListener;
import me.jellysquid.mods.sodium.client.world.ChunkStatusListenerManager;
import me.jellysquid.mods.sodium.compat.util.math.CompatChunkPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkProviderClient.class)
public abstract class MixinClientChunkManager implements ChunkStatusListenerManager, ExtChunkProviderClient {
    @Shadow
    @Final
    private Chunk blankChunk;
    private boolean needsTrackingUpdate = false;

    @Shadow
    @Nullable
    public abstract Chunk provideChunk(int x, int z);

    @Inject(method = "unloadChunk", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;remove(J)Ljava/lang/Object;", shift = At.Shift.BEFORE))
    private void afterUnloadChunk(int x, int z, CallbackInfo ci) {
        ExtNetHandlerPlayClient con = (ExtNetHandlerPlayClient) Minecraft.getMinecraft().getConnection();
        if (con.getListener() != null) {
            con.getListener().onChunkRemoved(x, z);
            con.getLoadedChunks().remove(ChunkPos.asLong(x, z));
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void afterTick(CallbackInfoReturnable<Boolean> cir) {
        if (!this.needsTrackingUpdate) {
            return;
        }
        ExtNetHandlerPlayClient con = (ExtNetHandlerPlayClient) Minecraft.getMinecraft().getConnection();
        LongIterator it = con.getLoadedChunks().iterator();

        while (it.hasNext()) {
            long pos = it.nextLong();

            int x = CompatChunkPos.getX(pos);
            int z = CompatChunkPos.getZ(pos);

            if (this.provideChunk(x, z) == blankChunk) {
                it.remove();

                if (con.getListener() != null) {
                    con.getListener().onChunkRemoved(x, z);
                }
            }
        }

        this.needsTrackingUpdate = false;
    }

    @Override
    public void setNeedsTrackingUpdate(boolean needsTrackingUpdate) {
        this.needsTrackingUpdate = needsTrackingUpdate;
    }

    @Override
    public boolean needsTrackingUpdate() {
        return false;
    }


    @Override
    public void setListener(ChunkStatusListener listener) {
        ExtNetHandlerPlayClient ext = (ExtNetHandlerPlayClient) Minecraft.getMinecraft().getConnection();
        ext.setListener(listener);
    }
}
