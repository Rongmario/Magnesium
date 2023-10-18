package me.jellysquid.mods.sodium.mixin.features.gui.fast_loading_screen;

import net.minecraft.client.gui.GuiScreenWorking;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Re-implements the loading screen with considerations to reduce draw calls and other sources of overhead. This can
 * improve world load times on slower processors with very few cores.
 */
@Mixin(GuiScreenWorking.class)
public class MixinLevelLoadingScreen {
    //Does not support 1.12.2
}
