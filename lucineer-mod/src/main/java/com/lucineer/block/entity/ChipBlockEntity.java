package com.lucineer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Base Chip Block Entity - Stores chip design and thermal data.
 */
public class ChipBlockEntity extends BlockEntity {
    
    private int designStage = 0;
    private int transistorCount = 0;
    private float temperature = 300.0f; // Kelvin
    private boolean verified = false;
    private int timingScore = 0;
    private int powerConsumption = 0;
    
    public ChipBlockEntity(BlockPos pos, BlockState state) {
        super(LucineerBlockEntities.CHIP_BLOCK_ENTITY.get(), pos, state);
    }
    
    public void setDesignStage(int stage) {
        this.designStage = Math.max(0, Math.min(9, stage));
        setChanged();
    }
    
    public int getDesignStage() { return designStage; }
    public int getTransistorCount() { return transistorCount; }
    public float getTemperature() { return temperature; }
    public boolean isVerified() { return verified; }
    
    public void addTransistor() {
        transistorCount++;
        setChanged();
    }
    
    public void setTemperature(float temp) {
        this.temperature = temp;
        setChanged();
    }
    
    public void verify() {
        if (transistorCount > 0 && designStage >= 7) {
            verified = true;
            setChanged();
        }
    }
    
    public void displayThermalData(net.minecraft.world.entity.player.Player player) {
        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal("Temperature: " + temperature + "K"),
            true
        );
    }
    
    public void displayTimingData(net.minecraft.world.entity.player.Player player) {
        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal("Timing Score: " + timingScore + "/100"),
            true
        );
    }
    
    public void onRedstoneInput(int signal) {
        // Chips respond to redstone input
        if (signal > 0 && !verified) {
            temperature += 5.0f;
        }
    }
    
    public void dropContents() {
        // Drop items when block is broken
    }
    
    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, ChipBlockEntity entity) {
        // Simulate thermal effects
        if (level.getGameTime() % 20 == 0) {
            entity.temperature = Math.max(300.0f, entity.temperature - 0.5f);
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("design_stage", designStage);
        tag.putInt("transistor_count", transistorCount);
        tag.putFloat("temperature", temperature);
        tag.putBoolean("verified", verified);
        tag.putInt("timing_score", timingScore);
        tag.putInt("power_consumption", powerConsumption);
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        designStage = tag.getInt("design_stage");
        transistorCount = tag.getInt("transistor_count");
        temperature = tag.getFloat("temperature");
        verified = tag.getBoolean("verified");
        timingScore = tag.getInt("timing_score");
        powerConsumption = tag.getInt("power_consumption");
    }
}
