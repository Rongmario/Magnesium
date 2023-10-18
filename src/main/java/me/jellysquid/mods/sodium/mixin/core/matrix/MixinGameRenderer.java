package me.jellysquid.mods.sodium.mixin.core.matrix;

import me.jellysquid.mods.sodium.client.render.GameRendererContext;
import me.jellysquid.mods.sodium.compat.util.math.Matrix4f;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;", shift = At.Shift.BEFORE))
    public void captureProjectionMatrix(CallbackInfo ci) {
        GameRendererContext.captureProjectionMatrix(new Matrix4f(ClippingHelperImpl.getInstance().projectionMatrix));
    }
}
