package com.lucineer.block;

import com.lucineer.block.entity.ChipBlockEntity;
import com.lucineer.block.entity.VoxelExplorerBlockEntity;
import com.lucineer.block.entity.ThermalSimulatorBlockEntity;
import com.lucineer.block.entity.NeuralNetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Chip Block - The core block for player-designed chips.
 * 
 * Features:
 * - Stores chip design data (RTL, netlist, timing)
 * - Real-time thermal simulation
 * - Visual heat map overlay
 * - Redstone I/O for integration
 */
public class ChipBlock extends BaseEntityBlock {
    
    // Block state properties
    public static final IntegerProperty DESIGN_STAGE = IntegerProperty.create("design_stage", 0, 9);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty OVERHEATED = BooleanProperty.create("overheated");
    public static final IntegerProperty TEMPERATURE = IntegerProperty.create("temperature", 0, 15);
    
    // Voxel shape for the chip (smaller than full block)
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
    
    public ChipBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(DESIGN_STAGE, 0)
            .setValue(POWERED, false)
            .setValue(OVERHEATED, false)
            .setValue(TEMPERATURE, 0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DESIGN_STAGE, POWERED, OVERHEATED, TEMPERATURE);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChipBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null; // No client-side ticking needed
        }
        return createTickerHelper(type, LucineerBlocks.CHIP_BLOCK_ENTITY.get(), 
            ChipBlockEntity::serverTick);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, 
            Player player, InteractionHand hand, BlockHitResult hit) {
        
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChipBlockEntity chipEntity)) {
            return InteractionResult.PASS;
        }
        
        ItemStack heldItem = player.getItemInHand(hand);
        
        // Check for tools
        if (heldItem.is(LucineerItems.MAGNIFYING_GLASS.get())) {
            // Open voxel explorer view
            player.openMenu(chipEntity, pos);
            return InteractionResult.CONSUME;
        }
        
        if (heldItem.is(LucineerItems.THERMAL_PROBE.get())) {
            // Show thermal data
            chipEntity.displayThermalData(player);
            return InteractionResult.CONSUME;
        }
        
        if (heldItem.is(LucineerItems.TIMING_ANALYZER.get())) {
            // Show timing analysis
            chipEntity.displayTimingData(player);
            return InteractionResult.CONSUME;
        }
        
        // Default: open design interface
        player.openMenu(chipEntity, pos);
        return InteractionResult.CONSUME;
    }
    
    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(POWERED);
    }
    
    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }
    
    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, 
            Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;
        
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
            
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChipBlockEntity chipEntity) {
                chipEntity.onRedstoneInput(level.getBestNeighborSignal(pos));
            }
        }
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, 
            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ChipBlockEntity chipEntity) {
                chipEntity.dropContents();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
