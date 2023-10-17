package me.jellysquid.mods.sodium.mixin.features.buffer_builder.fast_advance;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder {
    @Shadow
    private VertexFormat vertexFormat;

    @Shadow
    private VertexFormatElement vertexFormatElement;

    @Shadow
    private int vertexCount;

    @Shadow
    private int vertexFormatIndex;

    @Shadow
    public abstract boolean isColorDisabled();

    @Shadow
    public abstract BufferBuilder color(int red, int green, int blue, int alpha);

    /**
     * @author JellySquid
     * @reason Remove modulo operations and recursion
     */
    @Overwrite
    public void nextVertexFormatIndex() {
        List<VertexFormatElement> elements = this.vertexFormat.getElements();
        do {
            this.vertexCount += this.vertexFormatElement.getSize();

            // Wrap around the element pointer without using modulo
            if (++this.vertexFormatIndex >= elements.size()) {
                this.vertexFormatIndex -= elements.size();
            }

            this.vertexFormatElement = elements.get(this.vertexFormatIndex);
        } while (this.vertexFormatElement.getUsage() == VertexFormatElement.EnumUsage.PADDING);

        if (isColorDisabled() && this.vertexFormatElement.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
            //BufferVertexConsumer.super.color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha);
        }
    }
}
