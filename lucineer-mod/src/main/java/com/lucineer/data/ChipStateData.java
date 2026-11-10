package com.lucineer.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stores chip design state data.
 */
public record ChipStateData(
    int designStage,      // 0-9 (RTL to Tapeout)
    int transistorCount,
    String gateNetlist,   // JSON of gate connections
    int timingScore,      // 0-100
    int powerConsumption, // in milliwatts
    boolean verified
) {
    public static final Codec<ChipStateData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("design_stage").forGetter(ChipStateData::designStage),
            Codec.INT.fieldOf("transistor_count").forGetter(ChipStateData::transistorCount),
            Codec.STRING.fieldOf("gate_netlist").forGetter(ChipStateData::gateNetlist),
            Codec.INT.fieldOf("timing_score").forGetter(ChipStateData::timingScore),
            Codec.INT.fieldOf("power_consumption").forGetter(ChipStateData::powerConsumption),
            Codec.BOOL.fieldOf("verified").forGetter(ChipStateData::verified)
        ).apply(instance, ChipStateData::new)
    );
    
    public static final StreamCodec<ByteBuf, ChipStateData> STREAM_CODEC = StreamCodec.of(
        (buf, data) -> {
            buf.writeInt(data.designStage);
            buf.writeInt(data.transistorCount);
            ByteBufCodecs.STRING_UTF8.encode(buf, data.gateNetlist);
            buf.writeInt(data.timingScore);
            buf.writeInt(data.powerConsumption);
            buf.writeBoolean(data.verified);
        },
        buf -> new ChipStateData(
            buf.readInt(),
            buf.readInt(),
            ByteBufCodecs.STRING_UTF8.decode(buf),
            buf.readInt(),
            buf.readInt(),
            buf.readBoolean()
        )
    );
    
    public static final ChipStateData EMPTY = new ChipStateData(0, 0, "{}", 0, 0, false);
    
    public ChipStateData withStage(int stage) {
        return new ChipStateData(stage, transistorCount, gateNetlist, timingScore, powerConsumption, verified);
    }
    
    public ChipStateData withTransistors(int count) {
        return new ChipStateData(designStage, count, gateNetlist, timingScore, powerConsumption, verified);
    }
}
