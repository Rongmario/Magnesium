package me.jellysquid.mods.sodium.mixin.core.model;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.sodium.client.world.biome.ItemColorsExtended;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemColors.class)
public class MixinItemColors implements ItemColorsExtended {
    private static final IItemColor DEFAULT_PROVIDER = (stack, tintIdx) -> -1;
    private Reference2ReferenceMap<IRegistryDelegate<Item>, IItemColor> itemsToColor;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        this.itemsToColor = new Reference2ReferenceOpenHashMap<>();
        this.itemsToColor.defaultReturnValue(DEFAULT_PROVIDER);
    }

    @Inject(method = "registerItemColorHandler(Lnet/minecraft/client/renderer/color/IItemColor;[Lnet/minecraft/item/Item;)V", at = @At("HEAD"))
    private void preRegisterColor(IItemColor mapper, Item[] convertibles, CallbackInfo ci) {
        for (Item convertible : convertibles) {
            this.itemsToColor.put(convertible.delegate, mapper);
        }
    }

    @Override
    public IItemColor getColorProvider(ItemStack stack) {
        return this.itemsToColor.get(stack.getItem().delegate);
    }
}
