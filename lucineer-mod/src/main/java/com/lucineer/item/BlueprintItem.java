package com.lucineer.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Blueprint Item - Stores circuit designs and layouts.
 */
public class BlueprintItem extends Item {
    
    public BlueprintItem(Properties properties) {
        super(properties);
    }
    
    public String getDesignName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getString("design_name") : "Unnamed Design";
    }
    
    public void setDesignName(ItemStack stack, String name) {
        stack.getOrCreateTag().putString("design_name", name);
    }
    
    public int[][] getGrid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("grid")) {
            return new int[8][8];
        }
        
        int[][] grid = new int[8][8];
        for (int i = 0; i < 8; i++) {
            grid[i] = tag.getIntArray("row_" + i);
        }
        return grid;
    }
    
    public void setGrid(ItemStack stack, int[][] grid) {
        CompoundTag tag = stack.getOrCreateTag();
        for (int i = 0; i < 8; i++) {
            tag.putIntArray("row_" + i, grid[i]);
        }
    }
    
    public int getTransistorCount(ItemStack stack) {
        int[][] grid = getGrid(stack);
        int count = 0;
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == 1 || cell == 2) count++; // NMOS or PMOS
            }
        }
        return count;
    }
    
    public net.minecraft.network.chat.Component getDescription(ItemStack stack) {
        String name = getDesignName(stack);
        int transistors = getTransistorCount(stack);
        return net.minecraft.network.chat.Component.literal(
            String.format("%s (%d transistors)", name, transistors)
        );
    }
}
