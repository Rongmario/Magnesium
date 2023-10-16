package me.jellysquid.mods.sodium.compat.lwjgl;

import org.lwjgl.opengl.GL33;

public final class CompatGL33C {
    public static void glVertexAttribDivisor(int index, int divisor){
        GL33.glVertexAttribDivisor(index,divisor);
    }
}
