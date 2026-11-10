package com.lucineer;

import com.lucineer.block.LucineerBlocks;
import com.lucineer.item.LucineerItems;
import com.lucineer.block.entity.LucineerBlockEntities;
import com.lucineer.data.LucineerDataComponents;
import com.lucineer.learning.LearningManager;
import com.lucineer.thermal.ThermalSimulationManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lucineer: Chip Design STEM Studio
 * 
 * A comprehensive Minecraft mod that teaches electronics, chip design,
 * thermal engineering, and AI through hands-on gamified learning.
 * 
 * Key Features:
 * - Voxel-based chip cross-section explorer
 * - RTL-to-GDSII design flow simulation
 * - Real-time thermal simulation
 * - Neural network cell builder
 * - Educational adventures (Octet Switcher, Logic Gates, etc.)
 * - Progressive AI-generated lessons
 * - Age-adaptive content (5+ to professional)
 */
@Mod(LucineerMod.MOD_ID)
public class LucineerMod {
    public static final String MOD_ID = "lucineer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public LucineerMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register deferred registers
        LucineerDataComponents.DATA_COMPONENTS.register(modEventBus);
        LucineerBlocks.BLOCKS.register(modEventBus);
        LucineerItems.ITEMS.register(modEventBus);
        LucineerBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        
        // Register events
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        
        LOGGER.info("Lucineer STEM Studio initializing...");
        LOGGER.info("Learn electronics, chip design, and AI through Minecraft!");
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize learning system
            LearningManager.initialize();
            
            // Initialize thermal simulation engine
            ThermalSimulationManager.initialize();
            
            LOGGER.info("Lucineer common setup complete");
        });
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Lucineer server-side systems starting...");
        
        // Load player learning profiles
        LearningManager.loadAllProfiles(event.getServer());
    }
    
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
            LOGGER.info("Lucineer client setup complete");
        }
    }
}
