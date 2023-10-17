package me.jellysquid.mods.sodium.client.world.cloned;

import net.minecraft.util.BitArray;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

public interface PalettedContainerExtended<T> {



    BitArray getDataArray();

    IBlockStatePalette getPalette();

    T getDefaultValue();

    int getPaletteSize();
}
