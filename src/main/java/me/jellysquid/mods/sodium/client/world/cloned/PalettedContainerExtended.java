package me.jellysquid.mods.sodium.client.world.cloned;

import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.IBlockStatePalette;

public interface PalettedContainerExtended<T> {
    @SuppressWarnings("unchecked")
    static <T> PalettedContainerExtended<T> cast(T container) {
        return (PalettedContainerExtended<T>) container;
    }

    BitArray getDataArray();

    IBlockStatePalette getPalette();

    T getDefaultValue();

    int getPaletteSize();
}
