package me.jellysquid.mods.sodium.mixin.features.chunk_rendering;

import me.jellysquid.mods.sodium.client.world.ClientWorldExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public abstract class MixinClientWorld implements ClientWorldExtended {
    private long biomeSeed;
    @Final
    @Shadow
    private Minecraft mc;

    @Shadow
    public abstract ChunkProviderClient getChunkProvider();

    @Inject(method = "refreshVisibleChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", shift = At.Shift.AFTER, ordinal = 1))
    private void updateViewCenter(CallbackInfo ci) {

        int j = MathHelper.floor(this.mc.player.posX / 16.0D);
        int k = MathHelper.floor(this.mc.player.posZ / 16.0D);

        ExtChunkProviderClient ext = (ExtChunkProviderClient) getChunkProvider();
        ext.setNeedsTrackingUpdate(true);
    }

    /**
     * Captures the biome generation seed so that our own caches can make use of it.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_, EnumDifficulty p_i45063_4_, Profiler p_i45063_5_, CallbackInfo ci) {
        this.biomeSeed = p_i45063_2_.getSeed();
    }

    @Override
    public long getBiomeSeed() {
        return this.biomeSeed;
    }
}
