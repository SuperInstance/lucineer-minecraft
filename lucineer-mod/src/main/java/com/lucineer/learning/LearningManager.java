package com.lucineer.learning;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.WorldData;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player learning progress and AI-generated lessons.
 */
public class LearningManager {
    
    private static final Map<UUID, PlayerLearningProfile> profiles = new HashMap<>();
    private static MinecraftServer server;
    
    /**
     * Player learning profile data.
     */
    public record PlayerLearningProfile(
        UUID playerId,
        int totalXP,
        int level,
        int ageTier, // 1-5 (Explorer to Expert)
        Map<String, Integer> adventureProgress,
        Map<String, Boolean> unlockedRecipes
    ) {}
    
    public static void initialize() {
        com.lucineer.LucineerMod.LOGGER.info("Learning Manager initialized");
    }
    
    public static void loadAllProfiles(MinecraftServer server) {
        LearningManager.server = server;
        // In production, load from world save
        com.lucineer.LucineerMod.LOGGER.info("Loading player learning profiles...");
    }
    
    public static PlayerLearningProfile getProfile(Player player) {
        return profiles.computeIfAbsent(player.getUUID(), 
            id -> new PlayerLearningProfile(id, 0, 1, 1, new HashMap<>(), new HashMap<>())
        );
    }
    
    public static void addXP(Player player, int xp) {
        PlayerLearningProfile profile = getProfile(player);
        int newTotal = profile.totalXP() + xp;
        int newLevel = 1 + newTotal / 100;
        
        profiles.put(player.getUUID(), new PlayerLearningProfile(
            profile.playerId(),
            newTotal,
            newLevel,
            profile.ageTier(),
            profile.adventureProgress(),
            profile.unlockedRecipes()
        ));
        
        // Notify player
        if (newLevel > profile.level()) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    String.format("🎓 Level Up! You are now level %d!", newLevel)
                ),
                false
            );
        }
    }
    
    public static void recordDiscovery(Player player, String adventureId, int value) {
        PlayerLearningProfile profile = getProfile(player);
        Map<String, Integer> progress = new HashMap<>(profile.adventureProgress());
        progress.merge(adventureId + "_discovered", 1, Integer::sum);
        
        profiles.put(player.getUUID(), new PlayerLearningProfile(
            profile.playerId(),
            profile.totalXP(),
            profile.level(),
            profile.ageTier(),
            progress,
            profile.unlockedRecipes()
        ));
    }
    
    public static void unlockRecipe(Player player, String recipeId) {
        PlayerLearningProfile profile = getProfile(player);
        Map<String, Boolean> recipes = new HashMap<>(profile.unlockedRecipes());
        recipes.put(recipeId, true);
        
        profiles.put(player.getUUID(), new PlayerLearningProfile(
            profile.playerId(),
            profile.totalXP(),
            profile.level(),
            profile.ageTier(),
            profile.adventureProgress(),
            recipes
        ));
        
        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal(
                String.format("🔓 New recipe unlocked: %s!", recipeId)
            ),
            false
        );
    }
    
    public static int getAgeTier(Player player) {
        return getProfile(player).ageTier();
    }
    
    public static void setAgeTier(Player player, int tier) {
        PlayerLearningProfile profile = getProfile(player);
        profiles.put(player.getUUID(), new PlayerLearningProfile(
            profile.playerId(),
            profile.totalXP(),
            profile.level(),
            Math.max(1, Math.min(5, tier)),
            profile.adventureProgress(),
            profile.unlockedRecipes()
        ));
    }
}
