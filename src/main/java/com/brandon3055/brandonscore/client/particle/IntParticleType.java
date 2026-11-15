package com.brandon3055.brandonscore.client.particle;

import com.brandon3055.brandonscore.utils.DataUtils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by brandon3055 on 27/2/20.
 */
public class IntParticleType extends ParticleType<IntParticleType.IntParticleData> {

    public IntParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    @Override
    public com.mojang.serialization.MapCodec<IntParticleData> codec() {
        return Codec.INT.listOf().fieldOf("data").xmap(
                values -> new IntParticleData(this, values.stream().mapToInt(Integer::intValue).toArray()),
                value -> java.util.Arrays.stream(value.data).boxed().toList());
    }

    @Override
    public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, IntParticleData> streamCodec() {
        return new net.minecraft.network.codec.StreamCodec<>() {
            @Override public IntParticleData decode(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
                return new IntParticleData(IntParticleType.this, buffer.readVarIntArray());
            }
            @Override public void encode(net.minecraft.network.RegistryFriendlyByteBuf buffer, IntParticleData value) {
                buffer.writeVarIntArray(value.data);
            }
        };
    }

    public static class IntParticleData implements ParticleOptions {
        private ParticleType<?> type;
        private int[] data;

        public IntParticleData(ParticleType<?> type, int... data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public ParticleType<?> getType() {
            return type;
        }

        public void writeToNetwork(FriendlyByteBuf buffer) {
            buffer.writeVarIntArray(data);
        }

        public int[] get() {
            return data;
        }

        public String writeToString() {
            return String.format(Locale.ROOT, "%s %sb", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), DataUtils.stringArrayConcat(DataUtils.arrayToString(data), " "));
        }
    }
}
