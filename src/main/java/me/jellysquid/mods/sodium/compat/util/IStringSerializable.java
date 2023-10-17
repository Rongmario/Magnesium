package me.jellysquid.mods.sodium.compat.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class IStringSerializable {
    public static <E extends Enum<E> & net.minecraft.util.IStringSerializable> Codec<E> fromEnum(Supplier<E[]> p_233023_0_, Function<? super String, ? extends E> p_233023_1_) {
        E[] ae = p_233023_0_.get();
        return fromStringResolver(Enum::ordinal, (p_233026_1_) -> {
            return ae[p_233026_1_];
        }, p_233023_1_);
    }

    public static <E extends net.minecraft.util.IStringSerializable> Codec<E> fromStringResolver(final ToIntFunction<E> p_233024_0_, final IntFunction<E> p_233024_1_, final Function<? super String, ? extends E> p_233024_2_) {
        return new Codec<E>() {
            public <T> DataResult<T> encode(E p_encode_1_, DynamicOps<T> p_encode_2_, T p_encode_3_) {
                return p_encode_2_.compressMaps() ? p_encode_2_.mergeToPrimitive(p_encode_3_, p_encode_2_.createInt(p_233024_0_.applyAsInt(p_encode_1_))) : p_encode_2_.mergeToPrimitive(p_encode_3_, p_encode_2_.createString(p_encode_1_.getName()));
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> p_decode_1_, T p_decode_2_) {
                return p_decode_1_.compressMaps() ? p_decode_1_.getNumberValue(p_decode_2_).flatMap((p_233034_1_) -> {
                    return Optional.ofNullable(p_233024_1_.apply(p_233034_1_.intValue())).map(DataResult::success).orElseGet(() -> {
                        return DataResult.error("Unknown element id: " + p_233034_1_);
                    });
                }).map((p_233035_1_) -> {
                    return Pair.of(p_233035_1_, p_decode_1_.empty());
                }) : p_decode_1_.getStringValue(p_decode_2_).flatMap((p_233033_1_) -> {
                    return Optional.ofNullable(p_233024_2_.apply(p_233033_1_)).map(DataResult::success).orElseGet(() -> {
                        return DataResult.error("Unknown element name: " + p_233033_1_);
                    });
                }).map((p_233030_1_) -> {
                    return Pair.of(p_233030_1_, p_decode_1_.empty());
                });
            }

            public String toString() {
                return "StringRepresentable[" + p_233024_0_ + "]";
            }
        };
    }
}
