package com.lucineer.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stores bot script data for automation.
 */
public record BotScriptData(
    String scriptId,
    String category,       // MINING, FARMING, REDSTONE, CHIP_DESIGN, etc.
    String code,           // The script code (simplified DSL)
    int successRate,       // 0-100
    int executionCount,
    boolean cached
) {
    public static final Codec<BotScriptData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("script_id").forGetter(BotScriptData::scriptId),
            Codec.STRING.fieldOf("category").forGetter(BotScriptData::category),
            Codec.STRING.fieldOf("code").forGetter(BotScriptData::code),
            Codec.INT.fieldOf("success_rate").forGetter(BotScriptData::successRate),
            Codec.INT.fieldOf("execution_count").forGetter(BotScriptData::executionCount),
            Codec.BOOL.fieldOf("cached").forGetter(BotScriptData::cached)
        ).apply(instance, BotScriptData::new)
    );
    
    public static final StreamCodec<ByteBuf, BotScriptData> STREAM_CODEC = StreamCodec.of(
        (buf, data) -> {
            ByteBufCodecs.STRING_UTF8.encode(buf, data.scriptId);
            ByteBufCodecs.STRING_UTF8.encode(buf, data.category);
            ByteBufCodecs.STRING_UTF8.encode(buf, data.code);
            buf.writeInt(data.successRate);
            buf.writeInt(data.executionCount);
            buf.writeBoolean(data.cached);
        },
        buf -> new BotScriptData(
            ByteBufCodecs.STRING_UTF8.decode(buf),
            ByteBufCodecs.STRING_UTF8.decode(buf),
            ByteBufCodecs.STRING_UTF8.decode(buf),
            buf.readInt(),
            buf.readInt(),
            buf.readBoolean()
        )
    );
    
    public static final BotScriptData EMPTY = new BotScriptData("", "", "", 0, 0, false);
}
