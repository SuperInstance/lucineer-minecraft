package com.lucineer.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Magnifying Glass Item - Examine chip internals.
 */
public class MagnifyingGlassItem extends Item {
    
    public MagnifyingGlassItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // When right-clicking on a chip block, open voxel explorer view
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("🔬 Magnifying Glass ready - right-click a chip to examine"),
                true
            );
        }
        
        return InteractionResultHolder.success(stack);
    }
}
