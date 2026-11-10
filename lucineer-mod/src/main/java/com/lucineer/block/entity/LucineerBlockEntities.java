package com.lucineer.block.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Block Entity Type registration for Lucineer mod.
 */
public class LucineerBlockEntities {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, com.lucineer.LucineerMod.MOD_ID);
    
    // Educational Adventure Entities
    public static final Supplier<BlockEntityType<OctetSwitcherBlockEntity>> OCTET_SWITCHER_ENTITY = 
        BLOCK_ENTITIES.register("octet_switcher", () ->
            BlockEntityType.Builder.of(OctetSwitcherBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.OCTET_SWITCHER.get()).build(null));
    
    public static final Supplier<BlockEntityType<LogicGateBlockEntity>> LOGIC_GATE_ENTITY = 
        BLOCK_ENTITIES.register("logic_gate", () ->
            BlockEntityType.Builder.of(LogicGateBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.LOGIC_GATE_WORKBENCH.get()).build(null));
    
    public static final Supplier<BlockEntityType<ChipDesignBlockEntity>> CHIP_DESIGN_ENTITY = 
        BLOCK_ENTITIES.register("chip_design", () ->
            BlockEntityType.Builder.of(ChipDesignBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.CHIP_DESIGN_STATION.get()).build(null));
    
    public static final Supplier<BlockEntityType<BinaryCalculatorBlockEntity>> BINARY_CALCULATOR_ENTITY = 
        BLOCK_ENTITIES.register("binary_calculator", () ->
            BlockEntityType.Builder.of(BinaryCalculatorBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.BINARY_CALCULATOR.get()).build(null));
    
    public static final Supplier<BlockEntityType<NeuralNetworkBlockEntity>> NEURAL_NETWORK_ENTITY = 
        BLOCK_ENTITIES.register("neural_network", () ->
            BlockEntityType.Builder.of(NeuralNetworkBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.NEURAL_NETWORK_BENCH.get()).build(null));
    
    // Core Block Entities
    public static final Supplier<BlockEntityType<ChipBlockEntity>> CHIP_BLOCK_ENTITY = 
        BLOCK_ENTITIES.register("chip_block", () ->
            BlockEntityType.Builder.of(ChipBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.CHIP_BLOCK.get()).build(null));
    
    public static final Supplier<BlockEntityType<TransistorBlockEntity>> TRANSISTOR_ENTITY = 
        BLOCK_ENTITIES.register("transistor", () ->
            BlockEntityType.Builder.of(TransistorBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.TRANSISTOR_BLOCK.get()).build(null));
    
    public static final Supplier<BlockEntityType<HeatSourceBlockEntity>> HEAT_SOURCE_ENTITY = 
        BLOCK_ENTITIES.register("heat_source", () ->
            BlockEntityType.Builder.of(HeatSourceBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.HEAT_SOURCE.get()).build(null));
    
    public static final Supplier<BlockEntityType<ThermalSensorBlockEntity>> THERMAL_SENSOR_ENTITY = 
        BLOCK_ENTITIES.register("thermal_sensor", () ->
            BlockEntityType.Builder.of(ThermalSensorBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.THERMAL_SENSOR.get()).build(null));
    
    public static final Supplier<BlockEntityType<CoolingBlockEntity>> COOLING_ENTITY = 
        BLOCK_ENTITIES.register("cooling", () ->
            BlockEntityType.Builder.of(CoolingBlockEntity::new, 
                com.lucineer.block.LucineerBlocks.COOLING_ELEMENT.get()).build(null));
}
