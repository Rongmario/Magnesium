package me.jellysquid.mods.sodium.client.gl.buffer;

import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL20C;


public enum GlBufferUsage {
    GL_STREAM_DRAW(CompatGL20C.GL_STREAM_DRAW),
    GL_STREAM_READ(CompatGL20C.GL_STREAM_READ),
    GL_STREAM_COPY(CompatGL20C.GL_STREAM_COPY),
    GL_STATIC_DRAW(CompatGL20C.GL_STATIC_DRAW),
    GL_STATIC_READ(CompatGL20C.GL_STATIC_READ),
    GL_STATIC_COPY(CompatGL20C.GL_STATIC_COPY),
    GL_DYNAMIC_DRAW(CompatGL20C.GL_DYNAMIC_DRAW),
    GL_DYNAMIC_READ(CompatGL20C.GL_DYNAMIC_READ),
    GL_DYNAMIC_COPY(CompatGL20C.GL_DYNAMIC_COPY);

    private final int id;

    GlBufferUsage(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
