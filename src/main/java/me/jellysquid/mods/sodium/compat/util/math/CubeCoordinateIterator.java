package me.jellysquid.mods.sodium.compat.util.math;

public class CubeCoordinateIterator {
    private final int originX;
    private final int originY;
    private final int originZ;
    private final int width;
    private final int height;
    private final int depth;
    private final int end;
    private int index;
    private int x;
    private int y;
    private int z;

    public CubeCoordinateIterator(int p_i50798_1_, int p_i50798_2_, int p_i50798_3_, int p_i50798_4_, int p_i50798_5_, int p_i50798_6_) {
        this.originX = p_i50798_1_;
        this.originY = p_i50798_2_;
        this.originZ = p_i50798_3_;
        this.width = p_i50798_4_ - p_i50798_1_ + 1;
        this.height = p_i50798_5_ - p_i50798_2_ + 1;
        this.depth = p_i50798_6_ - p_i50798_3_ + 1;
        this.end = this.width * this.height * this.depth;
    }

    public boolean advance() {
        if (this.index == this.end) {
            return false;
        } else {
            this.x = this.index % this.width;
            int i = this.index / this.width;
            this.y = i % this.height;
            this.z = i / this.height;
            ++this.index;
            return true;
        }
    }

    public int nextX() {
        return this.originX + this.x;
    }

    public int nextY() {
        return this.originY + this.y;
    }

    public int nextZ() {
        return this.originZ + this.z;
    }

    public int getNextType() {
        int i = 0;
        if (this.x == 0 || this.x == this.width - 1) {
            ++i;
        }

        if (this.y == 0 || this.y == this.height - 1) {
            ++i;
        }

        if (this.z == 0 || this.z == this.depth - 1) {
            ++i;
        }

        return i;
    }
}