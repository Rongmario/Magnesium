package me.jellysquid.mods.sodium.compat.util;

import net.minecraft.util.ResourceLocation;

public class Identifier extends ResourceLocation {
    protected Identifier(int p_i45928_1_, String... p_i45928_2_) {
        super(p_i45928_1_, p_i45928_2_);
    }

    public Identifier(String p_i1293_1_) {
        super(p_i1293_1_);
    }

    public Identifier(String p_i1292_1_, String p_i1292_2_) {
        super(p_i1292_1_, p_i1292_2_);
    }
}
