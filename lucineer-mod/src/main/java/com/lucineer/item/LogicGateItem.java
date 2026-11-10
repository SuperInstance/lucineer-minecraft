package com.lucineer.item;

import net.minecraft.world.item.Item;

/**
 * Logic Gate Item - Represents AND, OR, NOT, XOR, NAND, NOR gates.
 */
public class LogicGateItem extends Item {
    
    private final LogicGateBlockEntity.GateType gateType;
    
    public LogicGateItem(Properties properties, LogicGateBlockEntity.GateType gateType) {
        super(properties);
        this.gateType = gateType;
    }
    
    public LogicGateBlockEntity.GateType getGateType() { return gateType; }
    
    /**
     * Compute the logic output.
     */
    public boolean compute(boolean a, boolean b) {
        return switch (gateType) {
            case AND -> a && b;
            case OR -> a || b;
            case NOT -> !a;
            case XOR -> a != b;
            case NAND -> !(a && b);
            case NOR -> !(a || b);
        };
    }
    
    public String getGateName() {
        return switch (gateType) {
            case AND -> "AND Gate";
            case OR -> "OR Gate";
            case NOT -> "NOT Gate (Inverter)";
            case XOR -> "XOR Gate";
            case NAND -> "NAND Gate";
            case NOR -> "NOR Gate";
        };
    }
    
    public String getGateDescription() {
        return switch (gateType) {
            case AND -> "Output is HIGH only when all inputs are HIGH";
            case OR -> "Output is HIGH when any input is HIGH";
            case NOT -> "Output is opposite of input (inverts signal)";
            case XOR -> "Output is HIGH when inputs are different";
            case NAND -> "Universal gate - Output is LOW only when all inputs are HIGH";
            case NOR -> "Output is HIGH only when all inputs are LOW";
        };
    }
}
