package com.lucineer.block;

import com.lucineer.block.entity.ThermalSensorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Thermal Sensor Block - Measures and displays temperature.
 */
public class ThermalSensorBlock extends BaseEntityBlock {
    
    public ThermalSensorBlock(Properties properties) {
        super(properties);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThermalSensorBlockEntity(pos, state);
    }
    
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        // Trigger thermal update
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ThermalSensorBlockEntity sensor) {
            sensor.updateThermalReading();
        }
    }
}
