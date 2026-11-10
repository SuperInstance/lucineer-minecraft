package com.lucineer.data;

import com.lucineer.LucineerMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Data Components for Lucineer mod.
 * These store custom data on items and blocks.
 */
public class LucineerDataComponents {
    
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = 
        DeferredRegister.createDataComponents(LucineerMod.MOD_ID);
    
    // Chip State - stores chip design data
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChipStateData>> CHIP_STATE = 
        DATA_COMPONENTS.registerComponentType("chip_state", builder -> 
            builder.persistent(ChipStateData.CODEC)
                   .networkSynchronized(ChipStateData.STREAM_CODEC));
    
    // Thermal State - stores temperature and heat data
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ThermalStateData>> THERMAL_STATE = 
        DATA_COMPONENTS.registerComponentType("thermal_state", builder -> 
            builder.persistent(ThermalStateData.CODEC)
                   .networkSynchronized(ThermalStateData.STREAM_CODEC));
    
    // Neural Network State - stores NN structure and weights
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<NeuralNetworkData>> NEURAL_NETWORK_STATE = 
        DATA_COMPONENTS.registerComponentType("neural_network_state", builder -> 
            builder.persistent(NeuralNetworkData.CODEC)
                   .networkSynchronized(NeuralNetworkData.STREAM_CODEC));
    
    // Player Learning Profile - stores XP, achievements, lessons
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlayerLearningData>> PLAYER_LEARNING = 
        DATA_COMPONENTS.registerComponentType("player_learning", builder -> 
            builder.persistent(PlayerLearningData.CODEC)
                   .networkSynchronized(PlayerLearningData.STREAM_CODEC));
    
    // Bot Script - stores automation scripts
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BotScriptData>> BOT_SCRIPT = 
        DATA_COMPONENTS.registerComponentType("bot_script", builder -> 
            builder.persistent(BotScriptData.CODEC)
                   .networkSynchronized(BotScriptData.STREAM_CODEC));
    
    // Educational Adventure Progress
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AdventureProgressData>> ADVENTURE_PROGRESS = 
        DATA_COMPONENTS.registerComponentType("adventure_progress", builder -> 
            builder.persistent(AdventureProgressData.CODEC)
                   .networkSynchronized(AdventureProgressData.STREAM_CODEC));
}
