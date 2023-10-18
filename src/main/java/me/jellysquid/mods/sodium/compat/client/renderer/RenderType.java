package me.jellysquid.mods.sodium.compat.client.renderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderType {

    public static final RenderType SOLID = new RenderType(2097152);

    public static final RenderType CUTOUT = new RenderType(131072);

    public static final RenderType CUTOUT_MIPPED = new RenderType(131072);

    private final int bufferSize;

    public RenderType(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getExpectedBufferSize() {
        return bufferSize;
    }
}
