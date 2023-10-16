package me.jellysquid.mods.sodium.client.gl.func;

import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL43C;
import org.lwjgl.opengl.ARBMultiDrawIndirect;
import org.lwjgl.opengl.ContextCapabilities;

public enum GlIndirectMultiDrawFunctions {
    CORE {
        @Override
        public void glMultiDrawArraysIndirect(int mode, long indirect, int primcount, int stride) {
            CompatGL43C.glMultiDrawArraysIndirect(mode, indirect, primcount, stride);
        }
    },
    ARB {
        @Override
        public void glMultiDrawArraysIndirect(int mode, long indirect, int primcount, int stride) {
            ARBMultiDrawIndirect.glMultiDrawArraysIndirect(mode, indirect, primcount, stride);
        }
    },
    UNSUPPORTED {
        @Override
        public void glMultiDrawArraysIndirect(int mode, long indirect, int primcount, int stride) {
            throw new UnsupportedOperationException();
        }
    };

    public static GlIndirectMultiDrawFunctions load(ContextCapabilities capabilities) {
        if (capabilities.OpenGL43) {
            return CORE;
        } else if (capabilities.GL_ARB_multi_draw_indirect && capabilities.GL_ARB_draw_indirect) {
            return ARB;
        } else {
            return UNSUPPORTED;
        }
    }

    public abstract void glMultiDrawArraysIndirect(int mode, long indirect, int primcount, int stride);
}
