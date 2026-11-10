package com.lucineer.item;

import net.minecraft.world.item.Item;

/**
 * LUCI Chip Item - AI inference chips.
 * LUCI-1 (1B), LUCI-3 (3B), LUCI-7 (7B) parameters
 */
public class LuciChipItem extends Item {
    
    private final int parameterCount; // in billions
    private final int powerConsumption; // in watts
    private final int tokenSpeed; // tokens per second
    
    public LuciChipItem(int params, Properties properties) {
        super(properties);
        this.parameterCount = params;
        this.powerConsumption = switch (params) {
            case 1 -> 2;
            case 3 -> 5;
            case 7 -> 10;
            default -> 15;
        };
        this.tokenSpeed = switch (params) {
            case 1 -> 15;
            case 3 -> 25;
            case 7 -> 35;
            default -> 50;
        };
    }
    
    public int getParameterCount() { return parameterCount; }
    public int getPowerConsumption() { return powerConsumption; }
    public int getTokenSpeed() { return tokenSpeed; }
    
    public String getModelName() {
        return String.format("LUCI-%dB", parameterCount);
    }
    
    /**
     * Get energy efficiency (tokens per watt).
     */
    public float getEfficiency() {
        return (float) tokenSpeed / powerConsumption;
    }
    
    @Override
    public net.minecraft.network.chat.Component getDescription() {
        return net.minecraft.network.chat.Component.literal(
            String.format("%dB parameter inference chip. %dW, %d tok/s. %.1f tok/W",
                parameterCount, powerConsumption, tokenSpeed, getEfficiency())
        );
    }
}
