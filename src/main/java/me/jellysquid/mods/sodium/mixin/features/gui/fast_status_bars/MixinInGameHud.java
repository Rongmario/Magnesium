package me.jellysquid.mods.sodium.mixin.features.gui.fast_status_bars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public abstract class MixinInGameHud extends Gui {
    @Shadow @Final protected Minecraft mc;
    private final BufferBuilder bufferBuilder = new BufferBuilder(8192);
    // It's possible for status bar rendering to be skipped
    private boolean isRenderingStatusBars;

    private EntityPlayer getCameraPlayer(){
        return (EntityPlayer) mc.getRenderViewEntity();
    }
    @Inject(method = "renderPlayerStats", at = @At("HEAD"))
    private void preRenderStatusBars(ScaledResolution p_180477_1_, CallbackInfo ci) {

        if (this.getCameraPlayer() != null) {
            this.bufferBuilder.begin(4, DefaultVertexFormats.POSITION_TEX);
            this.isRenderingStatusBars = true;
        } else {
            this.isRenderingStatusBars = false;
        }
    }

    @Redirect(method = {"renderPlayerStats", "renderMountHealth"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V"))
    private void drawTexture(GuiIngame instance, int x, int y, int textureX, int textureY, int width, int height) {
        int x1 = x + width;
        int y1 = y + height;
        float z = zLevel;
        // Default texture size is 256x256
        float u0 = textureX / 256f;
        float u1 = (textureX + width) / 256f;
        float v0 = textureY / 256f;
        float v1 = (textureY + height) / 256f;

        this.bufferBuilder.pos(x, y1, z).tex(u0, v1).endVertex();
        this.bufferBuilder.pos( x1, y1, z).tex(u1, v1).endVertex();
        this.bufferBuilder.pos( x1, y, z).tex(u1, v0).endVertex();
        this.bufferBuilder.pos( x, y, z).tex(u0, v0).endVertex();
    }

    @Inject(method = "renderPlayerStats", at = @At("RETURN"))
    private void renderStatusBars(ScaledResolution p_180477_1_, CallbackInfo ci) {
        if (this.isRenderingStatusBars) {
            this.bufferBuilder.finishDrawing();
            Tessellator.getInstance().vboUploader.draw(bufferBuilder);
        }
    }
}