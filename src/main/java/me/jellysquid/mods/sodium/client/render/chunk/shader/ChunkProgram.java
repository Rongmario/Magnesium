package me.jellysquid.mods.sodium.client.render.chunk.shader;

import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.GameRendererContext;
import me.jellysquid.mods.sodium.compat.client.renderer.CompatGlStateManager;
import me.jellysquid.mods.sodium.compat.lwjgl.CompatGL20C;
import me.jellysquid.mods.sodium.compat.util.Identifier;
import org.lwjgl.system.MemoryStack;

import java.util.function.Function;

/**
 * A forward-rendering shader program for chunks.
 */
public class ChunkProgram extends GlProgram {
    // Uniform variable binding indexes
    private final int uModelViewProjectionMatrix;
    private final int uModelScale;
    private final int uTextureScale;
    private final int uBlockTex;
    private final int uLightTex;

    // The fog shader component used by this program in order to setup the appropriate GL state
    private final ChunkShaderFogComponent fogShader;

    protected ChunkProgram(RenderDevice owner, Identifier name, int handle, Function<ChunkProgram, ChunkShaderFogComponent> fogShaderFunction) {
        super(owner, name, handle);

        this.uModelViewProjectionMatrix = this.getUniformLocation("u_ModelViewProjectionMatrix");

        this.uBlockTex = this.getUniformLocation("u_BlockTex");
        this.uLightTex = this.getUniformLocation("u_LightTex");
        this.uModelScale = this.getUniformLocation("u_ModelScale");
        this.uTextureScale = this.getUniformLocation("u_TextureScale");

        this.fogShader = fogShaderFunction.apply(this);
    }

    public void setup(float modelScale, float textureScale) {

        CompatGlStateManager.uniform1(this.uBlockTex, 0);
        CompatGlStateManager.uniform1(this.uLightTex, 2);

        CompatGL20C.glUniform3f(this.uModelScale, modelScale, modelScale, modelScale);
        CompatGL20C.glUniform2f(this.uTextureScale, textureScale, textureScale);

        this.fogShader.setup();

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            CompatGlStateManager.uniformMatrix4(this.uModelViewProjectionMatrix, false,
                    GameRendererContext.getModelViewProjectionMatrix(memoryStack));
        }
    }
}
