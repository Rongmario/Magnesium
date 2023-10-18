package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.render.texture.SpriteExtended;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextureAtlasSprite.class)
public abstract class MixinSprite implements SpriteExtended {
    private boolean forceNextUpdate;

    @Shadow
    private int tickCounter;

    @Shadow
    @Final
    private AnimationMetadataSection animationMetadata;

    @Shadow
    private int frameCounter;
    @Shadow
    @Final
    private int[][] interpolatedFrameData;

    @Shadow
    public abstract int getFrameCount();


    @Shadow
    public abstract int[][] getFrameTextureData(int index);

    @Shadow
    public abstract int getIconWidth();

    @Shadow
    public abstract int getIconHeight();

    @Shadow
    public abstract int getOriginX();

    @Shadow
    public abstract int getOriginY();

    @Shadow
    protected abstract void updateAnimationInterpolated();

    /**
     * @author JellySquid
     * @reason Allow conditional texture updating
     */
    @Overwrite
    public void updateAnimation() {
        this.tickCounter++;

        boolean onDemand = SodiumClientMod.options().advanced.animateOnlyVisibleTextures;

        if (!onDemand || this.forceNextUpdate) {
            this.uploadTexture();
        }
    }

    private void uploadTexture() {
        if (this.tickCounter >= this.animationMetadata.getFrameTime()) {
            int prevFrameIndex = this.animationMetadata.getFrameIndex(this.frameCounter);
            int frameCount = this.animationMetadata.getFrameCount() == 0 ? this.getFrameCount() : this.animationMetadata.getFrameCount();

            this.frameCounter = (this.frameCounter + 1) % frameCount;
            this.tickCounter = 0;

            int frameIndex = this.animationMetadata.getFrameIndex(this.frameCounter);

            if (prevFrameIndex != frameIndex && frameIndex >= 0 && frameIndex < this.getFrameCount()) {
                TextureUtil.uploadTextureMipmap(getFrameTextureData(0), getIconWidth(), getIconHeight(), getOriginX(), getOriginY(), false, false);
            }
        } else if (this.interpolatedFrameData != null) {
            this.updateInterpolatedTexture();
            /*
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(this::updateInterpolatedTexture);
            } else {

            }
             */
        }

        this.forceNextUpdate = false;
    }

    @Override
    public void markActive() {
        this.forceNextUpdate = true;
    }

    private void updateInterpolatedTexture() {
        updateAnimationInterpolated();
    }
}
