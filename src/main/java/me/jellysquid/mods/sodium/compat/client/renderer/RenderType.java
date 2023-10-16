package me.jellysquid.mods.sodium.compat.client.renderer;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

@SideOnly(Side.CLIENT)
public final class RenderType {

    public static final RenderType SOLID = new RenderType(2097152);

    public static final RenderType CUTOUT = new RenderType(131072);

    public static final RenderType CUTOUT_MIPPED = new RenderType(131072);

    private final int bufferSize;

    public RenderType(int bufferSize){
        this.bufferSize = bufferSize;
    }

    public int getExpectedBufferSize(){
        return bufferSize;
    }
}
