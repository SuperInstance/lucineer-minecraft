package com.lucineer.block;

import com.lucineer.block.entity.HeatSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Heat Source Block - Generates thermal energy for simulation.
 */
public class HeatSourceBlock extends BaseEntityBlock {
    
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    
    public HeatSourceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatSourceBlockEntity(pos, state);
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, powered), 3);
        }
    }
    
    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }
    
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }
    
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
}
