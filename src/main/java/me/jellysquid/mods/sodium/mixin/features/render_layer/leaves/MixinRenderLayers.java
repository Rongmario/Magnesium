package me.jellysquid.mods.sodium.mixin.features.render_layer.leaves;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.settings.GameSettings;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameSettings.class)
public class MixinRenderLayers {

    @Redirect(method = "setOptionValue", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;fancyGraphics:Z", opcode = Opcodes.PUTFIELD))
    private static void onSetFancyGraphicsOrBetter(GameSettings instance, boolean value) {
        instance.fancyGraphics = SodiumClientMod.options().quality.leavesQuality.isFancy(instance);
    }

    @Redirect(method = "loadOptions", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;fancyGraphics:Z", opcode = Opcodes.PUTFIELD))
    private static void onSetFancyGraphicsOrBetter_2(GameSettings instance, boolean value) {
        instance.fancyGraphics = SodiumClientMod.options().quality.leavesQuality.isFancy(instance);
    }
}
