package com.lucineer.thermal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages thermal simulation across the world.
 */
public class ThermalSimulationManager {
    
    private static final Map<BlockPos, ThermalNode> thermalNodes = new HashMap<>();
    private static boolean initialized = false;
    
    /**
     * Represents a thermal simulation node.
     */
    public record ThermalNode(
        BlockPos position,
        float temperature,
        float thermalConductivity,
        float specificHeat,
        boolean isActive
    ) {
        public ThermalNode withTemperature(float temp) {
            return new ThermalNode(position, temp, thermalConductivity, specificHeat, isActive);
        }
    }
    
    public static void initialize() {
        initialized = true;
        com.lucineer.LucineerMod.LOGGER.info("Thermal Simulation Manager initialized");
    }
    
    public static void tick(Level level) {
        if (!initialized) return;
        
        // Update thermal nodes every 20 ticks (1 second)
        if (level.getGameTime() % 20 == 0) {
            propagateHeat(level);
        }
    }
    
    private static void propagateHeat(Level level) {
        // Simple heat propagation simulation
        for (Map.Entry<BlockPos, ThermalNode> entry : thermalNodes.entrySet()) {
            BlockPos pos = entry.getKey();
            ThermalNode node = entry.getValue();
            
            if (!node.isActive()) continue;
            
            // Propagate heat to neighbors
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                ThermalNode neighbor = thermalNodes.get(neighborPos);
                
                if (neighbor != null) {
                    // Heat transfer equation: Q = k * A * (T1 - T2) / L
                    float deltaT = node.temperature() - neighbor.temperature();
                    float heatTransfer = node.thermalConductivity() * deltaT * 0.1f;
                    
                    // Update neighbor temperature
                    thermalNodes.put(neighborPos, 
                        neighbor.withTemperature(neighbor.temperature() + heatTransfer));
                }
            }
        }
    }
    
    public static void registerNode(BlockPos pos, float conductivity, float specificHeat) {
        thermalNodes.put(pos, new ThermalNode(pos, 300.0f, conductivity, specificHeat, true));
    }
    
    public static void removeNode(BlockPos pos) {
        thermalNodes.remove(pos);
    }
    
    public static void setTemperature(BlockPos pos, float temperature) {
        ThermalNode node = thermalNodes.get(pos);
        if (node != null) {
            thermalNodes.put(pos, node.withTemperature(temperature));
        }
    }
    
    public static float getTemperature(BlockPos pos) {
        ThermalNode node = thermalNodes.get(pos);
        return node != null ? node.temperature() : 300.0f;
    }
    
    public static void setActive(BlockPos pos, boolean active) {
        ThermalNode node = thermalNodes.get(pos);
        if (node != null) {
            thermalNodes.put(pos, new ThermalNode(
                pos, node.temperature(), node.thermalConductivity(), 
                node.specificHeat(), active
            ));
        }
    }
    
    /**
     * Material thermal properties database.
     */
    public static class Materials {
        public static final float SILICON_CONDUCTIVITY = 148.0f;
        public static final float COPPER_CONDUCTIVITY = 400.0f;
        public static final float ALUMINUM_CONDUCTIVITY = 237.0f;
        public static final float SIO2_CONDUCTIVITY = 1.4f;
        
        public static ThermalNode createSiliconNode(BlockPos pos) {
            return new ThermalNode(pos, 300.0f, SILICON_CONDUCTIVITY, 700.0f, true);
        }
        
        public static ThermalNode createCopperNode(BlockPos pos) {
            return new ThermalNode(pos, 300.0f, COPPER_CONDUCTIVITY, 385.0f, true);
        }
    }
}
