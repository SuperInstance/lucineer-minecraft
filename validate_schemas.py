#!/usr/bin/env python3
"""
Minecraft 1.21.1 NeoForge Schema Validator
Validates Lucineer mod JSON schemas against Minecraft standard formats.
"""

import json
import os
import sys
from pathlib import Path
from typing import Dict, List, Tuple, Any

class SchemaValidator:
    """Validates Minecraft JSON schemas."""
    
    # Standard Minecraft dimension type fields (1.21.1)
    DIMENSION_TYPE_REQUIRED = {"name", "ambient_light", "bed_works", "coordinate_scale", 
                               "has_ceiling", "has_raids", "has_skylight", "height", 
                               "logical_height", "min_y", "natural", "piglin_safe", 
                               "respawn_anchor_works", "ultrawarm"}
    
    DIMENSION_TYPE_OPTIONAL = {"effects", "fixed_time", "infiniburn", "monster_spawn_block_light_limit",
                               "monster_spawn_light_level"}
    
    # Standard Minecraft dimension fields
    DIMENSION_REQUIRED = {"type", "generator"}
    
    # Standard noise settings fields
    NOISE_SETTINGS_REQUIRED = {"sea_level", "disable_mob_generation", "aquifers_enabled", 
                               "ore_veins_enabled", "default_block", "default_fluid", 
                               "noise", "noise_router", "surface_rule"}
    
    # Standard biome fields
    BIOME_REQUIRED = {"temperature", "downfall", "has_precipitation", "effects"}
    
    def __init__(self, base_path: str):
        self.base_path = Path(base_path)
        self.errors: List[str] = []
        self.warnings: List[str] = []
        self.passed: List[str] = []
        
    def validate_json_file(self, file_path: Path) -> Tuple[bool, Dict]:
        """Validate JSON syntax and return parsed content."""
        try:
            with open(file_path, 'r') as f:
                return True, json.load(f)
        except json.JSONDecodeError as e:
            self.errors.append(f"{file_path}: Invalid JSON - {e}")
            return False, {}
        except Exception as e:
            self.errors.append(f"{file_path}: Read error - {e}")
            return False, {}
    
    def validate_dimension_type(self, file_path: Path, data: Dict) -> bool:
        """Validate dimension type schema."""
        valid = True
        
        # Check required fields
        for field in self.DIMENSION_TYPE_REQUIRED:
            if field not in data:
                self.errors.append(f"{file_path.name}: Missing required field '{field}'")
                valid = False
        
        # Check value ranges
        if "height" in data:
            if data["height"] < 16 or data["height"] > 4096:
                self.warnings.append(f"{file_path.name}: height={data['height']} outside typical range (16-4096)")
        
        if "min_y" in data:
            if data["min_y"] < -2048 or data["min_y"] > 2048:
                self.warnings.append(f"{file_path.name}: min_y={data['min_y']} outside valid range")
        
        if "coordinate_scale" in data:
            if data["coordinate_scale"] <= 0:
                self.errors.append(f"{file_path.name}: coordinate_scale must be positive")
                valid = False
        
        # Check Lucineer-specific fields
        if "lucineer:thermal_properties" in data:
            thermal = data["lucineer:thermal_properties"]
            if "ambient_temperature_k" in thermal:
                if thermal["ambient_temperature_k"] < 0:
                    self.errors.append(f"{file_path.name}: Negative temperature not allowed")
                    valid = False
        
        return valid
    
    def validate_dimension(self, file_path: Path, data: Dict) -> bool:
        """Validate dimension schema."""
        valid = True
        
        for field in self.DIMENSION_REQUIRED:
            if field not in data:
                self.errors.append(f"{file_path.name}: Missing required field '{field}'")
                valid = False
        
        if "generator" in data:
            gen = data["generator"]
            if "type" not in gen:
                self.errors.append(f"{file_path.name}: Generator missing 'type' field")
                valid = False
            if "biome_source" not in gen and gen.get("type") != "minecraft:flat":
                self.warnings.append(f"{file_path.name}: Generator missing 'biome_source'")
        
        return valid
    
    def validate_noise_settings(self, file_path: Path, data: Dict) -> bool:
        """Validate noise settings schema."""
        valid = True
        
        for field in self.NOISE_SETTINGS_REQUIRED:
            if field not in data:
                self.errors.append(f"{file_path.name}: Missing required field '{field}'")
                valid = False
        
        # Check noise structure
        if "noise" in data:
            noise = data["noise"]
            required_noise = {"min_y", "height", "size_horizontal", "size_vertical"}
            for field in required_noise:
                if field not in noise:
                    self.warnings.append(f"{file_path.name}: noise.{field} missing")
        
        # Check for thermal grid extension
        if "lucineer:thermal_grid" in data:
            thermal = data["lucineer:thermal_grid"]
            valid_boundaries = {"neumann", "dirichlet", "convective", "periodic"}
            for boundary in ["boundary_left", "boundary_right", "boundary_top", "boundary_bottom"]:
                if boundary in thermal:
                    if thermal[boundary] not in valid_boundaries:
                        self.errors.append(f"{file_path.name}: Invalid boundary type '{thermal[boundary]}'")
                        valid = False
        
        return valid
    
    def validate_biome(self, file_path: Path, data: Dict) -> bool:
        """Validate biome schema."""
        valid = True
        
        for field in self.BIOME_REQUIRED:
            if field not in data:
                self.errors.append(f"{file_path.name}: Missing required field '{field}'")
                valid = False
        
        if "temperature" in data:
            if data["temperature"] < 0 or data["temperature"] > 2:
                self.warnings.append(f"{file_path.name}: temperature={data['temperature']} outside typical range")
        
        if "downfall" in data:
            if data["downfall"] < 0 or data["downfall"] > 1:
                self.warnings.append(f"{file_path.name}: downfall={data['downfall']} outside valid range (0-1)")
                valid = False
        
        return valid
    
    def validate_data_component(self, file_path: Path, data: Dict) -> bool:
        """Validate data component type schema."""
        valid = True
        
        required = ["name", "schema"]
        for field in required:
            if field not in data:
                self.errors.append(f"{file_path.name}: Missing required field '{field}'")
                valid = False
        
        if "schema" in data:
            schema = data["schema"]
            if "type" not in schema:
                self.errors.append(f"{file_path.name}: Schema missing 'type' field")
                valid = False
            if "properties" not in schema and schema.get("type") == "object":
                self.warnings.append(f"{file_path.name}: Object schema has no properties defined")
        
        return valid
    
    def validate_all(self) -> Tuple[int, int, int]:
        """Validate all JSON files in the minecraft directory."""
        files_validated = 0
        files_passed = 0
        files_failed = 0
        
        for json_file in self.base_path.rglob("*.json"):
            files_validated += 1
            success, data = self.validate_json_file(json_file)
            
            if not success:
                files_failed += 1
                continue
            
            # Determine file type and validate
            parts = json_file.parts
            if "dimension_type" in parts:
                valid = self.validate_dimension_type(json_file, data)
            elif "dimension" in parts and "dimension_type" not in parts:
                valid = self.validate_dimension(json_file, data)
            elif "noise_settings" in parts:
                valid = self.validate_noise_settings(json_file, data)
            elif "biome" in parts:
                valid = self.validate_biome(json_file, data)
            elif "data_component_type" in parts:
                valid = self.validate_data_component(json_file, data)
            else:
                valid = True
                self.warnings.append(f"{json_file.name}: Unknown file type")
            
            if valid:
                files_passed += 1
                self.passed.append(str(json_file.relative_to(self.base_path)))
            else:
                files_failed += 1
        
        return files_validated, files_passed, files_failed
    
    def print_report(self):
        """Print validation report."""
        print("=" * 60)
        print("LUCINEER MINECRAFT SCHEMA VALIDATION REPORT")
        print("=" * 60)
        print()
        
        print(f"✓ Passed: {len(self.passed)}")
        print(f"⚠ Warnings: {len(self.warnings)}")
        print(f"✗ Errors: {len(self.errors)}")
        print()
        
        if self.errors:
            print("ERRORS:")
            for error in self.errors:
                print(f"  ✗ {error}")
            print()
        
        if self.warnings:
            print("WARNINGS:")
            for warning in self.warnings:
                print(f"  ⚠ {warning}")
            print()
        
        if self.passed:
            print("PASSED FILES:")
            for p in self.passed:
                print(f"  ✓ {p}")
        
        print()
        print("=" * 60)
        return len(self.errors) == 0


if __name__ == "__main__":
    base_path = Path(__file__).parent
    validator = SchemaValidator(str(base_path))
    validator.validate_all()
    success = validator.print_report()
    sys.exit(0 if success else 1)
