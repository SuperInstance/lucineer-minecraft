package com.lucineer.block;

import com.lucineer.block.entity.TransistorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Transistor Block - Basic switching element.
 * 
 * Properties:
 * - TYPE: NMOS or PMOS
 * - GATE: Gate input (on/off)
 * - POWERED: Output state
 */
public class TransistorBlock extends BaseEntityBlock {
    
    public static final EnumProperty<TransistorType> TYPE = EnumProperty.create("type", TransistorType.class);
    public static final BooleanProperty GATE = BooleanProperty.create("gate");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    
    public TransistorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(TYPE, TransistorType.NMOS)
            .setValue(GATE, false)
            .setValue(POWERED, false));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, GATE, POWERED);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransistorBlockEntity(pos, state);
    }
    
    public enum TransistorType {
        NMOS, PMOS
    }
}
