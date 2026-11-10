package com.lucineer.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Cooling Element Block - Dissipates heat in thermal simulation.
 */
public class CoolingBlock extends BaseEntityBlock {
    
    public CoolingBlock(Properties properties) {
        super(properties);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new com.lucineer.block.entity.CoolingBlockEntity(pos, state);
    }
    
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
}
