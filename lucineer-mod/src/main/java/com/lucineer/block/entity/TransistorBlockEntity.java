package com.lucineer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Transistor Block Entity - A single transistor with configurable type.
 */
public class TransistorBlockEntity extends BlockEntity {
    
    public enum TransistorType {
        NMOS, PMOS
    }
    
    private TransistorType transistorType = TransistorType.NMOS;
    private boolean gateInput = false;
    private float thresholdVoltage = 0.7f; // Volts
    
    public TransistorBlockEntity(BlockPos pos, BlockState state) {
        super(LucineerBlockEntities.TRANSISTOR_ENTITY.get(), pos, state);
    }
    
    public void setTransistorType(TransistorType type) {
        this.transistorType = type;
        setChanged();
    }
    
    public TransistorType getTransistorType() { return transistorType; }
    public boolean getGateInput() { return gateInput; }
    public float getThresholdVoltage() { return thresholdVoltage; }
    
    /**
     * Compute if transistor is conducting based on gate input.
     */
    public boolean isConducting() {
        return switch (transistorType) {
            case NMOS -> gateInput; // Conducts when gate is high
            case PMOS -> !gateInput; // Conducts when gate is low
        };
    }
    
    public void setGateInput(boolean input) {
        this.gateInput = input;
        setChanged();
    }
    
    public void toggleType() {
        transistorType = transistorType == TransistorType.NMOS 
            ? TransistorType.PMOS 
            : TransistorType.NMOS;
        setChanged();
    }
    
    public String getSymbol() {
        return transistorType == TransistorType.NMOS ? "⬇️" : "⬆️";
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("transistor_type", transistorType.name());
        tag.putBoolean("gate_input", gateInput);
        tag.putFloat("threshold_voltage", thresholdVoltage);
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        try {
            transistorType = TransistorType.valueOf(tag.getString("transistor_type"));
        } catch (IllegalArgumentException e) {
            transistorType = TransistorType.NMOS;
        }
        gateInput = tag.getBoolean("gate_input");
        thresholdVoltage = tag.getFloat("threshold_voltage");
    }
}
