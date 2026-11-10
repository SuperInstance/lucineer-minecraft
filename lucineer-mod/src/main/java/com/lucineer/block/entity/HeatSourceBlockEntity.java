package com.lucineer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Heat Source Block Entity - Generates thermal energy for simulation.
 */
public class HeatSourceBlockEntity extends BlockEntity {
    
    private float heatOutput = 350.0f; // Kelvin output
    private float powerDensity = 0.5f; // W/mm²
    private boolean active = false;
    
    public HeatSourceBlockEntity(BlockPos pos, BlockState state) {
        super(LucineerBlockEntities.HEAT_SOURCE_ENTITY.get(), pos, state);
    }
    
    public float getHeatOutput() { return heatOutput; }
    public float getPowerDensity() { return powerDensity; }
    public boolean isActive() { return active; }
    
    public void setActive(boolean active) {
        this.active = active;
        setChanged();
    }
    
    public void setHeatOutput(float heat) {
        this.heatOutput = Math.max(300.0f, Math.min(1000.0f, heat));
        setChanged();
    }
    
    public void setPowerDensity(float power) {
        this.powerDensity = Math.max(0.1f, Math.min(2.0f, power));
        setChanged();
    }
    
    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, HeatSourceBlockEntity entity) {
        if (entity.active && level.getGameTime() % 10 == 0) {
            // Propagate heat to nearby blocks
            entity.propagateHeat(level, pos);
        }
    }
    
    private void propagateHeat(net.minecraft.world.level.Level level, BlockPos pos) {
        // Simple heat propagation simulation
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighbor);
            if (be instanceof ThermalSensorBlockEntity sensor) {
                sensor.updateFromHeatSource(heatOutput, pos);
            }
        }
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("heat_output", heatOutput);
        tag.putFloat("power_density", powerDensity);
        tag.putBoolean("active", active);
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        heatOutput = tag.getFloat("heat_output");
        powerDensity = tag.getFloat("power_density");
        active = tag.getBoolean("active");
    }
}
