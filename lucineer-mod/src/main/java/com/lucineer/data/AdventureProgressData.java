package com.lucineer.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import java.util.Set;

/**
 * Stores educational adventure progress.
 */
public record AdventureProgressData(
    String adventureId,
    Set<Integer> discoveredValues,
    Set<String> completedObjectives,
    int totalXP,
    int switchFlips,
    long playTimeMs
) {
    public static final Codec<AdventureProgressData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("adventure_id").forGetter(AdventureProgressData::adventureId),
            Codec.INT.listOf().xmap(
                Set::copyOf,
                s -> s.stream().toList()
            ).fieldOf("discovered_values").forGetter(AdventureProgressData::discoveredValues),
            Codec.STRING.listOf().xmap(
                Set::copyOf,
                s -> s.stream().toList()
            ).fieldOf("completed_objectives").forGetter(AdventureProgressData::completedObjectives),
            Codec.INT.fieldOf("total_xp").forGetter(AdventureProgressData::totalXP),
            Codec.INT.fieldOf("switch_flips").forGetter(AdventureProgressData::switchFlips),
            Codec.LONG.fieldOf("play_time_ms").forGetter(AdventureProgressData::playTimeMs)
        ).apply(instance, AdventureProgressData::new)
    );
    
    public static final StreamCodec<ByteBuf, AdventureProgressData> STREAM_CODEC = StreamCodec.of(
        (buf, data) -> {
            ByteBufCodecs.STRING_UTF8.encode(buf, data.adventureId);
            buf.writeInt(data.discoveredValues.size());
            for (int v : data.discoveredValues) buf.writeInt(v);
            buf.writeInt(data.completedObjectives.size());
            for (String s : data.completedObjectives) ByteBufCodecs.STRING_UTF8.encode(buf, s);
            buf.writeInt(data.totalXP);
            buf.writeInt(data.switchFlips);
            buf.writeLong(data.playTimeMs);
        },
        buf -> {
            String id = ByteBufCodecs.STRING_UTF8.decode(buf);
            int valCount = buf.readInt();
            Set<Integer> values = new java.util.HashSet<>();
            for (int i = 0; i < valCount; i++) values.add(buf.readInt());
            int objCount = buf.readInt();
            Set<String> objectives = new java.util.HashSet<>();
            for (int i = 0; i < objCount; i++) objectives.add(ByteBufCodecs.STRING_UTF8.decode(buf));
            return new AdventureProgressData(id, values, objectives, buf.readInt(), buf.readInt(), buf.readLong());
        }
    );
    
    public static AdventureProgressData empty(String adventureId) {
        return new AdventureProgressData(adventureId, Set.of(), Set.of(), 0, 0, 0);
    }
}
