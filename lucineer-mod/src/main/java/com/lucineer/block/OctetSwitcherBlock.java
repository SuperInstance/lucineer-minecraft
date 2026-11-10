package com.lucineer.block;

import com.lucineer.block.entity.OctetSwitcherBlockEntity;
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
 * Octet Switcher Block - Educational binary learning game.
 * 
 * 8 switches representing bits (128, 64, 32, 16, 8, 4, 2, 1)
 * Players learn binary counting by flipping switches.
 * Outputs redstone signal proportional to value (value/255 * 15)
 */
public class OctetSwitcherBlock extends BaseEntityBlock {
    
    public OctetSwitcherBlock(Properties properties) {
        super(properties);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OctetSwitcherBlockEntity(pos, state);
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
        if (be instanceof OctetSwitcherBlockEntity switcher) {
            // Open the switcher UI
            player.openMenu(switcher, pos);
            return InteractionResult.CONSUME;
        }
        
        return InteractionResult.PASS;
    }
}
