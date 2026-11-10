package com.lucineer.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;

/**
 * Thermal Probe Item - Measures temperature of blocks.
 */
public class ThermalProbeItem extends Item {
    
    public ThermalProbeItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // Raycast to find what player is looking at
            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            BlockPos pos = hit.getBlockPos();
            BlockEntity be = level.getBlockEntity(pos);
            
            if (be instanceof com.lucineer.block.entity.ThermalSensorBlockEntity sensor) {
                float temp = sensor.getCurrentTemperature();
                String status = sensor.getStatus();
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                        String.format("🌡️ Temperature: %.1fK (%.1f°C) - %s", 
                            temp, temp - 273.15f, status)
                    ),
                    true
                );
            } else if (be instanceof com.lucineer.block.entity.HeatSourceBlockEntity heatSource) {
                float temp = heatSource.getHeatOutput();
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                        String.format("🔥 Heat Output: %.1fK - Power: %.2f W/mm²", 
                            temp, heatSource.getPowerDensity())
                    ),
                    true
                );
            } else {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("🌡️ Point at a thermal block to measure"),
                    true
                );
            }
        }
        
        return InteractionResultHolder.success(stack);
    }
}
