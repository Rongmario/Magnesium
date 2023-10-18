package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class MixinSpriteBillboardParticle {
    @Shadow
    protected TextureAtlasSprite particleTexture;

    private boolean shouldTickSprite;


    @Inject(method = "setParticleTexture", at = @At("RETURN"))
    private void afterSetSprite(TextureAtlasSprite sprite, CallbackInfo ci) {

        this.shouldTickSprite = sprite != null && sprite.hasAnimationMetadata();
    }

    @Inject(method = "renderParticle", at = @At("HEAD"))
    private void buildGeometry(BufferBuilder p_180434_1_, Entity p_180434_2_, float p_180434_3_, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_, CallbackInfo ci) {
        if (this.shouldTickSprite) {
            SpriteUtil.markSpriteActive(this.particleTexture);
        }
    }

}