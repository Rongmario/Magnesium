package me.jellysquid.mods.sodium.client.gl.state;

import me.jellysquid.mods.sodium.client.gl.array.GlVertexArray;
import me.jellysquid.mods.sodium.client.gl.buffer.GlBuffer;
import me.jellysquid.mods.sodium.client.gl.buffer.GlBufferTarget;
import me.jellysquid.mods.sodium.compat.client.renderer.CompatGlStateManager;
import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL30C;
import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL31C;

import java.util.Arrays;

public class GlStateTracker {
    private static final int UNASSIGNED_HANDLE = -1;

    private final int[] bufferState = new int[GlBufferTarget.COUNT];
    private final int[] bufferRestoreState = new int[GlBufferTarget.COUNT];

    private int vertexArrayState;
    private int vertexArrayRestoreState;

    public GlStateTracker() {
        this.clearRestoreState();
    }

    public boolean makeBufferActive(GlBufferTarget target, GlBuffer buffer) {
        return this.makeBufferActive(target, buffer == null ? GlBuffer.NULL_BUFFER_ID : buffer.handle());
    }

    private boolean makeBufferActive(GlBufferTarget target, int buffer) {
        int prevBuffer = this.bufferState[target.ordinal()];

        if (prevBuffer == UNASSIGNED_HANDLE) {
            this.bufferRestoreState[target.ordinal()] = CompatGlStateManager.getInteger(target.getBindingParameter());
        }

        this.bufferState[target.ordinal()] = buffer;

        return prevBuffer != buffer;
    }

    public boolean makeVertexArrayActive(GlVertexArray array) {
        return this.makeVertexArrayActive(array == null ? GlVertexArray.NULL_ARRAY_ID : array.handle());
    }

    private boolean makeVertexArrayActive(int array) {
        int prevArray = this.vertexArrayState;

        if (prevArray == UNASSIGNED_HANDLE) {
            this.vertexArrayRestoreState = CompatGlStateManager.getInteger(CompatGL31C.GL_VERTEX_ARRAY_BINDING);
        }

        this.vertexArrayState = array;

        return prevArray != array;
    }

    public void applyRestoreState() {
        for (int i = 0; i < GlBufferTarget.COUNT; i++) {
            if (this.bufferState[i] != this.bufferRestoreState[i] &&
                    this.bufferRestoreState[i] != UNASSIGNED_HANDLE) {
                CompatGlStateManager.bindBuffers(GlBufferTarget.VALUES[i].getTargetParameter(), this.bufferRestoreState[i]);
            }
        }

        if (this.vertexArrayState != this.vertexArrayRestoreState &&
                this.vertexArrayRestoreState != UNASSIGNED_HANDLE) {
            CompatGL30C.glBindVertexArray(this.vertexArrayRestoreState);
        }
    }

    public void clearRestoreState() {
        Arrays.fill(this.bufferState, UNASSIGNED_HANDLE);
        Arrays.fill(this.bufferRestoreState, UNASSIGNED_HANDLE);

        this.vertexArrayState = UNASSIGNED_HANDLE;
        this.vertexArrayRestoreState = UNASSIGNED_HANDLE;
    }
}
