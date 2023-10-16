package me.jellysquid.mods.sodium.client.gl.func;

import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL33C;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.ContextCapabilities;

public enum GlInstancedArrayFunctions {
    CORE {
        @Override
        public void glVertexAttribDivisor(int index, int divisor) {
            CompatGL33C.glVertexAttribDivisor(index, divisor);
        }
    },
    ARB {
        @Override
        public void glVertexAttribDivisor(int index, int divisor) {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, divisor);
        }
    },
    UNSUPPORTED {
        @Override
        public void glVertexAttribDivisor(int index, int divisor) {
            throw new UnsupportedOperationException();
        }
    };

    public static GlInstancedArrayFunctions load(ContextCapabilities capabilities) {
        if (capabilities.OpenGL33) {
            return CORE;
        } else if (capabilities.GL_ARB_instanced_arrays) {
            return ARB;
        } else {
            return UNSUPPORTED;
        }
    }

    public abstract void glVertexAttribDivisor(int index, int divisor);
}
