package com.lucineer.block.entity;

import com.lucineer.data.AdventureProgressData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Block Entity for the Octet Switcher educational game.
 * 
 * Tracks:
 * - 8 switch states (bits)
 * - Discovered values (256 possible)
 * - Completed objectives
 * - XP earned
 */
public class OctetSwitcherBlockEntity extends BlockEntity {
    
    // 8 bit values
    private boolean[] switches = new boolean[8];
    private AdventureProgressData progress = AdventureProgressData.empty("octet_switcher");
    
    // Bit values: 128, 64, 32, 16, 8, 4, 2, 1
    private static final int[] BIT_VALUES = {128, 64, 32, 16, 8, 4, 2, 1};
    
    public OctetSwitcherBlockEntity(BlockPos pos, BlockState state) {
        super(com.lucineer.block.entity.LucineerBlockEntities.OCTET_SWITCHER_ENTITY.get(), pos, state);
    }
    
    public int getCurrentValue() {
        int value = 0;
        for (int i = 0; i < 8; i++) {
            if (switches[i]) value += BIT_VALUES[i];
        }
        return value;
    }
    
    public void toggleSwitch(int index) {
        if (index >= 0 && index < 8) {
            switches[index] = !switches[index];
            int value = getCurrentValue();
            progress = new AdventureProgressData(
                progress.adventureId(),
                java.util.Collections.singleton(value),
                progress.completedObjectives(),
                progress.totalXP(),
                progress.switchFlips() + 1,
                progress.playTimeMs()
            );
            setChanged();
        }
    }
    
    public boolean getSwitch(int index) {
        return index >= 0 && index < 8 && switches[index];
    }
    
    public void setSwitch(int index, boolean value) {
        if (index >= 0 && index < 8) {
            switches[index] = value;
            setChanged();
        }
    }
    
    public void setAllSwitches(boolean[] newSwitches) {
        if (newSwitches.length == 8) {
            this.switches = newSwitches.clone();
            setChanged();
        }
    }
    
    public int getDiscoveredCount() {
        return progress.discoveredValues().size();
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        for (int i = 0; i < 8; i++) {
            tag.putBoolean("switch_" + i, switches[i]);
        }
    }
    
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        for (int i = 0; i < 8; i++) {
            switches[i] = tag.getBoolean("switch_" + i);
        }
    }
    
    /**
     * Get redstone output power (0-15) based on current value.
     */
    public int getRedstoneOutput() {
        return (int) (getCurrentValue() / 255.0 * 15);
    }
    
    /**
     * Calculate binary string representation.
     */
    public String getBinaryString() {
        StringBuilder sb = new StringBuilder();
        for (boolean b : switches) {
            sb.append(b ? '1' : '0');
        }
        return sb.toString();
    }
    
    /**
     * Calculate hex string representation.
     */
    public String getHexString() {
        return String.format("%02X", getCurrentValue());
    }
}
