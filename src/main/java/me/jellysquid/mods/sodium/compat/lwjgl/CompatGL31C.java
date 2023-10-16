package me.jellysquid.mods.sodium.compat.lwjgl;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

public final class CompatGL31C {
    public static final int GL_COPY_READ_BUFFER = GL31.GL_COPY_READ_BUFFER;
    public static final int GL_COPY_WRITE_BUFFER = GL31.GL_COPY_WRITE_BUFFER;

    public static final int GL_VERTEX_ARRAY_BINDING = GL30.GL_VERTEX_ARRAY_BINDING;
    public static void glCopyBufferSubData(int readtarget, int writetarget, long readoffset, long writeoffset, long size){
        GL31.glCopyBufferSubData(readtarget,writetarget,readoffset,writeoffset,size);
    }
}
