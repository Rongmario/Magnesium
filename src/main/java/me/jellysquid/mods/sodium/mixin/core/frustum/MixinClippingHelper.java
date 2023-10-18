package me.jellysquid.mods.sodium.mixin.core.frustum;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClippingHelperImpl.class)
public abstract class MixinClippingHelper extends ClippingHelper implements ExtClippingHelper {
    private float xF, yF, zF;
    private float nxX, nxY, nxZ, nxW;
    private float pxX, pxY, pxZ, pxW;
    private float nyX, nyY, nyZ, nyW;
    private float pyX, pyY, pyZ, pyW;
    private float nzX, nzY, nzZ, nzW;
    private float pzX, pzY, pzZ, pzW;

    @Shadow
    protected abstract void normalize(float[] p_180547_1_);

    @Override
    public float getX() {
        return xF;
    }

    @Override
    public void setX(float x) {
        xF = x;
    }

    @Override
    public float getY() {
        return yF;
    }

    @Override
    public void setY(float y) {
        yF = y;
    }

    @Override
    public float getZ() {
        return zF;
    }

    @Override
    public void setZ(float z) {
        zF = z;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void transform(CallbackInfo ci) {
        float[] afloat2 = this.frustum[0];
        afloat2[0] = this.clippingMatrix[3] - this.clippingMatrix[0];
        afloat2[1] = this.clippingMatrix[7] - this.clippingMatrix[4];
        afloat2[2] = this.clippingMatrix[11] - this.clippingMatrix[8];
        afloat2[3] = this.clippingMatrix[15] - this.clippingMatrix[12];
        this.normalize(afloat2);
        this.nxX = afloat2[0];
        this.nxY = afloat2[1];
        this.nxZ = afloat2[2];
        this.nxW = afloat2[3];
        float[] afloat3 = this.frustum[1];
        afloat3[0] = this.clippingMatrix[3] + this.clippingMatrix[0];
        afloat3[1] = this.clippingMatrix[7] + this.clippingMatrix[4];
        afloat3[2] = this.clippingMatrix[11] + this.clippingMatrix[8];
        afloat3[3] = this.clippingMatrix[15] + this.clippingMatrix[12];
        this.normalize(afloat3);
        this.pxX = afloat3[0];
        this.pxY = afloat3[1];
        this.pxZ = afloat3[2];
        this.pxW = afloat3[3];
        float[] afloat4 = this.frustum[2];
        afloat4[0] = this.clippingMatrix[3] + this.clippingMatrix[1];
        afloat4[1] = this.clippingMatrix[7] + this.clippingMatrix[5];
        afloat4[2] = this.clippingMatrix[11] + this.clippingMatrix[9];
        afloat4[3] = this.clippingMatrix[15] + this.clippingMatrix[13];
        this.normalize(afloat4);
        this.nyX = afloat4[0];
        this.nyY = afloat4[1];
        this.nyZ = afloat4[2];
        this.nyW = afloat4[3];
        float[] afloat5 = this.frustum[3];
        afloat5[0] = this.clippingMatrix[3] - this.clippingMatrix[1];
        afloat5[1] = this.clippingMatrix[7] - this.clippingMatrix[5];
        afloat5[2] = this.clippingMatrix[11] - this.clippingMatrix[9];
        afloat5[3] = this.clippingMatrix[15] - this.clippingMatrix[13];
        this.normalize(afloat5);
        this.pyX = afloat5[0];
        this.pyY = afloat5[1];
        this.pyZ = afloat5[2];
        this.pyW = afloat5[3];
        float[] afloat6 = this.frustum[4];
        afloat6[0] = this.clippingMatrix[3] - this.clippingMatrix[2];
        afloat6[1] = this.clippingMatrix[7] - this.clippingMatrix[6];
        afloat6[2] = this.clippingMatrix[11] - this.clippingMatrix[10];
        afloat6[3] = this.clippingMatrix[15] - this.clippingMatrix[14];
        this.normalize(afloat6);
        this.nzX = afloat6[0];
        this.nzY = afloat6[1];
        this.nzZ = afloat6[2];
        this.nzW = afloat6[3];
        float[] afloat7 = this.frustum[5];
        afloat7[0] = this.clippingMatrix[3] + this.clippingMatrix[2];
        afloat7[1] = this.clippingMatrix[7] + this.clippingMatrix[6];
        afloat7[2] = this.clippingMatrix[11] + this.clippingMatrix[10];
        afloat7[3] = this.clippingMatrix[15] + this.clippingMatrix[14];
        this.normalize(afloat7);
        this.pzX = afloat7[0];
        this.pzY = afloat7[1];
        this.pzZ = afloat7[2];
        this.pzW = afloat7[3];
    }


    /**
     * @author JellySquid
     * @reason Optimize away object allocations and for-loop
     */
    @Override
    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.nxX * (this.nxX < 0 ? minX : maxX) + this.nxY * (this.nxY < 0 ? minY : maxY) + this.nxZ * (this.nxZ < 0 ? minZ : maxZ) >= -this.nxW &&
                this.pxX * (this.pxX < 0 ? minX : maxX) + this.pxY * (this.pxY < 0 ? minY : maxY) + this.pxZ * (this.pxZ < 0 ? minZ : maxZ) >= -this.pxW &&
                this.nyX * (this.nyX < 0 ? minX : maxX) + this.nyY * (this.nyY < 0 ? minY : maxY) + this.nyZ * (this.nyZ < 0 ? minZ : maxZ) >= -this.nyW &&
                this.pyX * (this.pyX < 0 ? minX : maxX) + this.pyY * (this.pyY < 0 ? minY : maxY) + this.pyZ * (this.pyZ < 0 ? minZ : maxZ) >= -this.pyW &&
                this.nzX * (this.nzX < 0 ? minX : maxX) + this.nzY * (this.nzY < 0 ? minY : maxY) + this.nzZ * (this.nzZ < 0 ? minZ : maxZ) >= -this.nzW &&
                this.pzX * (this.pzX < 0 ? minX : maxX) + this.pzY * (this.pzY < 0 ? minY : maxY) + this.pzZ * (this.pzZ < 0 ? minZ : maxZ) >= -this.pzW;
    }
}
