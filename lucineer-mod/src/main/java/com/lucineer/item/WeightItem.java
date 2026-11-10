package com.lucineer.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Weight Item - Adjustable weight value for neural networks.
 * Right-click to adjust weight, color indicates sign.
 */
public class WeightItem extends Item {
    
    private static final float[] WEIGHT_VALUES = {
        -1.0f, -0.5f, -0.25f, 0.0f, 0.25f, 0.5f, 1.0f
    };
    
    public WeightItem(Properties properties) {
        super(properties);
    }
    
    public float getWeight(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getFloat("weight");
        }
        return 0.5f;
    }
    
    public void setWeight(ItemStack stack, float weight) {
        stack.getOrCreateTag().putFloat("weight", weight);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // Cycle through weight values
            float currentWeight = getWeight(stack);
            int nextIndex = 0;
            
            for (int i = 0; i < WEIGHT_VALUES.length; i++) {
                if (Math.abs(WEIGHT_VALUES[i] - currentWeight) < 0.01f) {
                    nextIndex = (i + 1) % WEIGHT_VALUES.length;
                    break;
                }
            }
            
            setWeight(stack, WEIGHT_VALUES[nextIndex]);
            
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    String.format("⚖️ Weight: %.2f", WEIGHT_VALUES[nextIndex])
                ),
                true
            );
        }
        
        return InteractionResultHolder.success(stack);
    }
    
    @Override
    public net.minecraft.network.chat.Component getName(ItemStack stack) {
        float weight = getWeight(stack);
        String sign = weight >= 0 ? "+" : "";
        return net.minecraft.network.chat.Component.literal(
            String.format("Weight (%s%.2f)", sign, weight)
        );
    }
}
