package me.jellysquid.mods.sodium.client.world.cloned.palette;


import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.util.ObjectIntIdentityMap;

public class ClonedPaletteFallback<K> implements ClonedPalette<K> {
    private final IntObjectHashMap<K> idList;

    public ClonedPaletteFallback(IntObjectHashMap<K> idList) {
        this.idList = idList;
    }

    @Override
    public K get(int id) {
        return this.idList.get(id);
    }
}
