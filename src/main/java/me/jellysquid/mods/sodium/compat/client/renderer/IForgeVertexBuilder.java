package me.jellysquid.mods.sodium.compat.client.renderer;

import me.jellysquid.mods.sodium.compat.util.LightUtil;
import me.jellysquid.mods.sodium.compat.util.math.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.system.MemoryStack;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public interface IForgeVertexBuilder {
    default IVertexBuilder getVertexBuilder() {
        return (IVertexBuilder) this;
    }

    // Copy of addQuad, but enables tinting and per-vertex alpha
    default void addVertexData(BakedQuad bakedQuad, float red, float green, float blue, int lightmapCoord, int overlayColor, boolean readExistingColor) {
        addVertexData(bakedQuad, red, green, blue, 1.0f, lightmapCoord, overlayColor, readExistingColor);
    }

    // Copy of addQuad with alpha support
    default void addVertexData(BakedQuad bakedQuad, float red, float green, float blue, float alpha, int lightmapCoord, int overlayColor) {
        addVertexData(bakedQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, alpha, new int[]{lightmapCoord, lightmapCoord, lightmapCoord, lightmapCoord}, overlayColor, false);
    }

    // Copy of addQuad with alpha support
    default void addVertexData(BakedQuad bakedQuad, float red, float green, float blue, float alpha, int lightmapCoord, int overlayColor, boolean readExistingColor) {
        addVertexData(bakedQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, alpha, new int[]{lightmapCoord, lightmapCoord, lightmapCoord, lightmapCoord}, overlayColor, readExistingColor);
    }

    // Copy of addQuad with alpha support
    default void addVertexData(BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor) {
        int[] aint = bakedQuad.getVertexData();
        Vector3i faceNormal = Direction.from(bakedQuad.getFace()).getNormal();
        Vector3f normal = new Vector3f((float) faceNormal.getX(), (float) faceNormal.getY(), (float) faceNormal.getZ());
        //normal.transform(GlStateManager.matri);
        //normal.transform(matrixEntry.normal());
        int intSize = DefaultVertexFormats.BLOCK.getIntegerSize();
        int vertexCount = aint.length / intSize;

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormats.BLOCK.getSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for (int v = 0; v < vertexCount; ++v) {
                ((Buffer) intbuffer).clear();
                intbuffer.put(aint, v * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);
                float cr;
                float cg;
                float cb;
                float ca;
                if (readExistingColor) {
                    float r = (float) (bytebuffer.get(12) & 255) / 255.0F;
                    float g = (float) (bytebuffer.get(13) & 255) / 255.0F;
                    float b = (float) (bytebuffer.get(14) & 255) / 255.0F;
                    float a = (float) (bytebuffer.get(15) & 255) / 255.0F;
                    cr = r * baseBrightness[v] * red;
                    cg = g * baseBrightness[v] * green;
                    cb = b * baseBrightness[v] * blue;
                    ca = a * alpha;
                } else {
                    cr = baseBrightness[v] * red;
                    cg = baseBrightness[v] * green;
                    cb = baseBrightness[v] * blue;
                    ca = alpha;
                }

                int lightmapCoord = applyBakedLighting(lightmapCoords[v], bytebuffer);
                float f9 = bytebuffer.getFloat(16);
                float f10 = bytebuffer.getFloat(20);
                Vector4f pos = new Vector4f(f, f1, f2, 1.0F);
                //pos.transform(matrix4f);
                //applyBakedNormals(normal, bytebuffer, matrixEntry.normal());
                ((IVertexBuilder) this).vertex(pos.x(), pos.y(), pos.z(), cr, cg, cb, ca, f9, f10, overlayCoords, lightmapCoord, normal.x(), normal.y(), normal.z());
            }
        }
    }

    default int applyBakedLighting(int lightmapCoord, ByteBuffer data) {
        int bl = lightmapCoord & 0xFFFF;
        int sl = (lightmapCoord >> 16) & 0xFFFF;

        int offset = LightUtil.getLightOffset(0) * 4; // int offset for vertex 0 * 4 bytes per int
        int blBaked = Short.toUnsignedInt(data.getShort(offset));
        int slBaked = Short.toUnsignedInt(data.getShort(offset + 2));
        bl = Math.max(bl, blBaked);
        sl = Math.max(sl, slBaked);
        return bl | (sl << 16);
    }

    default void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
        byte nx = data.get(28);
        byte ny = data.get(29);
        byte nz = data.get(30);
        if (nx != 0 || ny != 0 || nz != 0) {
            generated.set(nx / 127f, ny / 127f, nz / 127f);
            generated.transform(normalTransform);
        }
    }
}
