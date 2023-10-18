package me.jellysquid.mods.sodium.mixin.core.pipeline;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.gl.attribute.BufferVertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.VertexDrain;
import me.jellysquid.mods.sodium.client.model.vertex.VertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.type.BlittableVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.type.VertexType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.*;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements VertexBufferView, VertexDrain {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    private IntBuffer rawIntBuffer;

    @Shadow
    private int vertexCount;


    @Shadow
    private VertexFormat vertexFormat;

    @Shadow
    private ByteBuffer byteBuffer;

    @Shadow
    private FloatBuffer rawFloatBuffer;

    @Shadow
    private ShortBuffer rawShortBuffer;

    @Redirect(method = "getVertexState", at = @At(value = "INVOKE", target = "Ljava/nio/IntBuffer;limit(I)Ljava/nio/Buffer;"))
    public Buffer debugGetNextBuffer(IntBuffer instance, int newLimit) {
        ensureBufferCapacity(newLimit);
        this.rawIntBuffer = instance;
        instance.limit(newLimit);
        return instance;
    }

    @Override
    public boolean ensureBufferCapacity(int bytes) {
        if (vertexFormat == null)
            return false;

        // Ensure that there is always space for 1 more vertex; see BufferBuilder.next()
        bytes += vertexFormat.getSize();

        if (this.vertexCount * this.vertexFormat.getSize() + bytes <= this.byteBuffer.capacity()) {
            return false;
        }

        int newSize = this.byteBuffer.capacity() + MathHelper.roundUp(bytes, 2097152);

        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", this.byteBuffer.capacity(), newSize);

        this.byteBuffer.position(0);


        ByteBuffer byteBuffer = GLAllocation.createDirectByteBuffer(newSize);
        byteBuffer.put(this.byteBuffer);
        byteBuffer.rewind();

        this.byteBuffer = byteBuffer;
        this.rawIntBuffer = byteBuffer.asIntBuffer();
        this.rawFloatBuffer = byteBuffer.asFloatBuffer();
        this.rawShortBuffer = byteBuffer.asShortBuffer();
        return true;
    }

    @Override
    public ByteBuffer getDirectBuffer() {
        return this.byteBuffer;
    }

    @Override
    public int getWriterPosition() {
        return this.vertexCount * this.vertexFormat.getSize();
    }

    @Override
    public BufferVertexFormat getVertexFormat() {
        return BufferVertexFormat.from(this.vertexFormat);
    }

    @Override
    public void flush(int vertexCount, BufferVertexFormat format) {
        if (BufferVertexFormat.from(this.vertexFormat) != format) {
            throw new IllegalStateException("Mis-matched vertex format (expected: [" + format + "], currently using: [" + this.vertexFormat + "])");
        }

        this.vertexCount += vertexCount;
    }

    @Override
    public <T extends VertexSink> T createSink(VertexType<T> factory) {
        BlittableVertexType<T> blittable = factory.asBlittable();

        if (blittable != null && blittable.getBufferVertexFormat() == this.getVertexFormat()) {
            return blittable.createBufferWriter(this, SodiumClientMod.isDirectMemoryAccessEnabled());
        }

        return factory.createFallbackWriter((BufferBuilder) (Object) this);
    }
}
