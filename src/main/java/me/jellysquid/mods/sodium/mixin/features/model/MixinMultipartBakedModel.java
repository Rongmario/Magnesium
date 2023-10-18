package me.jellysquid.mods.sodium.mixin.features.model;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.sodium.client.util.rand.XoRoShiRoRandom;
import net.minecraft.block.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;

@Mixin(MultipartBakedModel.class)
public class MixinMultipartBakedModel {
    private final Map<IBlockState, IBakedModel[]> stateCacheFast = new Reference2ReferenceOpenHashMap<>();
    private final StampedLock lock = new StampedLock();

    private final XoRoShiRoRandom random = new XoRoShiRoRandom();

    @Shadow
    @Final
    private Map<com.google.common.base.Predicate<IBlockState>, IBakedModel> selectors;

    /**
     * @author JellySquid
     * @reason Avoid expensive allocations and replace bitfield indirection
     */
    @Overwrite
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null) {
            return Collections.emptyList();
        }

        IBakedModel[] models;

        long readStamp = this.lock.readLock();
        try {
            models = this.stateCacheFast.get(state);
        } finally {
            this.lock.unlockRead(readStamp);
        }

        if (models == null) {
            long writeStamp = this.lock.writeLock();
            try {
                List<IBakedModel> modelList = new ArrayList<>(this.selectors.size());

                for (Map.Entry<com.google.common.base.Predicate<IBlockState>, IBakedModel> pair : this.selectors.entrySet()) {
                    if (pair.getKey().test(state)) {
                        modelList.add(pair.getValue());
                    }
                }

                models = modelList.toArray(new IBakedModel[modelList.size()]);
                this.stateCacheFast.put(state, models);
            } finally {
                this.lock.unlockWrite(writeStamp);
            }
        }

        List<BakedQuad> quads = new ArrayList<>();
        long seed = random.nextLong();

        for (IBakedModel model : models) {
            random.setSeed(seed);
            quads.addAll(model.getQuads(state, side, random.nextLong()));
        }

        return quads;
    }

}
