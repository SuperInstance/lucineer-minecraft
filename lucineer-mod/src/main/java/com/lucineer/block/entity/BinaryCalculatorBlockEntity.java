package com.lucineer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Block Entity for Binary Calculator.
 * 
 * 4-bit adder/subtractor with carry visualization.
 */
public class BinaryCalculatorBlockEntity extends BlockEntity {
    
    // Two 4-bit numbers (values 0-15)
    private boolean[] bitsA = new boolean[4];
    private boolean[] bitsB = new boolean[4];
    private Operation operation = Operation.ADD;
    
    public enum Operation {
        ADD, SUB
    }
    
    // Bit values: 8, 4, 2, 1
    private static final int[] BIT_VALUES = {8, 4, 2, 1};
    
    public BinaryCalculatorBlockEntity(BlockPos pos, BlockState state) {
        super(com.lucineer.block.entity.LucineerBlockEntities.BINARY_CALCULATOR_ENTITY.get(), pos, state);
    }
    
    public int getValueA() {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            if (bitsA[i]) value += BIT_VALUES[i];
        }
        return value;
    }
    
    public int getValueB() {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            if (bitsB[i]) value += BIT_VALUES[i];
        }
        return value;
    }
    
    public int getResult() {
        int result = operation == Operation.ADD 
            ? getValueA() + getValueB() 
            : getValueA() - getValueB();
        
        // Handle overflow/underflow
        if (result < 0) result = 16 + result;
        if (result > 15) result = result % 16;
        
        return result;
    }
    
    public boolean[] getResultBits() {
        boolean[] result = new boolean[4];
        int value = getResult();
        for (int i = 0; i < 4; i++) {
            result[i] = (value & BIT_VALUES[i]) != 0;
        }
        return result;
    }
    
    /**
     * Calculate carries for visualization.
     */
    public boolean[] getCarries() {
        boolean[] carries = new boolean[4];
        boolean carry = false;
        
        for (int i = 3; i >= 0; i--) {
            int sum = (bitsA[i] ? 1 : 0) + (bitsB[i] ? 1 : 0) + (carry ? 1 : 0);
            carries[i] = sum >= 2;
            carry = sum >= 2;
        }
        
        return carries;
    }
    
    public void toggleBitA(int index) {
        if (index >= 0 && index < 4) {
            bitsA[index] = !bitsA[index];
            setChanged();
        }
    }
    
    public void toggleBitB(int index) {
        if (index >= 0 && index < 4) {
            bitsB[index] = !bitsB[index];
            setChanged();
        }
    }
    
    public void setOperation(Operation op) {
        this.operation = op;
        setChanged();
    }
    
    public Operation getOperation() { return operation; }
    public boolean[] getBitsA() { return bitsA.clone(); }
    public boolean[] getBitsB() { return bitsB.clone(); }
    
    public void setBitsA(boolean[] bits) {
        if (bits.length == 4) {
            this.bitsA = bits.clone();
            setChanged();
        }
    }
    
    public void setBitsB(boolean[] bits) {
        if (bits.length == 4) {
            this.bitsB = bits.clone();
            setChanged();
        }
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        for (int i = 0; i < 4; i++) {
            tag.putBoolean("bit_a_" + i, bitsA[i]);
            tag.putBoolean("bit_b_" + i, bitsB[i]);
        }
        tag.putString("operation", operation.name());
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        for (int i = 0; i < 4; i++) {
            bitsA[i] = tag.getBoolean("bit_a_" + i);
            bitsB[i] = tag.getBoolean("bit_b_" + i);
        }
        try {
            operation = Operation.valueOf(tag.getString("operation"));
        } catch (IllegalArgumentException e) {
            operation = Operation.ADD;
        }
    }
}
