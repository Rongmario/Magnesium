package me.jellysquid.mods.sodium.compat.client.renderer;

import net.minecraft.client.renderer.GlStateManager;

public class CompatRenderSystem {
    public static void enableBlend(){
        GlStateManager.enableBlend();
    }
    public static void disableTexture(){
        GlStateManager.disableTexture2D();
    }
    public static void blendFuncSeparate(GlStateManager.SourceFactor p_blendFuncSeparate_0_, GlStateManager.DestFactor p_blendFuncSeparate_1_, GlStateManager.SourceFactor p_blendFuncSeparate_2_, GlStateManager.DestFactor p_blendFuncSeparate_3_) {
        GlStateManager.tryBlendFuncSeparate(p_blendFuncSeparate_0_.factor, p_blendFuncSeparate_1_.factor, p_blendFuncSeparate_2_.factor, p_blendFuncSeparate_3_.factor);
    }
    public static void defaultBlendFunc(){
        blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }
    public static void disableBlend(){
        GlStateManager.disableBlend();
    }
    public static void enableTexture(){
        GlStateManager.enableTexture2D();
    }
}
