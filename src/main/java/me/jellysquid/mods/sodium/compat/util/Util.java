package me.jellysquid.mods.sodium.compat.util;

import com.mojang.serialization.DataResult;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public final class Util {
    public static DataResult<int[]> fixedSize(IntStream p_240987_0_, int p_240987_1_) {
        int[] aint = p_240987_0_.limit(p_240987_1_ + 1).toArray();
        if (aint.length != p_240987_1_) {
            String s = "Input is not a list of " + p_240987_1_ + " ints";
            return aint.length >= p_240987_1_ ? DataResult.error(s, Arrays.copyOf(aint, p_240987_1_)) : DataResult.error(s);
        } else {
            return DataResult.success(aint);
        }
    }

    public static <T> T make(Supplier<T> p_199748_0_) {
        return p_199748_0_.get();
    }

    public static int getRandom(int[] p_240988_0_, Random p_240988_1_) {
        return p_240988_0_[p_240988_1_.nextInt(p_240988_0_.length)];
    }

    public static <T> T getRandom(T[] p_240989_0_, Random p_240989_1_) {
        return p_240989_0_[p_240989_1_.nextInt(p_240989_0_.length)];
    }
}
