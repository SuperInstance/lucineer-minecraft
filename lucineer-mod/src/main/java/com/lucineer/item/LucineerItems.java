package com.lucineer.item;

import com.lucineer.LucineerMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registration for Lucineer mod.
 */
public class LucineerItems {
    
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LucineerMod.MOD_ID);
    
    // Tools
    public static final DeferredItem<Item> MAGNIFYING_GLASS = ITEMS.register("magnifying_glass",
        () -> new MagnifyingGlassItem(new Item.Properties().stacksTo(1)));
    
    public static final DeferredItem<Item> THERMAL_PROBE = ITEMS.register("thermal_probe",
        () -> new ThermalProbeItem(new Item.Properties().stacksTo(1)));
    
    public static final DeferredItem<Item> TIMING_ANALYZER = ITEMS.register("timing_analyzer",
        () -> new TimingAnalyzerItem(new Item.Properties().stacksTo(1)));
    
    public static final DeferredItem<Item> LOGIC_ANALYZER = ITEMS.register("logic_analyzer",
        () -> new LogicAnalyzerItem(new Item.Properties().stacksTo(1)));
    
    // Components
    public static final DeferredItem<Item> TRANSISTOR = ITEMS.register("transistor",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> LOGIC_GATE = ITEMS.register("logic_gate",
        () -> new LogicGateItem(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> WIRE_SEGMENT = ITEMS.register("wire_segment",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> CAPACITOR = ITEMS.register("capacitor",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    // Neural Network Components
    public static final DeferredItem<Item> INPUT_CELL = ITEMS.register("input_cell",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> NEURON_CELL = ITEMS.register("neuron_cell",
        () -> new NeuronCellItem(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> WEIGHT_ITEM = ITEMS.register("weight_item",
        () -> new WeightItem(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> ACTIVATION_CELL = ITEMS.register("activation_cell",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> OUTPUT_CELL = ITEMS.register("output_cell",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    // Chips (products)
    public static final DeferredItem<Item> LUCI_1_CHIP = ITEMS.register("luci_1_chip",
        () -> new LuciChipItem(1, new Item.Properties().stacksTo(16)));
    
    public static final DeferredItem<Item> LUCI_3_CHIP = ITEMS.register("luci_3_chip",
        () -> new LuciChipItem(3, new Item.Properties().stacksTo(16)));
    
    public static final DeferredItem<Item> LUCI_7_CHIP = ITEMS.register("luci_7_chip",
        () -> new LuciChipItem(7, new Item.Properties().stacksTo(16)));
    
    public static final DeferredItem<Item> CUSTOM_CHIP = ITEMS.register("custom_chip",
        () -> new CustomChipItem(new Item.Properties().stacksTo(16)));
    
    // Crafting Materials
    public static final DeferredItem<Item> SILICON_INGOT = ITEMS.register("silicon_ingot",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> SILICON_DUST = ITEMS.register("silicon_dust",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> COPPER_TRACE = ITEMS.register("copper_trace",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    public static final DeferredItem<Item> GOLD_WIRE = ITEMS.register("gold_wire",
        () -> new Item(new Item.Properties().stacksTo(64)));
    
    // Documentation
    public static final DeferredItem<Item> LESSON_BOOK = ITEMS.register("lesson_book",
        () -> new LessonBookItem(new Item.Properties().stacksTo(1)));
    
    public static final DeferredItem<Item> DESIGN_BLUEPRINT = ITEMS.register("design_blueprint",
        () -> new BlueprintItem(new Item.Properties().stacksTo(16)));
}
