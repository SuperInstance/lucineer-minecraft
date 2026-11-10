# Lucineer Minecraft Mod - Data Pack

**Version:** 1.21.1 NeoForge  
**Generated from:** github.com/superinstance/lucineer  
**Purpose:** Integrate Lucineer AI chip thermal simulation into Minecraft world generation

---

## Overview

This data pack translates the Lucineer project's core concepts into Minecraft 1.21.1 NeoForge data pack format:

| Lucineer Concept | Minecraft Implementation |
|-----------------|-------------------------|
| Thermal Simulation Grid | Dimension with custom noise settings |
| Material Properties | Data Component: `thermal_material` |
| Heat Sources | Data Component: `heat_source` |
| Boundary Conditions | Data Component: `boundary_condition` |
| Simulation Config | Data Component: `simulation_config` |
| LUCI Chips | Data Component: `luci_chip` |

---

## File Structure

```
minecraft/
├── data/
│   └── lucineer/
│       ├── dimension_type/          # World rules
│       │   ├── silicon_foundry.json
│       │   ├── neural_synapse.json
│       │   └── thermal_test_chamber.json
│       │
│       ├── dimension/               # World instances
│       │   ├── silicon_foundry.json
│       │   ├── neural_synapse.json
│       │   └── thermal_test_chamber.json
│       │
│       ├── worldgen/
│       │   ├── biome/
│       │   │   └── silicon_wafer.json
│       │   └── noise_settings/
│       │       ├── silicon_foundry_noise.json
│       │       └── neural_synapse_noise.json
│       │
│       └── data_component_type/     # Custom item data
│           ├── luci_chip.json
│           ├── thermal_material.json
│           ├── heat_source.json
│           ├── boundary_condition.json
│           └── simulation_config.json
│
├── validate_schemas.py              # Validation script
├── worldgen_test.py                 # WorldGen test suite
└── README.md
```

---

## Dimension Types

### 1. Silicon Foundry (`lucineer:silicon_foundry`)
- **Purpose:** Represents a chip manufacturing environment
- **Based on:** Thermal simulation parameters from `core_thermal.py`
- **Height:** 384 blocks (matches simulation grid scale)
- **Thermal Properties:**
  - Ambient temperature: 300K
  - Thermal conductivity base: 148 W/mK (silicon)
  - Boundary conditions: Neumann (sides), Convective (top), Dirichlet (bottom)

### 2. Neural Synapse (`lucineer:neural_synapse`)
- **Purpose:** Represents neuromorphic computing structures
- **Based on:** Biological thermal simulation from `biological_thermal.py`
- **Height:** 256 blocks
- **Neural Properties:**
  - Synapse density: 0.85
  - Ternary weights: [-1, 0, +1]
  - Weight precision: b1.58 (BitNet compatible)

### 3. Thermal Test Chamber (`lucineer:thermal_test_chamber`)
- **Purpose:** Flat dimension for controlled thermal testing
- **Height:** 128 blocks
- **Test Features:**
  - Isolation mode enabled
  - All boundary types available
  - 100x100x1 default grid size

---

## Data Components

### LUCI Chip (`lucineer:luci_chip`)
Represents an AI inference cartridge with embedded neural weights.

| Variant | Parameters | Power | Speed | Process |
|---------|------------|-------|-------|---------|
| LUCI-1  | 1B         | 2W    | 15 tok/s | 28nm |
| LUCI-3  | 3B         | 5W    | 25 tok/s | 28nm |
| LUCI-7  | 7B         | 10W   | 35 tok/s | 28nm |

### Thermal Material (`lucineer:thermal_material`)
Material properties for thermal simulation.

| Material | k (W/mK) | cp (J/kgK) | ρ (kg/m³) |
|----------|----------|------------|-----------|
| Silicon | 148.0 | 700.0 | 2329.0 |
| Silicon Thin Film | 50.0 | 700.0 | 2329.0 |
| Copper Damascene | 300.0 | 385.0 | 8960.0 |
| SiO₂ | 1.4 | 745.0 | 2200.0 |
| GST (Phase Change) | 0.5-2.0 | 210.0 | 6000.0 |
| Cytoplasm (Bio) | 0.6 | 4186.0 | 1000.0 |

### Heat Source (`lucineer:heat_source`)
Defines heat generation in thermal simulation.

**Types:** `point`, `gaussian`, `uniform`, `line`, `rectangular`, `cylindrical`

**Time Profiles:** `constant`, `pulse`, `periodic`, `ramp`, `custom`

### Boundary Condition (`lucineer:boundary_condition`)
Thermal boundary definitions.

**Types:**
- `dirichlet` - Fixed temperature
- `neumann` - Fixed heat flux (adiabatic when flux=0)
- `convective` - Heat transfer coefficient
- `periodic` - Wrapping boundaries
- `radiation` - Radiative heat transfer

---

## Noise Settings

### Silicon Foundry Noise
Based on finite difference thermal grid:
- Default block: Iron Block (represents interconnects)
- Surface block: Copper Block (heat spreader)
- Custom `lucineer:thermal_grid` extension for boundary conditions

### Neural Synapse Noise
Based on stochastic network topology:
- Default block: Netherrack (synaptic substrate)
- Surface block: Glowstone (neural activity)
- Custom `lucineer:neural_network` extension for synapse patterns

---

## Validation Results

```
============================================================
LUCINEER MINECRAFT SCHEMA VALIDATION REPORT
============================================================

✓ Passed: 14
⚠ Warnings: 0
✗ Errors: 0
============================================================
```

### WorldGen Test Results

| Test | Description | Result |
|------|-------------|--------|
| Test 1 | Silicon Foundry Noise Settings | ✓ PASSED |
| Test 2 | Neural Synapse Noise Settings | ✓ PASSED |
| Test 3 | Thermal Grid Boundary Validation | ✓ PASSED |
| Test 4 | Large Grid Performance Test (100x100) | ✓ PASSED |

---

## Integration Notes

### For NeoForge Mod Developers

1. **Dimension Registration:**
   ```java
   ResourceKey<DimensionType> SILICON_FOUNDRY_TYPE = 
       ResourceKey.create(Registries.DIMENSION_TYPE, 
           new ResourceLocation("lucineer", "silicon_foundry"));
   ```

2. **Data Component Usage:**
   ```java
   // Attach thermal material to block entity
   blockEntity.set(DataComponents.THERMAL_MATERIAL, ThermalMaterial.SILICON);
   
   // Add heat source to location
   heatSourceComponent = new HeatSource(
       new BlockPos(25, 25, 0), 
       1e-6, // 1 microwatt
       HeatSource.Shape.GAUSSIAN,
       2.0   // sigma
   );
   ```

3. **Custom Noise Router:**
   - Use `lucineer:thermal_grid` extension for thermal-aware terrain
   - Temperature affects block placement (high temp = metallic blocks)

---

## Source Code Mapping

| Lucineer Python Module | Minecraft Schema |
|-----------------------|------------------|
| `thermal_simulation/core_thermal.py` | `dimension_type/silicon_foundry.json` |
| `thermal_simulation/materials.py` | `data_component_type/thermal_material.json` |
| `thermal_simulation/biological_thermal.py` | `dimension_type/neural_synapse.json` |
| `SimulationConfig` dataclass | `data_component_type/simulation_config.json` |
| `HeatSource` dataclass | `data_component_type/heat_source.json` |
| `BoundaryCondition` dataclass | `data_component_type/boundary_condition.json` |

---

## License

Generated from Lucineer project (github.com/superinstance/lucineer).  
Original project MIT License applies.
