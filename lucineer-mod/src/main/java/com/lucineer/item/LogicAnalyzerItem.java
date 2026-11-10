package com.lucineer.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.lucineer.block.entity.LogicGateBlockEntity;

/**
 * Logic Analyzer Item - Debug digital circuits.
 */
public class LogicAnalyzerItem extends Item {
    
    public LogicAnalyzerItem(Properties properties) {
        super(properties);
    }
    
    /**
     * Analyze a logic gate and return its truth table.
     */
    public String analyzeGate(LogicGateBlockEntity gate) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Logic Gate Analysis ===\n");
        sb.append("Type: ").append(gate.getGateType()).append("\n");
        sb.append("Symbol: ").append(gate.getGateSymbol()).append("\n");
        sb.append("\nTruth Table:\n");
        
        boolean hasBInput = gate.getGateType() != LogicGateBlockEntity.GateType.NOT;
        
        if (hasBInput) {
            sb.append("A | B | OUT\n");
            sb.append("--|---|----\n");
            for (boolean a : new boolean[]{false, true}) {
                for (boolean b : new boolean[]{false, true}) {
                    gate.setInputA(a);
                    gate.setInputB(b);
                    sb.append(String.format("%d | %d | %d\n", 
                        a ? 1 : 0, b ? 1 : 0, gate.computeOutput() ? 1 : 0));
                }
            }
        } else {
            sb.append("A | OUT\n");
            sb.append("--|----\n");
            for (boolean a : new boolean[]{false, true}) {
                gate.setInputA(a);
                sb.append(String.format("%d | %d\n", a ? 1 : 0, gate.computeOutput() ? 1 : 0));
            }
        }
        
        return sb.toString();
    }
}
