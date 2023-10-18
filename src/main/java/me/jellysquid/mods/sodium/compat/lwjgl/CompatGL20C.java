package me.jellysquid.mods.sodium.compat.lwjgl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class CompatGL20C {

    public static final int GL_ARRAY_BUFFER = GL15.GL_ARRAY_BUFFER;
    public static final int GL_ARRAY_BUFFER_BINDING = GL15.GL_ARRAY_BUFFER_BINDING;

    public static final int GL_STREAM_DRAW = GL15.GL_STREAM_DRAW;
    public static final int GL_STREAM_READ = GL15.GL_STREAM_READ;
    public static final int GL_STREAM_COPY = GL15.GL_STREAM_COPY;
    public static final int GL_DYNAMIC_DRAW = GL15.GL_DYNAMIC_DRAW;
    public static final int GL_DYNAMIC_READ = GL15.GL_DYNAMIC_READ;
    public static final int GL_DYNAMIC_COPY = GL15.GL_DYNAMIC_COPY;
    public static final int GL_STATIC_DRAW = GL15.GL_STATIC_DRAW;
    public static final int GL_STATIC_READ = GL15.GL_STATIC_READ;
    public static final int GL_STATIC_COPY = GL15.GL_STATIC_COPY;

    public static final int GL_LINK_STATUS = GL20.GL_LINK_STATUS;

    public static final int GL_TRUE = GL11.GL_TRUE;

    public static final int GL_COMPILE_STATUS = GL20.GL_COMPILE_STATUS;

    public static final int GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER;

    public static final int GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER;

    public static final int GL_LINES = GL11.GL_LINES;

    public static final int GL_TRIANGLES = GL11.GL_TRIANGLES;

    public static final int GL_QUADS = GL11.GL_QUADS;

    public static final int GL_FLOAT = GL11.GL_FLOAT;

    public static final int GL_UNSIGNED_SHORT = GL11.GL_UNSIGNED_SHORT;

    public static final int GL_UNSIGNED_BYTE = GL11.GL_UNSIGNED_BYTE;

    public static final int GL_VENDOR = GL11.GL_VENDOR;

    public static final int GL_VERSION = GL11.GL_VERSION;

    public static void glBufferData(int target, long data_size, int usage) {
        GL15.glBufferData(target, data_size, usage);
    }

    public static void glMultiDrawArrays(int mode, IntBuffer piFirst, IntBuffer piCount) {
        GL14.glMultiDrawArrays(mode, piFirst, piCount);
    }

    public static String glGetProgramInfoLog(int program) {
        return GL20.glGetProgramInfoLog(program, GL20.GL_INFO_LOG_LENGTH);
    }

    public static void glBindAttribLocation(int program, int index, CharSequence name) {
        GL20.glBindAttribLocation(program, index, name);
    }

    public static String glGetShaderInfoLog(int shader) {
        return GL20.glGetShaderInfoLog(shader, GL20.GL_INFO_LOG_LENGTH);
    }

    public static void nglShaderSource(int shader, ByteBuffer string) {
        GL20.glShaderSource(shader,string);
    }

    public static void glDisableVertexAttribArray(int index) {
        GL20.glDisableVertexAttribArray(index);
    }

    public static void glVertexAttrib4fv(int index, float x, float y, float z, float w) {
        GL20.glVertexAttrib4f(index, x, y, z, w);
    }

    public static void glVertexAttrib4fv(int index, FloatBuffer buffer) {
        GL20.glVertexAttribPointer(index, buffer.capacity(), false, buffer.position(), buffer);
    }

    public static void glUniform3f(int location, float v0, float v1, float v2) {
        GL20.glUniform3f(location, v0, v1, v2);
    }

    public static void glUniform2f(int location, float v0, float v1) {
        GL20.glUniform2f(location, v0, v1);
    }

    public static void glUniform4fv(int location, FloatBuffer values) {
        GL20.glUniform4(location, values);
    }

    public static void glUniform4fv(int location, float[] values) {
        glUniform4fv(location, FloatBuffer.wrap(values));
    }

    public static void glUniform1f(int location, float v0) {
        GL20.glUniform1f(location, v0);
    }
}
