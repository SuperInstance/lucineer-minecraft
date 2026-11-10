package com.lucineer.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import java.util.List;
import java.util.Set;

/**
 * Stores player learning progress and achievements.
 */
public record PlayerLearningData(
    int totalXP,
    int level,
    Set<String> completedLessons,
    Set<String> unlockedRecipes,
    List<String> activeObjectives,
    int ageTier,           // 1-5 (Explorer to Expert)
    String currentAdventure
) {
    public static final Codec<PlayerLearningData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("total_xp").forGetter(PlayerLearningData::totalXP),
            Codec.INT.fieldOf("level").forGetter(PlayerLearningData::level),
            Codec.STRING.listOf().xmap(
                Set::copyOf,
                List::copyOf
            ).fieldOf("completed_lessons").forGetter(PlayerLearningData::completedLessons),
            Codec.STRING.listOf().xmap(
                Set::copyOf,
                List::copyOf
            ).fieldOf("unlocked_recipes").forGetter(PlayerLearningData::unlockedRecipes),
            Codec.STRING.listOf().fieldOf("active_objectives").forGetter(PlayerLearningData::activeObjectives),
            Codec.INT.fieldOf("age_tier").forGetter(PlayerLearningData::ageTier),
            Codec.STRING.fieldOf("current_adventure").forGetter(PlayerLearningData::currentAdventure)
        ).apply(instance, PlayerLearningData::new)
    );
    
    public static final StreamCodec<ByteBuf, PlayerLearningData> STREAM_CODEC = StreamCodec.of(
        (buf, data) -> {
            buf.writeInt(data.totalXP);
            buf.writeInt(data.level);
            buf.writeInt(data.completedLessons.size());
            for (String s : data.completedLessons) ByteBufCodecs.STRING_UTF8.encode(buf, s);
            buf.writeInt(data.unlockedRecipes.size());
            for (String s : data.unlockedRecipes) ByteBufCodecs.STRING_UTF8.encode(buf, s);
            buf.writeInt(data.activeObjectives.size());
            for (String s : data.activeObjectives) ByteBufCodecs.STRING_UTF8.encode(buf, s);
            buf.writeInt(data.ageTier);
            ByteBufCodecs.STRING_UTF8.encode(buf, data.currentAdventure);
        },
        buf -> {
            int xp = buf.readInt();
            int level = buf.readInt();
            int lessonCount = buf.readInt();
            Set<String> lessons = new java.util.HashSet<>();
            for (int i = 0; i < lessonCount; i++) lessons.add(ByteBufCodecs.STRING_UTF8.decode(buf));
            int recipeCount = buf.readInt();
            Set<String> recipes = new java.util.HashSet<>();
            for (int i = 0; i < recipeCount; i++) recipes.add(ByteBufCodecs.STRING_UTF8.decode(buf));
            int objCount = buf.readInt();
            List<String> objectives = new java.util.ArrayList<>();
            for (int i = 0; i < objCount; i++) objectives.add(ByteBufCodecs.STRING_UTF8.decode(buf));
            int ageTier = buf.readInt();
            String adventure = ByteBufCodecs.STRING_UTF8.decode(buf);
            return new PlayerLearningData(xp, level, lessons, recipes, objectives, ageTier, adventure);
        }
    );
    
    public static final PlayerLearningData DEFAULT = new PlayerLearningData(
        0, 1, Set.of(), Set.of(), List.of(), 1, ""
    );
    
    public PlayerLearningData withXP(int additionalXP) {
        int newTotal = totalXP + additionalXP;
        int newLevel = 1 + (newTotal / 100);
        return new PlayerLearningData(newTotal, newLevel, completedLessons, unlockedRecipes, activeObjectives, ageTier, currentAdventure);
    }
}
