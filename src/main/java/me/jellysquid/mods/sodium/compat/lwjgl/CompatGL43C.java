package me.jellysquid.mods.sodium.compat.lwjgl;

import org.lwjgl.opengl.GL43;

public final class CompatGL43C {
    public static void glMultiDrawArraysIndirect(int mode, long indirect_buffer_offset, int primcount, int stride){
        GL43.glMultiDrawArraysIndirect(mode,indirect_buffer_offset,primcount,stride);
    }
}
