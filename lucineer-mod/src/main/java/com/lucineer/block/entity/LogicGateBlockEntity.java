package com.lucineer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Block Entity for Logic Gate Workbench.
 * 
 * Supports: AND, OR, NOT, XOR, NAND, NOR gates
 */
public class LogicGateBlockEntity extends BlockEntity {
    
    public enum GateType {
        AND, OR, NOT, XOR, NAND, NOR
    }
    
    private GateType gateType = GateType.AND;
    private boolean inputA = false;
    private boolean inputB = false;
    
    public LogicGateBlockEntity(BlockPos pos, BlockState state) {
        super(com.lucineer.block.entity.LucineerBlockEntities.LOGIC_GATE_ENTITY.get(), pos, state);
    }
    
    public void setGateType(GateType type) {
        this.gateType = type;
        setChanged();
    }
    
    public GateType getGateType() {
        return gateType;
    }
    
    public void setInputA(boolean value) {
        this.inputA = value;
        setChanged();
    }
    
    public void setInputB(boolean value) {
        this.inputB = value;
        setChanged();
    }
    
    public boolean getInputA() { return inputA; }
    public boolean getInputB() { return inputB; }
    
    /**
     * Compute the gate output based on inputs and gate type.
     */
    public boolean computeOutput() {
        return switch (gateType) {
            case AND -> inputA && inputB;
            case OR -> inputA || inputB;
            case NOT -> !inputA;
            case XOR -> inputA != inputB;
            case NAND -> !(inputA && inputB);
            case NOR -> !(inputA || inputB);
        };
    }
    
    /**
     * Get the gate symbol for display.
     */
    public String getGateSymbol() {
        return switch (gateType) {
            case AND -> "&";
            case OR -> "≥1";
            case NOT -> "1";
            case XOR -> "=1";
            case NAND -> "&̅";
            case NOR -> "≥1̅";
        };
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("gate_type", gateType.name());
        tag.putBoolean("input_a", inputA);
        tag.putBoolean("input_b", inputB);
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        try {
            gateType = GateType.valueOf(tag.getString("gate_type"));
        } catch (IllegalArgumentException e) {
            gateType = GateType.AND;
        }
        inputA = tag.getBoolean("input_a");
        inputB = tag.getBoolean("input_b");
    }
}
