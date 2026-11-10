package com.lucineer;

import com.lucineer.block.LucineerBlocks;
import com.lucineer.entity.LucineerEntities;
import com.lucineer.item.LucineerItems;
import com.lucineer.learning.LearningManager;
import com.lucineer.network.LucineerNetworking;
import com.lucineer.thermal.ThermalSimulationManager;
import com.lucineer.voxel.VoxelExplorerManager;
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
 * - RTL-to-GDSII design flow
 * - Real-time thermal simulation
 * - Neural network cell builder
 * - Progressive AI-generated lessons
 * - Age-adaptive content (5+ to professional)
 */
@Mod(LucineerMod.MOD_ID)
public class LucineerMod {
    public static final String MOD_ID = "lucineer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public LucineerMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register deferred registers
        LucineerBlocks.BLOCKS.register(modEventBus);
        LucineerItems.ITEMS.register(modEventBus);
        LucineerEntities.ENTITY_TYPES.register(modEventBus);
        LucineerDataComponents.DATA_COMPONENTS.register(modEventBus);
        
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
            
            // Initialize voxel explorer
            VoxelExplorerManager.initialize();
            
            // Register network handlers
            LucineerNetworking.register();
            
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
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Register client-side renderers
            LucineerBlocks.registerRenderers();
            LucineerEntities.registerRenderers();
            
            // Initialize voxel visualization system
            VoxelExplorerManager.initializeClient();
            
            LOGGER.info("Lucineer client setup complete");
        }
    }
}
