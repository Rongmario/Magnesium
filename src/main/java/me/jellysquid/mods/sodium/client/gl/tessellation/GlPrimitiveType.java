package me.jellysquid.mods.sodium.client.gl.tessellation;

import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL20C;

public enum GlPrimitiveType {
    LINES(CompatGL20C.GL_LINES),
    TRIANGLES(CompatGL20C.GL_TRIANGLES),
    QUADS(CompatGL20C.GL_QUADS);

    private final int id;

    GlPrimitiveType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
