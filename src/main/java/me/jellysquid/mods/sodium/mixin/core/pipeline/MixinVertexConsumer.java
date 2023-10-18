package me.jellysquid.mods.sodium.mixin.core.pipeline;

import me.jellysquid.mods.sodium.client.model.vertex.VertexDrain;
import me.jellysquid.mods.sodium.client.model.vertex.VertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.type.VertexType;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public abstract class MixinVertexConsumer implements VertexDrain {
    @Override
    public <T extends VertexSink> T createSink(VertexType<T> factory) {
        return factory.createFallbackWriter((BufferBuilder)(Object) this);
    }
}
