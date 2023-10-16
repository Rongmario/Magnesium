package me.jellysquid.mods.sodium.client.gl.tessellation;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.buffer.GlBufferTarget;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.func.GlFunctions;
import me.jellysquid.mods.sodium.compat.client.renderer.CompatGlStateManager;

public abstract class GlAbstractTessellation implements GlTessellation {
    protected final GlPrimitiveType primitiveType;
    protected final TessellationBinding[] bindings;

    protected GlAbstractTessellation(GlPrimitiveType primitiveType, TessellationBinding[] bindings) {
        this.primitiveType = primitiveType;
        this.bindings = bindings;
    }

    @Override
    public GlPrimitiveType getPrimitiveType() {
        return this.primitiveType;
    }

    protected void bindAttributes(CommandList commandList) {
        for (TessellationBinding binding : this.bindings) {
            commandList.bindBuffer(GlBufferTarget.ARRAY_BUFFER, binding.getBuffer());

            for (GlVertexAttributeBinding attrib : binding.getAttributeBindings()) {
                CompatGlStateManager.vertexAttribPointer(attrib.getIndex(), attrib.getCount(), attrib.getFormat(), attrib.isNormalized(),
                        attrib.getStride(), attrib.getPointer());
                CompatGlStateManager.enableVertexAttribArray(attrib.getIndex());

                if (binding.isInstanced()) {
                    GlFunctions.INSTANCED_ARRAY.glVertexAttribDivisor(attrib.getIndex(), 1);
                }
            }
        }
    }
}
