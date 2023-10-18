package me.jellysquid.mods.sodium.mixin.core.frustum;

import me.jellysquid.mods.sodium.client.util.math.FrustumExtended;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Frustum.class)
public class MixinFrustum implements FrustumExtended {


    @Shadow
    @Final
    private ClippingHelper clippingHelper;

    @Inject(method = "setPosition", at = @At("HEAD"))
    private void prePositionUpdate(double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        ExtClippingHelper ext = (ExtClippingHelper) clippingHelper;

        ext.setX((float) cameraX);
        ext.setY((float) cameraY);
        ext.setZ((float) cameraZ);
    }

    @Override
    public boolean fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

        ExtClippingHelper ext = (ExtClippingHelper) clippingHelper;
        return clippingHelper.isBoxInFrustum(minX - ext.getX(), minY - ext.getY(), minZ - ext.getZ(),
                maxX - ext.getX(), maxY - ext.getY(), maxZ - ext.getZ());
    }

}
