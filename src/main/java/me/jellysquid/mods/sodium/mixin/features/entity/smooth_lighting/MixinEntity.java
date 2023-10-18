package me.jellysquid.mods.sodium.mixin.features.entity.smooth_lighting;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.model.light.EntityLighter;
import me.jellysquid.mods.sodium.client.render.entity.EntityLightSampler;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity<T extends Entity> implements EntityLightSampler<T> {
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private void preGetLight(CallbackInfoReturnable<Float> cir) {
        // Use smooth entity lighting if enabled

        if (SodiumClientMod.options().quality.smoothLighting == SodiumGameOptions.LightingQuality.HIGH) {
            cir.setReturnValue((float) EntityLighter.getBlendedLight(this, ((T) (Object) this), 0f));
        }
    }

}
