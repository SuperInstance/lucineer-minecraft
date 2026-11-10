package com.lucineer.block.entity;

import com.lucineer.data.ChipStateData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Block Entity for Chip Design Station.
 * 
 * Features:
 * - 8x8 transistor placement grid
 * - Design stages (RTL -> GDSII)
 * - Thermal simulation
 */
public class ChipDesignBlockEntity extends BlockEntity {
    
    // 8x8 grid of components: 0=empty, 1=NMOS, 2=PMOS, 3=Wire, 4=VDD, 5=GND
    private int[][] grid = new int[8][8];
    private ChipStateData chipState = ChipStateData.EMPTY;
    
    // Design stages
    public static final int STAGE_RTL = 0;
    public static final int STAGE_SYNTHESIS = 1;
    public static final int STAGE_FLOORPLAN = 2;
    public static final int STAGE_PLACEMENT = 3;
    public static final int STAGE_ROUTING = 4;
    public static final int STAGE_TIMING = 5;
    public static final int STAGE_VERIFICATION = 6;
    public static final int STAGE_TAPEOUT = 7;
    
    public ChipDesignBlockEntity(BlockPos pos, BlockState state) {
        super(com.lucineer.block.entity.LucineerBlockEntities.CHIP_DESIGN_ENTITY.get(), pos, state);
    }
    
    public void setComponent(int row, int col, int component) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            grid[row][col] = component;
            updateTransistorCount();
            setChanged();
        }
    }
    
    public int getComponent(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return grid[row][col];
        }
        return 0;
    }
    
    private void updateTransistorCount() {
        int count = 0;
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == 1 || cell == 2) count++; // NMOS or PMOS
            }
        }
        chipState = chipState.withTransistors(count);
    }
    
    public int getTransistorCount() {
        return chipState.transistorCount();
    }
    
    public void advanceStage() {
        if (chipState.designStage() < STAGE_TAPEOUT) {
            chipState = chipState.withStage(chipState.designStage() + 1);
            setChanged();
        }
    }
    
    public int getDesignStage() {
        return chipState.designStage();
    }
    
    public String getStageName() {
        return switch (chipState.designStage()) {
            case 0 -> "RTL Design";
            case 1 -> "Synthesis";
            case 2 -> "Floorplanning";
            case 3 -> "Placement";
            case 4 -> "Routing";
            case 5 -> "Timing Analysis";
            case 6 -> "Verification";
            case 7 -> "Tapeout";
            default -> "Unknown";
        };
    }
    
    public void clearGrid() {
        grid = new int[8][8];
        chipState = ChipStateData.EMPTY;
        setChanged();
    }
    
    /**
     * Load a CMOS inverter template.
     */
    public void loadInverterTemplate() {
        clearGrid();
        grid[1][3] = 4; // VDD
        grid[2][3] = 2; // PMOS
        grid[3][3] = 3; // Wire
        grid[4][3] = 1; // NMOS
        grid[5][3] = 5; // GND
        chipState = chipState.withTransistors(2);
        setChanged();
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        for (int i = 0; i < 8; i++) {
            tag.putIntArray("row_" + i, grid[i]);
        }
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        for (int i = 0; i < 8; i++) {
            int[] row = tag.getIntArray("row_" + i);
            if (row.length == 8) {
                grid[i] = row;
            }
        }
        updateTransistorCount();
    }
}
