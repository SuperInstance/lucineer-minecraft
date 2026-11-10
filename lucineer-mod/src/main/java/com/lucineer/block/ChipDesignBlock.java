package com.lucineer.block;

import com.lucineer.block.entity.ChipDesignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Chip Design Station Block - Layout transistors on a grid.
 */
public class ChipDesignBlock extends BaseEntityBlock {
    
    public ChipDesignBlock(Properties properties) {
        super(properties);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChipDesignBlockEntity(pos, state);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, 
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChipDesignBlockEntity designStation) {
            player.openMenu(designStation, pos);
            return InteractionResult.CONSUME;
        }
        
        return InteractionResult.PASS;
    }
}
