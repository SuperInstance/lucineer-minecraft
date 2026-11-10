package com.lucineer.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stores thermal simulation state.
 */
public record ThermalStateData(
    float temperature,      // in Kelvin
    float[] heatMap,        // 16x16 heat distribution
    float thermalResistance,
    boolean overHeated
) {
    public static final Codec<ThermalStateData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("temperature").forGetter(ThermalStateData::temperature),
            Codec.FLOAT.listOf().xmap(
                list -> list.stream().mapToFloat(Float::floatValue).toArray(),
                arr -> java.util.Arrays.stream(arr).boxed().toList()
            ).fieldOf("heat_map").forGetter(ThermalStateData::heatMap),
            Codec.FLOAT.fieldOf("thermal_resistance").forGetter(ThermalStateData::thermalResistance),
            Codec.BOOL.fieldOf("overheated").forGetter(ThermalStateData::overHeated)
        ).apply(instance, ThermalStateData::new)
    );
    
    public static final StreamCodec<ByteBuf, ThermalStateData> STREAM_CODEC = StreamCodec.of(
        (buf, data) -> {
            buf.writeFloat(data.temperature);
            for (float f : data.heatMap) buf.writeFloat(f);
            buf.writeFloat(data.thermalResistance);
            buf.writeBoolean(data.overHeated);
        },
        buf -> {
            float temp = buf.readFloat();
            float[] map = new float[256];
            for (int i = 0; i < 256; i++) map[i] = buf.readFloat();
            float resistance = buf.readFloat();
            boolean overheated = buf.readBoolean();
            return new ThermalStateData(temp, map, resistance, overheated);
        }
    );
    
    public static final ThermalStateData DEFAULT = new ThermalStateData(
        300.0f, // Room temperature
        new float[256],
        0.5f,
        false
    );
}
