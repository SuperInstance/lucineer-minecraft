package com.lucineer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Cooling Block Entity - Dissipates heat for thermal management.
 */
public class CoolingBlockEntity extends BlockEntity {
    
    private float coolingRate = 5.0f; // Kelvin per tick
    private float maxCooling = 50.0f; // Maximum temperature reduction
    private boolean active = false;
    
    public CoolingBlockEntity(BlockPos pos, BlockState state) {
        super(com.lucineer.block.entity.LucineerBlockEntities.COOLING_ENTITY.get(), pos, state);
    }
    
    public float getCoolingRate() { return coolingRate; }
    public float getMaxCooling() { return maxCooling; }
    public boolean isActive() { return active; }
    
    public void setActive(boolean active) {
        this.active = active;
        setChanged();
    }
    
    /**
     * Apply cooling to a temperature value.
     */
    public float applyCooling(float currentTemp) {
        if (!active) return currentTemp;
        
        float newTemp = currentTemp - coolingRate;
        return Math.max(300.0f, newTemp); // Can't go below room temp
    }
    
    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, CoolingBlockEntity entity) {
        if (entity.active) {
            // Cool nearby blocks
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                BlockEntity be = level.getBlockEntity(neighbor);
                if (be instanceof ThermalSensorBlockEntity sensor) {
                    sensor.updateFromHeatSource(sensor.getCurrentTemperature() - entity.coolingRate, pos);
                }
            }
        }
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("cooling_rate", coolingRate);
        tag.putFloat("max_cooling", maxCooling);
        tag.putBoolean("active", active);
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        coolingRate = tag.getFloat("cooling_rate");
        maxCooling = tag.getFloat("max_cooling");
        active = tag.getBoolean("active");
    }
}
