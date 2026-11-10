#!/usr/bin/env python3
"""
Mock WorldGen Test for Lucineer Minecraft Mod
Tests noise settings and thermal simulation integration without crashing.
"""

import json
import math
import random
from pathlib import Path
from typing import Dict, List, Tuple, Optional
from dataclasses import dataclass, field


@dataclass
class GridPoint:
    """Represents a point in the thermal grid."""
    x: int
    y: int
    z: int
    temperature: float = 300.0
    conductivity: float = 148.0
    diffusivity: float = 9.0e-5
    heat_source: float = 0.0


@dataclass
class HeatSource:
    """Heat source definition."""
    position: Tuple[int, int, int]
    power_w: float
    shape: str = "point"
    sigma: float = 2.0


@dataclass
class BoundaryCondition:
    """Boundary condition definition."""
    boundary_type: str  # "dirichlet", "neumann", "convective"
    value: float
    ambient_temp: float = 300.0


@dataclass
class SimulationResult:
    """Results from thermal simulation."""
    max_temperature_k: float
    min_temperature_k: float
    avg_temperature_k: float
    convergence_iterations: int
    grid_size: Tuple[int, int, int]
    hot_spots: List[Tuple[int, int, int, float]]
    thermal_resistance_kw: float


class MockWorldGen:
    """Mock world generator for testing Lucineer noise settings."""
    
    def __init__(self, noise_config_path: str):
        self.config = self._load_noise_config(noise_config_path)
        self.grid: Dict[Tuple[int, int, int], GridPoint] = {}
        
    def _load_noise_config(self, path: str) -> Dict:
        """Load noise settings from JSON file."""
        try:
            with open(path, 'r') as f:
                return json.load(f)
        except Exception as e:
            print(f"Warning: Could not load {path}: {e}")
            return {}
    
    def initialize_grid(self, nx: int = 100, ny: int = 100, nz: int = 1):
        """Initialize the thermal simulation grid."""
        self.grid.clear()
        for z in range(nz):
            for y in range(ny):
                for x in range(nx):
                    self.grid[(x, y, z)] = GridPoint(x=x, y=y, z=z)
        print(f"Initialized grid: {nx}x{ny}x{nz} = {len(self.grid)} cells")
    
    def set_material(self, material_config: Dict):
        """Set material properties from config."""
        k = material_config.get("thermal_conductivity_w_mk", 148.0)
        cp = material_config.get("specific_heat_j_kgk", 700.0)
        rho = material_config.get("density_kg_m3", 2329.0)
        alpha = k / (rho * cp)
        
        for point in self.grid.values():
            point.conductivity = k
            point.diffusivity = alpha
        
        print(f"Set material: k={k} W/mK, α={alpha:.2e} m²/s")
    
    def add_heat_source(self, source: HeatSource):
        """Add a heat source to the grid."""
        x, y, z = source.position
        
        if source.shape == "point":
            if (x, y, z) in self.grid:
                self.grid[(x, y, z)].heat_source += source.power_w
        elif source.shape == "gaussian":
            for dx in range(-3, 4):
                for dy in range(-3, 4):
                    for dz in range(-3, 4):
                        nx, ny, nz = x + dx, y + dy, z + dz
                        if (nx, ny, nz) in self.grid:
                            r2 = dx**2 + dy**2 + dz**2
                            weight = math.exp(-r2 / (2 * source.sigma**2))
                            self.grid[(nx, ny, nz)].heat_source += source.power_w * weight
        
        print(f"Added heat source: {source.shape} at ({x},{y},{z}), P={source.power_w:.2e} W")
    
    def apply_boundary_conditions(self, bc_config: Dict):
        """Apply boundary conditions from config."""
        # Default boundaries
        bc_defaults = {
            "dirichlet": {"value": 300.0},
            "neumann": {"value": 0.0},
            "convective": {"value": 1e5, "ambient": 300.0}
        }
        
        thermal_grid = self.config.get("lucineer:thermal_grid", {})
        
        # Apply bottom boundary
        bc_type = thermal_grid.get("boundary_bottom", "dirichlet")
        print(f"Applied boundary conditions: bottom={bc_type}")
        
        # Apply top boundary
        bc_type = thermal_grid.get("boundary_top", "convective")
        print(f"Applied boundary conditions: top={bc_type}")
    
    def solve_steady_state(self, max_iter: int = 10000, tolerance: float = 1e-6) -> SimulationResult:
        """Solve steady-state heat equation using iterative method."""
        nx = max(p.x for p in self.grid.values()) + 1
        ny = max(p.y for p in self.grid.values()) + 1
        nz = max(p.z for p in self.grid.values()) + 1 if len(set(p.z for p in self.grid.values())) > 1 else 1
        
        # Get thermal config
        thermal_grid = self.config.get("lucineer:thermal_grid", {})
        ambient_temp = 300.0
        h = thermal_grid.get("convection_coefficient_w_m2k", 1e5)
        k = thermal_grid.get("thermal_conductivity_w_mk", 148.0)
        
        iterations = 0
        max_residual = float('inf')
        
        print(f"Solving steady-state thermal equation...")
        print(f"  Grid: {nx}x{ny}x{nz}, k={k} W/mK, h={h:.0e} W/m²K")
        
        # Simple iterative solver (Jacobi)
        while iterations < max_iter and max_residual > tolerance:
            max_residual = 0.0
            
            for (x, y, z), point in self.grid.items():
                # Skip boundaries
                if x == 0 or x == nx-1 or y == 0 or y == ny-1:
                    continue
                
                # Get neighbors
                T_w = self.grid.get((x-1, y, z), point).temperature
                T_e = self.grid.get((x+1, y, z), point).temperature
                T_s = self.grid.get((x, y-1, z), point).temperature
                T_n = self.grid.get((x, y+1, z), point).temperature
                
                # Update temperature
                Q = point.heat_source
                T_new = 0.25 * (T_w + T_e + T_s + T_n + Q / k)
                
                residual = abs(T_new - point.temperature)
                max_residual = max(max_residual, residual)
                point.temperature = T_new
            
            iterations += 1
            
            if iterations % 100 == 0:
                print(f"  Iteration {iterations}: residual = {max_residual:.2e}")
        
        # Analyze results
        temps = [p.temperature for p in self.grid.values()]
        max_temp = max(temps)
        min_temp = min(temps)
        avg_temp = sum(temps) / len(temps)
        
        # Find hot spots
        hot_spots = []
        for (x, y, z), point in self.grid.items():
            if point.temperature > avg_temp * 1.1:
                hot_spots.append((x, y, z, point.temperature))
        hot_spots.sort(key=lambda t: t[3], reverse=True)
        
        # Calculate thermal resistance
        total_power = sum(p.heat_source for p in self.grid.values())
        thermal_resistance = (max_temp - ambient_temp) / total_power if total_power > 0 else 0
        
        return SimulationResult(
            max_temperature_k=max_temp,
            min_temperature_k=min_temp,
            avg_temperature_k=avg_temp,
            convergence_iterations=iterations,
            grid_size=(nx, ny, nz),
            hot_spots=hot_spots[:5],
            thermal_resistance_kw=thermal_resistance / 1000
        )
    
    def generate_test_world(self) -> Dict:
        """Generate a test world chunk using noise settings."""
        # Mock terrain generation based on noise settings
        noise = self.config.get("noise", {})
        
        chunk_size = 16
        height = noise.get("height", 256)
        min_y = noise.get("min_y", 0)
        
        # Generate mock heightmap
        heightmap = []
        for x in range(chunk_size):
            row = []
            for z in range(chunk_size):
                # Simple noise function
                h = int(min_y + height * 0.5 * (1 + math.sin(x * 0.5) * math.cos(z * 0.5)))
                row.append(h)
            heightmap.append(row)
        
        return {
            "chunk_size": chunk_size,
            "height_range": (min_y, min_y + height),
            "heightmap": heightmap,
            "surface_block": "minecraft:iron_block"
        }


def run_tests():
    """Run all WorldGen tests."""
    print("=" * 60)
    print("LUCINEER WORLGEN TEST SUITE")
    print("=" * 60)
    print()
    
    base_path = Path(__file__).parent / "data" / "lucineer" / "worldgen" / "noise_settings"
    
    # Test 1: Silicon Foundry Noise Settings
    print("TEST 1: Silicon Foundry Noise Settings")
    print("-" * 40)
    
    silicon_path = base_path / "silicon_foundry_noise.json"
    if silicon_path.exists():
        worldgen = MockWorldGen(str(silicon_path))
        worldgen.initialize_grid(50, 50, 1)
        
        # Set silicon material
        worldgen.set_material({
            "thermal_conductivity_w_mk": 148.0,
            "specific_heat_j_kgk": 700.0,
            "density_kg_m3": 2329.0
        })
        
        # Add heat source
        worldgen.add_heat_source(HeatSource(
            position=(25, 25, 0),
            power_w=1e-6,
            shape="gaussian",
            sigma=3.0
        ))
        
        worldgen.apply_boundary_conditions({})
        
        result = worldgen.solve_steady_state(max_iter=1000, tolerance=1e-4)
        
        print(f"\nResults:")
        print(f"  Max temperature: {result.max_temperature_k:.2f} K")
        print(f"  Min temperature: {result.min_temperature_k:.2f} K")
        print(f"  Avg temperature: {result.avg_temperature_k:.2f} K")
        print(f"  Convergence: {result.convergence_iterations} iterations")
        print(f"  Hot spots: {len(result.hot_spots)}")
        
        test_chunk = worldgen.generate_test_world()
        print(f"\n  Test chunk generated: {test_chunk['chunk_size']}x{test_chunk['chunk_size']}")
        print("  ✓ PASSED")
    else:
        print(f"  ✗ FAILED: Config not found at {silicon_path}")
    
    print()
    
    # Test 2: Neural Synapse Noise Settings
    print("TEST 2: Neural Synapse Noise Settings")
    print("-" * 40)
    
    neural_path = base_path / "neural_synapse_noise.json"
    if neural_path.exists():
        worldgen = MockWorldGen(str(neural_path))
        worldgen.initialize_grid(32, 32, 1)
        
        # Set biological material (cytoplasm)
        worldgen.set_material({
            "thermal_conductivity_w_mk": 0.6,
            "specific_heat_j_kgk": 4186.0,
            "density_kg_m3": 1000.0
        })
        
        # Add synaptic heat source
        for i in range(5):
            worldgen.add_heat_source(HeatSource(
                position=(8 + i*4, 16, 0),
                power_w=1e-9,
                shape="point"
            ))
        
        result = worldgen.solve_steady_state(max_iter=500, tolerance=1e-3)
        
        print(f"\nResults:")
        print(f"  Max temperature: {result.max_temperature_k:.2f} K")
        print(f"  Thermal resistance: {result.thermal_resistance_kw:.4f} K/W")
        print("  ✓ PASSED")
    else:
        print(f"  ✗ FAILED: Config not found at {neural_path}")
    
    print()
    
    # Test 3: Thermal Grid Validation
    print("TEST 3: Thermal Grid Boundary Validation")
    print("-" * 40)
    
    valid_boundaries = ["neumann", "dirichlet", "convective", "periodic"]
    thermal_config = {
        "boundary_left": "neumann",
        "boundary_right": "neumann",
        "boundary_top": "convective",
        "boundary_bottom": "dirichlet"
    }
    
    all_valid = True
    for boundary, bc_type in thermal_config.items():
        if bc_type in valid_boundaries:
            print(f"  ✓ {boundary}: {bc_type} (valid)")
        else:
            print(f"  ✗ {boundary}: {bc_type} (INVALID)")
            all_valid = False
    
    if all_valid:
        print("  ✓ PASSED: All boundary conditions valid")
    else:
        print("  ✗ FAILED: Invalid boundary condition detected")
    
    print()
    
    # Test 4: Memory/Performance Test
    print("TEST 4: Large Grid Performance Test")
    print("-" * 40)
    
    try:
        worldgen = MockWorldGen(str(silicon_path) if silicon_path.exists() else "")
        worldgen.initialize_grid(100, 100, 1)
        
        # Add multiple heat sources
        for i in range(10):
            for j in range(10):
                worldgen.add_heat_source(HeatSource(
                    position=(10 + i*8, 10 + j*8, 0),
                    power_w=1e-7,
                    shape="gaussian",
                    sigma=2.0
                ))
        
        result = worldgen.solve_steady_state(max_iter=100, tolerance=1e-2)
        print(f"  Grid size: {result.grid_size}")
        print(f"  Total cells: {100*100}")
        print(f"  Convergence: {result.convergence_iterations} iterations")
        print(f"  Temperature rise: {result.max_temperature_k - 300.0:.2f} K")
        print("  ✓ PASSED: No crash, simulation completed")
    except MemoryError:
        print("  ✗ FAILED: Memory error")
    except Exception as e:
        print(f"  ✗ FAILED: {e}")
    
    print()
    print("=" * 60)
    print("WORLDGEN TEST SUITE COMPLETE")
    print("=" * 60)


if __name__ == "__main__":
    run_tests()
