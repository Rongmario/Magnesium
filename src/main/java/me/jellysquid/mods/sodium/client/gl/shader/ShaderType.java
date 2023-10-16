package me.jellysquid.mods.sodium.client.gl.shader;

import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL20C;

/**
 * An enumeration over the supported OpenGL shader types.
 */
public enum ShaderType {
    VERTEX(CompatGL20C.GL_VERTEX_SHADER),
    FRAGMENT(CompatGL20C.GL_FRAGMENT_SHADER);

    public final int id;

    ShaderType(int id) {
        this.id = id;
    }
}
