package me.jellysquid.mods.sodium.mixin.features.gui;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.compat.client.renderer.CompatRenderSystem;
import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL20C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL20C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiOverlayDebug.class)
public abstract class MixinDebugHud {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    @Final
    private FontRenderer fontRenderer;

    private List<String> capturedList = null;

    @Redirect(method = {"renderDebugInfoLeft", "renderDebugInfoRight"}, at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int preRenderText(List<String> list) {
        // Capture the list to be rendered later
        this.capturedList = list;

        return 0; // Prevent the rendering of any text
    }

    @Inject(method = "renderDebugInfoLeft", at = @At("RETURN"))
    public void renderLeftText(CallbackInfo ci) {
        this.renderCapturedText( false);
    }

    @Inject(method = "renderDebugInfoRight", at = @At("RETURN"))
    public void renderRightText(ScaledResolution p_175239_1_, CallbackInfo ci) {
        this.renderCapturedText( true);
    }

    private void renderCapturedText( boolean right) {
        Validate.notNull(this.capturedList, "Failed to capture string list");

        this.renderBackdrop( this.capturedList, right);
        this.renderStrings(this.capturedList, right);

        this.capturedList = null;
    }

    private void renderStrings(List<String> list, boolean right) {
        for (int i = 0; i < list.size(); ++i) {
            String string = list.get(i);

            if (!Strings.isNullOrEmpty(string)) {
                int height = 9;
                int width = this.fontRenderer.getStringWidth(string);

                float x1 = right ? this.mc.displayWidth- 2 - width : 2;
                float y1 = 2 + (height * i);

                this.fontRenderer.drawString(string, x1, y1, 0xe0e0e0, false);
            }
        }
    }

    private void renderBackdrop( List<String> list, boolean right) {
        CompatRenderSystem.enableBlend();
        CompatRenderSystem.disableTexture();
        CompatRenderSystem.defaultBlendFunc();

        int color = 0x90505050;

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(CompatGL20C.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);


        for (int i = 0; i < list.size(); ++i) {
            String string = list.get(i);

            if (Strings.isNullOrEmpty(string)) {
                continue;
            }

            int height = 9;
            int width = this.fontRenderer.getStringWidth(string);

            int x = right ? this.mc.displayWidth - 2 - width : 2;
            int y = 2 + height * i;

            float x1 = x - 1;
            float y1 = y - 1;
            float x2 = x + width + 1;
            float y2 = y + height - 1;

            bufferBuilder.pos( x1, y2, 0.0F).color(g, h, k, f).endVertex();
            bufferBuilder.pos( x2, y2, 0.0F).color(g, h, k, f).endVertex();
            bufferBuilder.pos( x2, y1, 0.0F).color(g, h, k, f).endVertex();
            bufferBuilder.pos(x1, y1, 0.0F).color(g, h, k, f).endVertex();
        }

        bufferBuilder.finishDrawing();

        Tessellator.getInstance().vboUploader.draw(bufferBuilder);
        CompatRenderSystem.enableTexture();
        CompatRenderSystem.disableBlend();
    }
}
