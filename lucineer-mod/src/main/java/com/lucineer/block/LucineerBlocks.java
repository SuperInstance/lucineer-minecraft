package com.lucineer.block;

import com.lucineer.LucineerMod;
import com.lucineer.block.entity.LucineerBlockEntities;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Block registration for Lucineer mod.
 */
public class LucineerBlocks {
    
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LucineerMod.MOD_ID);
    
    // Educational Adventure Blocks
    public static final DeferredBlock<Block> OCTET_SWITCHER = registerBlock("octet_switcher",
        () -> new OctetSwitcherBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.EMERALD)
            .strength(2.0f, 3.0f)
            .sound(SoundType.METAL)));
    
    public static final DeferredBlock<Block> LOGIC_GATE_WORKBENCH = registerBlock("logic_gate_workbench",
        () -> new LogicGateBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.GOLD)
            .strength(2.5f, 3.5f)
            .sound(SoundType.METAL)));
    
    public static final DeferredBlock<Block> CHIP_DESIGN_STATION = registerBlock("chip_design_station",
        () -> new ChipDesignBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.DIAMOND)
            .strength(3.0f, 4.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));
    
    public static final DeferredBlock<Block> BINARY_CALCULATOR = registerBlock("binary_calculator",
        () -> new BinaryCalculatorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.LAPIS)
            .strength(2.0f, 3.0f)
            .sound(SoundType.METAL)));
    
    public static final DeferredBlock<Block> NEURAL_NETWORK_BENCH = registerBlock("neural_network_bench",
        () -> new NeuralNetworkBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.MAGENTA)
            .strength(2.5f, 3.5f)
            .sound(SoundType.METAL)));
    
    // Chip Design Blocks
    public static final DeferredBlock<Block> CHIP_BLOCK = registerBlock("chip_block",
        () -> new ChipBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOL)
            .strength(1.5f, 2.0f)
            .sound(SoundType.METAL)));
    
    public static final DeferredBlock<Block> SILICON_WAFER = registerBlock("silicon_wafer",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 4.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));
    
    public static final DeferredBlock<Block> TRANSISTOR_BLOCK = registerBlock("transistor_block",
        () -> new TransistorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.CLAY)
            .strength(2.0f, 3.0f)
            .sound(SoundType.METAL)));
    
    // Thermal Simulation Blocks
    public static final DeferredBlock<Block> HEAT_SOURCE = registerBlock("heat_source",
        () -> new HeatSourceBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.FIRE)
            .strength(2.0f, 2.0f)
            .sound(SoundType.METAL)
            .lightLevel(state -> 7)));
    
    public static final DeferredBlock<Block> THERMAL_SENSOR = registerBlock("thermal_sensor",
        () -> new ThermalSensorBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.ICE)
            .strength(1.5f, 2.0f)
            .sound(SoundType.METAL)));
    
    public static final DeferredBlock<Block> COOLING_ELEMENT = registerBlock("cooling_element",
        () -> new CoolingBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WATER)
            .strength(2.0f, 2.5f)
            .sound(SoundType.METAL)));
    
    // Helper method to register block with item
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        registerBlockItem(name, registered);
        return registered;
    }
    
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        com.lucineer.item.LucineerItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
