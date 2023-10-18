package me.jellysquid.mods.sodium.compat.lwjgl;

import org.lwjgl.opengl.GL30;

public final class CompatGL30C {
    public static void glBindVertexArray(int array) {
        GL30.glBindVertexArray(array);
    }

    public static int glGenVertexArrays() {
        return GL30.glGenVertexArrays();
    }

    public static void glDeleteVertexArrays(int array) {
        GL30.glDeleteVertexArrays(array);
    }
}
