package me.jellysquid.mods.sodium.mixin.features.options;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.compat.client.CompatCloudsRenderMode;
import net.minecraft.client.settings.GameSettings;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameSettings.class)
public class MixinGameOptions {
    @Shadow
    public int renderDistanceChunks;

    @Shadow
    public boolean fancyGraphics;

    private void set(GameSettings instance) {
        SodiumGameOptions options = SodiumClientMod.options();
        if (this.renderDistanceChunks < 4 || !options.quality.enableClouds) {
            instance.clouds = CompatCloudsRenderMode.OFF.getType();
            return;
        }
        instance.clouds = options.quality.cloudQuality.isFancy(instance) ? CompatCloudsRenderMode.FANCY.getType() : CompatCloudsRenderMode.FAST.getType();
    }

    @Redirect(method = "setOptionValue", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;clouds:I", opcode = Opcodes.PUTFIELD))
    private void sustainCloudRenderMode(GameSettings instance, int value) {
        set(instance);
    }

    @Redirect(method = "loadOptions", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;clouds:I", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void loadFromSodium_1(GameSettings instance, int value) {
        set(instance);
    }

    @Redirect(method = "loadOptions", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;clouds:I", opcode = Opcodes.PUTFIELD, ordinal = 1))
    private void loadFromSodium_2(GameSettings instance, int value) {
        set(instance);
    }

    @Redirect(method = "loadOptions", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;clouds:I", opcode = Opcodes.PUTFIELD, ordinal = 2))
    private void loadFromSodium_3(GameSettings instance, int value) {
        set(instance);
    }

}
