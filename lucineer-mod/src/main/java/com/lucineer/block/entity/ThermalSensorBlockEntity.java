package com.lucineer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Thermal Sensor Block Entity - Measures and displays temperature.
 */
public class ThermalSensorBlockEntity extends BlockEntity {
    
    private float currentTemperature = 300.0f;
    private float maxTemperature = 300.0f;
    private float minTemperature = 300.0f;
    private final Map<BlockPos, Float> heatSources = new HashMap<>();
    
    public ThermalSensorBlockEntity(BlockPos pos, BlockState state) {
        super(LucineerBlockEntities.THERMAL_SENSOR_ENTITY.get(), pos, state);
    }
    
    public float getCurrentTemperature() { return currentTemperature; }
    public float getMaxTemperature() { return maxTemperature; }
    public float getMinTemperature() { return minTemperature; }
    
    public void updateThermalReading() {
        // Calculate average temperature from all heat sources
        if (heatSources.isEmpty()) {
            currentTemperature = 300.0f;
        } else {
            float sum = 0;
            for (Float temp : heatSources.values()) {
                sum += temp;
            }
            currentTemperature = sum / heatSources.size();
        }
        
        maxTemperature = Math.max(maxTemperature, currentTemperature);
        minTemperature = Math.min(minTemperature, currentTemperature);
        
        setChanged();
    }
    
    public void updateFromHeatSource(float temperature, BlockPos source) {
        // Apply distance-based falloff
        double distance = Math.sqrt(source.distSqr(worldPosition));
        float effectiveTemp = (float) (temperature - distance * 10);
        heatSources.put(source, Math.max(300.0f, effectiveTemp));
        updateThermalReading();
    }
    
    /**
     * Get redstone signal strength based on temperature.
     * 0 = 300K, 15 = 1000K+
     */
    public int getRedstoneSignal() {
        float normalized = (currentTemperature - 300) / 700;
        return Math.max(0, Math.min(15, (int) (normalized * 15)));
    }
    
    /**
     * Check if temperature exceeds safe threshold.
     */
    public boolean isOverheated() {
        return currentTemperature > 358.0f; // 85°C = 358K
    }
    
    /**
     * Get temperature status for display.
     */
    public String getStatus() {
        if (currentTemperature < 320) return "COLD";
        if (currentTemperature < 358) return "NORMAL";
        if (currentTemperature < 400) return "WARM";
        if (currentTemperature < 500) return "HOT";
        return "CRITICAL";
    }
    
    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, ThermalSensorBlockEntity entity) {
        if (level.getGameTime() % 20 == 0) {
            // Natural cooling
            if (entity.currentTemperature > 300.0f) {
                entity.currentTemperature -= 0.5f;
                entity.setChanged();
            }
        }
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("current_temp", currentTemperature);
        tag.putFloat("max_temp", maxTemperature);
        tag.putFloat("min_temp", minTemperature);
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        currentTemperature = tag.getFloat("current_temp");
        maxTemperature = tag.getFloat("max_temp");
        minTemperature = tag.getFloat("min_temp");
    }
}
