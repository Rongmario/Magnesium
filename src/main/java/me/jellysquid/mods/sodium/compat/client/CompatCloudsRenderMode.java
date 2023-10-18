package me.jellysquid.mods.sodium.compat.client;

public enum CompatCloudsRenderMode {

    OFF(0),
    FAST(1),
    FANCY(2);

    private final int type;

    CompatCloudsRenderMode(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
