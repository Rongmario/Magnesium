package me.jellysquid.mods.sodium.mixin.features.particle.cull;

import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
    /*
    @ModifyVariable(method = "renderParticles", at = @At("HEAD"))
    private void setupFrustum(Entity entity, float partialTicks, CallbackInfo ci) {
        boolean useCulling = SodiumClientMod.options().advanced.useParticleCulling;
        ClippingHelper frustum = useCulling ? SodiumWorldRenderer.getInstance().getFrustum() : null;

        instance.renderParticles(matrixStack, immediate, lightmapTextureManager, camera, f, frustum);
    }



    @ModifyVariable(method = "renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/render/Frustum;)V", at = @At("HEAD"), ordinal = 0)
    private Frustum checkOption(Frustum oldInstance) {
        boolean useCulling = SodiumClientMod.options().advanced.useParticleCulling;
        return useCulling ? oldInstance : null;
    }

     */
}
