# LUCINEER MINECRAFT MOD - COMPREHENSIVE DESIGN SPECIFICATION
## A Gamified STEM Learning Platform That Grows With The Player

---

# PART 1: ARCHITECTURAL OVERVIEW

## Vision Statement

Transform Lucineer's complete platform - voxel explorer, cell builder, manufacturing, thermal simulation, and MIST game - into a seamless Minecraft 1.21.1 experience where players:

1. **Explore** chip anatomy through voxel cross-sections
2. **Build** neural networks with spreadsheet AI mechanics
3. **Design** chips that actually work (thermal simulation)
4. **Learn** progressively from age 5 to professional engineer
5. **Craft** real STEM projects with AI-assisted generation
6. **Grow** through endless AI-generated lessons

---

## Platform Components Mapping

| Lucineer Component | Minecraft Implementation | Learning Outcome |
|-------------------|------------------------|------------------|
| **Voxel Explorer** | Cross-section dimension | Technology anatomy |
| **Cell Builder** | Spreadsheet blocks | Neural network fundamentals |
| **Manufacturing** | Fab dimension + minigames | Semiconductor physics |
| **Thermal Simulation** | Real-time heat overlay | FEA concepts |
| **MIST Game** | Puppy companion mob | AI learning metaphors |
| **Learning Hub** | Quest system | Progressive education |
| **RTL Studio** | Verilog blocks | HDL programming |
| **Progressive Learning** | Skill tree + XP | Endless growth |

---

# PART 2: DIMENSIONAL DESIGN

## 2.1 Silicon Foundry Dimension
**Source**: `voxel-explorer/page.tsx` + `manufacturing/page.tsx`

### Structure
```
┌─────────────────────────────────────────────────────────────────┐
│  LAYER 12: Air/Vacuum                                           │
├─────────────────────────────────────────────────────────────────┤
│  LAYER 11: Heat Spreader (Copper)                               │
├─────────────────────────────────────────────────────────────────┤
│  LAYER 10: Thermal Interface Material                           │
├─────────────────────────────────────────────────────────────────┤
│  LAYER 9: Die Backside (Silicon substrate)                      │
├─────────────────────────────────────────────────────────────────┤
│  LAYER 8: Active Layer (Transistors)                            │
├─────────────────────────────────────────────────────────────────┤
│  LAYER 7-1: Metal Interconnects (M1-M7)                         │
├─────────────────────────────────────────────────────────────────┤
│  LAYER 0: Package Substrate                                     │
└─────────────────────────────────────────────────────────────────┘
```

### Block Types (per manufacturing steps)

| Step | Block | Physics |
|------|-------|---------|
| Silicon Extraction | `raw_silicon_block` | Heat transforms sand |
| Czochralski Growth | `silicon_ingot` | Crystal pulling mechanic |
| Wafer Slicing | `silicon_wafer` | Diamond wire cutting |
| Photolithography | `photoresist_block` | UV exposure pattern |
| Etching | `etched_silicon` | Chemical/plasma removal |
| Ion Implantation | `doped_silicon` | Dopant depth mechanics |
| Metallization | `interconnect_block` | Resistance calculation |
| Mask-Locked Weights | `ternary_weight_block` | {-1, 0, +1} encoding |
| Packaging | `packaged_chip` | Thermal resistance |
| Testing | `tested_chip` | Yield calculation |

### Interactive Characters (from Voxel Explorer)

Each character guides players at their level:

```json
{
  "characters": {
    "volt": {
      "role": "Voltage Guide",
      "emoji": "⚡",
      "dialogue": {
        "elementary": "I'm Volt! I carry electricity like a lightning bolt! ZAP!",
        "middle": "I measure electrical potential difference - the push that moves electrons.",
        "high": "I represent voltage (V), the electromotive force. V = IR is my law!"
      }
    },
    "bit": {
      "role": "Data Carrier", 
      "emoji": "📦",
      "dialogue": {
        "elementary": "I can be a ZERO or a ONE! 🎭",
        "middle": "I'm the smallest unit of digital data. Eight of me = 1 Byte!",
        "high": "I encode information entropy - one bit = one binary decision."
      }
    },
    "fetch": {
      "role": "Memory Retriever",
      "emoji": "🐕",
      "dialogue": {
        "elementary": "Going to get your data! *woof* 🐕",
        "middle": "I retrieve instructions and data from memory for the CPU.",
        "high": "I execute: MAR ← PC, MDR ← M[MAR], IR ← MDR, PC ← PC+1"
      }
    },
    "switch": {
      "role": "Transistor Gatekeeper",
      "emoji": "🚪",
      "dialogue": {
        "elementary": "Gate OPEN! Come on through! 🚪",
        "middle": "I'm a transistor - a tiny switch that controls electron flow.",
        "high": "Vgs > Vth - inversion layer formed. Channel modulation."
      }
    }
  }
}
```

---

## 2.2 Neural Synapse Dimension
**Source**: `biological_thermal.py` + `mac_array.py`

### Bio-Inspired Thermal Design

```
Synaptic Spine Structure (translated to Minecraft):

         ┌─────────────┐
         │ Spine Head  │  ← Active element (neuron cell)
         │  (Heat Gen) │
         └──────┬──────┘
                │
         ┌──────▼──────┐
         │ Spine Neck  │  ← Thermal isolator (narrow passage)
         │ (High R_th) │
         └──────┬──────┘
                │
    ───────────┴───────────
    Dendrite (Main thermal path)
```

### Thermal Mechanics

```python
# In-game thermal simulation (simplified for real-time)

class InGameThermalSimulation:
    """Real-time thermal overlay for Minecraft"""
    
    def __init__(self):
        self.materials = {
            "silicon": {"k": 148.0, "alpha": 9.08e-5},
            "copper": {"k": 400.0, "alpha": 1.11e-4},
            "sio2": {"k": 1.4, "alpha": 8.3e-7},
            "thermal_paste": {"k": 5.0, "alpha": 2.0e-6}
        }
        
    def calculate_heat_spread(self, source_pos, power_w, material):
        """Calculate heat spread radius based on material properties"""
        k = self.materials[material]["k"]
        # Simplified: heat_spread ∝ √(k × P)
        return math.sqrt(k * power_w * 0.001)  # blocks
        
    def get_block_temperature(self, pos, sources, dt):
        """Get temperature at position based on heat equation"""
        # Simplified 2D heat equation
        T = 300.0  # ambient
        for source in sources:
            dist = distance(pos, source.pos)
            if dist < source.thermal_radius:
                T += source.power * source.efficiency / (dist + 1)
        return T
```

---

## 2.3 Cell Builder Workshop
**Source**: `cell-builder/page.tsx`

### Spreadsheet Neural Blocks

```
┌──────────────────────────────────────────────────────────────────────┐
│                    NEURAL NETWORK SPREADSHEET                         │
├────────┬────────┬────────┬────────┬────────┬────────┬────────────────┤
│   A    │   B    │   C    │   D    │   E    │   F    │      TYPE      │
├────────┼────────┼────────┼────────┼────────┼────────┼────────────────┤
│  0.5   │  0.8   │  0.3   │  0.1   │  0.6   │  0.4   │  INPUT         │
├────────┼────────┼────────┼────────┼────────┼────────┼────────────────┤
│  0.7   │ -0.3   │  0.2   │ -0.5   │  0.9   │ -0.1   │  WEIGHT        │
├────────┼────────┼────────┼────────┼────────┼────────┼────────────────┤
│  0.1   │  0.0   │  0.2   │  0.1   │ -0.1   │  0.0   │  BIAS          │
├────────┼────────┼────────┼────────┼────────┼────────┼────────────────┤
│ =NEURON│(A1:F1, │A2:F2,  │ A3:F3) │        │        │  FORMULA       │
└────────┴────────┴────────┴────────┴────────┴────────┴────────────────┘
```

### Block Functions (AI Functions from Cell Builder)

```typescript
// In-game block types for cell builder

const AI_BLOCK_FUNCTIONS = {
  // Basic Neural Operations
  NEURON: {
    inputs: 3,
    syntax: "=NEURON(inputs, weights, bias)",
    description: "Computes neuron output: σ(Σ(wi*xi) + b)",
    crafting: ["input_block", "weight_block", "bias_block"]
  },
  
  // Activation Functions
  RELU: {
    inputs: 1,
    syntax: "=RELU(value)",
    description: "max(0, x)",
    visual: "Green glow when active"
  },
  SIGMOID: {
    inputs: 1,
    syntax: "=SIGMOID(value)",
    description: "1 / (1 + e^-x)",
    visual: "Smooth gradient output"
  },
  TERNARY: {
    inputs: 1,
    syntax: "=TERNARY(value)",
    description: "Convert to {-1, 0, +1} for BitNet",
    visual: "Red/Gray/Green coloring"
  },
  
  // Layer Operations
  DENSE: {
    inputs: 3,
    syntax: "=DENSE(inputs, weights, bias)",
    description: "Fully connected layer",
    crafting: ["input_array", "weight_matrix", "bias_vector"]
  },
  
  // Attention (Advanced)
  ATTENTION: {
    inputs: 3,
    syntax: "=ATTENTION(Q, K, V)",
    description: "Scaled dot-product attention",
    unlock_level: 50
  }
};
```

---

# PART 3: PROGRESSIVE LEARNING ENGINE

## 3.1 Skill Tree Architecture
**Source**: `prisma/schema.prisma` + `learning/page.tsx`

### 10-Tier Progression System

```
                        ┌─────────────────┐
                        │   TIER 10:      │
                        │   MASTER        │
                        │   (Teacher)     │
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
       ┌──────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐
       │   TIER 9:   │    │   TIER 8:   │    │   TIER 7:   │
       │ Expert      │    │ Advanced    │    │ Senior      │
       │ Research    │    │ Design      │    │ Engineer    │
       └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
              │                  │                  │
       ┌──────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐
       │   TIER 6:   │    │   TIER 5:   │    │   TIER 4:   │
       │ AI Research │    │ Chip Design │    │ College     │
       └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
              │                  │                  │
       ┌──────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐
       │   TIER 3:   │    │   TIER 2:   │    │   TIER 1:   │
       │ High School│    │ Middle      │    │ Elementary  │
       │ (15-18)     │    │ (11-14)     │    │ (5-10)      │
       └─────────────┘    └─────────────┘    └─────────────┘
```

### Learning Modules (from cell-builder/page.tsx)

```json
{
  "modules": [
    {
      "id": "intro-neurons",
      "title": "Introduction to Neurons",
      "tier": 1,
      "age_range": "5-10",
      "difficulty": "Beginner",
      "lessons": 5,
      "duration_minutes": 15,
      "topics": ["What is a neuron?", "Inputs and outputs", "Simple calculations"],
      "xp_reward": 100,
      "unlock_requirements": []
    },
    {
      "id": "cell-formulas",
      "title": "AI Formulas in Cells",
      "tier": 2,
      "age_range": "11-14",
      "difficulty": "Beginner",
      "lessons": 8,
      "duration_minutes": 25,
      "topics": ["Formula syntax", "Cell references", "Neural functions"],
      "xp_reward": 200,
      "unlock_requirements": ["intro-neurons"]
    },
    {
      "id": "build-network",
      "title": "Build a Neural Network",
      "tier": 3,
      "age_range": "15-18",
      "difficulty": "Intermediate",
      "lessons": 12,
      "duration_minutes": 45,
      "topics": ["Layer design", "Forward pass", "Activation functions"],
      "xp_reward": 500,
      "unlock_requirements": ["cell-formulas"]
    },
    {
      "id": "train-model",
      "title": "Train Your Model",
      "tier": 4,
      "difficulty": "Advanced",
      "lessons": 15,
      "duration_minutes": 60,
      "topics": ["Gradient descent", "Loss functions", "Weight updates"],
      "xp_reward": 1000,
      "unlock_requirements": ["build-network"]
    },
    {
      "id": "quantization-chips",
      "title": "Quantization for Chips",
      "tier": 5,
      "difficulty": "Expert",
      "lessons": 8,
      "duration_minutes": 40,
      "topics": ["Ternary weights", "INT4 quantization", "Hardware mapping"],
      "xp_reward": 2000,
      "unlock_requirements": ["train-model"]
    }
  ]
}
```

---

## 3.2 Endless Lesson Generator

### Integration with Vibe Engine

```python
class EndlessLessonGenerator:
    """Generates infinite progressive lessons using LLM cascade"""
    
    def __init__(self, cascade_router):
        self.router = cascade_router
        self.topic_tree = self._build_topic_tree()
        
    def _build_topic_tree(self):
        """Build hierarchical topic tree from Lucineer content"""
        return {
            "tier_1_beginner": {
                "mining": {
                    "subtopics": ["ore_types", "mining_tools", "safety"],
                    "practical": ["craft_pickaxe", "find_iron", "build_mine"],
                    "extends_to": ["automation_basics"]
                },
                "electronics_basics": {
                    "subtopics": ["voltage", "current", "resistance"],
                    "practical": ["build_battery", "light_led", "simple_circuit"],
                    "characters": ["volt", "bit", "switch"]
                }
            },
            "tier_2_intermediate": {
                "transistor_logic": {
                    "subtopics": ["nmos", "pmos", "logic_gates"],
                    "practical": ["build_inverter", "build_and_gate", "build_cpu"],
                    "extends_to": ["digital_design"]
                },
                "neural_basics": {
                    "subtopics": ["neurons", "weights", "forward_pass"],
                    "practical": ["build_neuron", "train_simple", "make_prediction"],
                    "extends_to": ["deep_learning"]
                }
            },
            # ... tiers 3-10
        }
    
    async def generate_next_lesson(self, player_profile):
        """Generate personalized next lesson using LLM"""
        
        # Analyze player's interests and skill gaps
        interests = player_profile.get("interests", [])
        completed = player_profile.get("completed_lessons", [])
        skill_levels = player_profile.get("skill_levels", {})
        
        # Find appropriate topic
        topic = self._select_topic(interests, completed, skill_levels)
        
        # Calculate difficulty
        difficulty = self._calculate_difficulty(topic, skill_levels)
        
        # Generate lesson using LLM
        prompt = f"""
        Create a Minecraft lesson about: {topic}
        Difficulty: {difficulty}/10
        Player's completed lessons: {completed[-5:]}
        
        Output JSON:
        {{
          "title": "...",
          "concepts": ["concept1", "concept2"],
          "steps": [
            "Step 1: ...",
            "Step 2: ..."
          ],
          "challenge": "Build X to demonstrate Y",
          "reward": {{ "xp": 100, "items": [...] }},
          "next_topic": "suggested next topic"
        }}
        """
        
        response = await self.router.route(GenerationRequest(
            prompt=prompt,
            task_type="lesson",
            complexity=TaskComplexity.CREATIVE,
            temperature=0.8
        ))
        
        lesson = self._parse_lesson(response.content)
        lesson["topic"] = topic
        lesson["difficulty"] = difficulty
        return lesson
```

---

# PART 4: MIST COMPANION SYSTEM

## 4.1 Puppy Mob Implementation
**Source**: `mist/page.tsx`

### Progressive Revelation Mechanics

```java
// NeoForge entity for MIST puppy companion

public class MistPuppyEntity extends TamableAnimal implements IRoamingSound {
    
    // Teaching states (from TEACHINGS in MIST game)
    public enum TeachingStage {
        IDENTITY,    // "You are a dog. A special kind of dog."
        SHEEP,       // "Sheep are gentle. They follow each other."
        RAMS,        // "Not all sheep are the same."
        WORK,        // "Every dog on this farm has a job."
        FAMILY       // "Your mother was a great working dog."
    }
    
    private TeachingStage currentStage;
    private int teachingIndex;
    private List<String> discoveredInsights;
    
    // AI learning metaphors (from MIST Parent Layer)
    public static final Map<String, String> AI_CONCEPTS = Map.of(
        "Tabula Rosa", "Your child experiences being a blank-slate AI model",
        "Knowledge Distillation", "Each generation represents AI knowledge transfer",
        "Progressive Revelation", "Complexity revealed gradually, like training specialized models"
    );
    
    // Player interaction triggers discovery
    public void onPlayerDiscover(Player player, DiscoveryType type) {
        // Trigger appropriate teaching
        String teaching = getNextTeaching(type);
        sendDialogue(player, teaching);
        
        // Track insight for skill progression
        String insight = extractInsight(teaching);
        discoveredInsights.add(insight);
        
        // Award XP
        player.awardXP(10);
        
        // Check for level up
        if (discoveredInsights.size() >= INSIGHTS_PER_LEVEL) {
            levelUp();
            currentStage = nextStage();
        }
    }
    
    // Knowledge transfer (puppy teaching puppies)
    public MistPuppyEntity breed(MistPuppyEntity other) {
        MistPuppyEntity puppy = super.breed(other);
        
        // Transfer learned insights to offspring
        // This models knowledge distillation in AI
        puppy.inheritInsights(this.getBestInsights(), other.getBestInsights());
        
        return puppy;
    }
}
```

---

# PART 5: THERMAL SIMULATION INTEGRATION

## 5.1 Real-Time Thermal Overlay
**Source**: `thermal_simulation/*.py`

### Block Thermal Properties

```json
{
  "thermal_materials": {
    "silicon": {
      "thermal_conductivity_w_mk": 148.0,
      "specific_heat_j_kgk": 700.0,
      "density_kg_m3": 2329.0,
      "diffusivity_m2_s": 9.08e-5,
      "color_hot": "#FF4444",
      "color_cold": "#4444FF"
    },
    "copper_damascene": {
      "thermal_conductivity_w_mk": 300.0,
      "specific_heat_j_kgk": 385.0,
      "density_kg_m3": 8960.0,
      "diffusivity_m2_s": 9.75e-5,
      "heat_spread_rate": 1.5
    },
    "sio2": {
      "thermal_conductivity_w_mk": 1.4,
      "specific_heat_j_kgk": 745.0,
      "density_kg_m3": 2200.0,
      "diffusivity_m2_s": 8.3e-7,
      "insulation_factor": 0.8
    },
    "thermal_paste": {
      "thermal_conductivity_w_mk": 5.0,
      "interface_resistance_k_w": 0.02
    }
  }
}
```

### Heat Source Blocks

```java
public class HeatSourceBlock extends Block {
    
    // From HeatSource dataclass in core_thermal.py
    public enum HeatShape {
        POINT,      // Single block heat source
        GAUSSIAN,   // Distributed with sigma
        UNIFORM,    // Even distribution over area
        LINE,       // Linear heat source
        RECTANGULAR // Area heat source
    }
    
    private double powerWatts;
    private HeatShape shape;
    private double sigma;
    private boolean active;
    
    // Time profiles from image_request.json
    public enum TimeProfile {
        CONSTANT,
        PULSE,
        PERIODIC,
        RAMP,
        CUSTOM
    }
    
    // Calculate heat spread using thermal equation
    public Map<BlockPos, Double> calculateHeatSpread(Level level, BlockPos origin) {
        Map<BlockPos, Double> heatMap = new HashMap<>();
        
        // Get surrounding blocks' thermal properties
        for (BlockPos pos : getSpreadArea(origin)) {
            BlockState state = level.getBlockState(pos);
            MaterialProps props = ThermalRegistry.getMaterial(state);
            
            double distance = pos.distManhattan(origin);
            double heat = calculateHeatAt(distance, props);
            heatMap.put(pos, heat);
        }
        
        return heatMap;
    }
    
    // Gaussian heat distribution
    private double calculateHeatAt(double distance, MaterialProps props) {
        if (shape == HeatShape.GAUSSIAN) {
            return powerWatts * Math.exp(-distance*distance / (2 * sigma * sigma));
        }
        // ... other shapes
    }
}
```

---

# PART 6: VIBE-CODING FOR MINECRAFT

## 6.1 Natural Language to Minecraft Commands
**Source**: `vibe/engine.py`

### Vibe Request System

```typescript
// In-game vibe request handling

interface VibeRequest {
  naturalLanguage: string;
  playerContext: {
    position: Vec3d;
    biome: string;
    inventory: ItemStack[];
    nearbyBlocks: Block[];
    nearbyEntities: Entity[];
    recentActions: string[];
  };
}

interface VibeResponse {
  message: string;
  actions: GameAction[];
  generatedCode?: string;
  suggestions: string[];
  learningOpportunities: string[];
}

// Example vibe requests
const EXAMPLE_VIBES = [
  {
    input: "Build me a simple house",
    output: {
      message: "I'll help you build a cozy starter home!",
      actions: [
        { type: "fill", block: "oak_planks", from: [0,0,0], to: [6,4,8] },
        { type: "fill", block: "air", from: [1,1,1], to: [5,3,7] },
        { type: "place", block: "oak_door", pos: [3,1,0] }
      ]
    }
  },
  {
    input: "Make me a chip that does AI",
    output: {
      message: "Let's design an inference chip! I'll guide you through placing ternary weights.",
      actions: [
        { type: "open_gui", gui: "chip_designer" },
        { type: "show_tutorial", topic: "ternary_weights" }
      ],
      learningOpportunities: ["BitNet architecture", "Ternary encoding {-1, 0, +1}", "Thermal management"]
    }
  },
  {
    input: "Explain how this transistor works",
    output: {
      message: "This transistor is like a tiny switch! When voltage hits the gate...",
      actions: [
        { type: "highlight", blocks: ["transistor_gate", "transistor_source", "transistor_drain"] },
        { type: "show_animation", animation: "electron_flow" }
      ]
    }
  }
];
```

---

# PART 7: ASSET GENERATION INTEGRATION

## 7.1 Texture Generation
**Source**: `asset-generator/image/providers.py`

### Minecraft-Optimized Generation

```python
class MinecraftAssetGenerator:
    """Generates Minecraft-optimized textures using AI"""
    
    # Style presets from Lucineer's image generation
    STYLES = {
        "pixel_art": {
            "prompt_suffix": "pixel art style, minecraft aesthetic, 16x16, clean edges",
            "default_size": (64, 64),
            "optimize": True,
            "palette_limit": 256
        },
        "circuit_texture": {
            "prompt_suffix": "silicon chip cross-section, metallic, technical diagram",
            "default_size": (128, 128),
            "tileable": True
        },
        "transistor_diagram": {
            "prompt_suffix": "transistor cross-section, educational diagram, labeled",
            "default_size": (256, 256)
        }
    }
    
    async def generate_texture(self, request: TextureRequest):
        """Generate a Minecraft texture using DeepInfra or local SD"""
        
        # Enhance prompt with style
        style = self.STYLES.get(request.style, "pixel_art")
        enhanced_prompt = f"{request.prompt}, {style['prompt_suffix']}"
        
        # Generate via cascade router
        response = await self.image_router.route(ImageGenerationRequest(
            prompt=enhanced_prompt,
            size=style["default_size"],
            style=ImageStyle.PIXEL_ART if request.style == "pixel_art" else ImageStyle.REALISTIC
        ))
        
        # Optimize for Minecraft
        if style.get("optimize"):
            image_data = MinecraftTextureOptimizer.to_minecraft_texture(
                response.image_data,
                size=16 if request.item_type == "item" else 64
            )
        
        return TextureResponse(
            image_data=image_data,
            resource_path=f"lucineer:textures/{request.category}/{request.name}.png"
        )
```

---

# PART 8: DATABASE SCHEMA

## 8.1 Player Progression Model
**Source**: `prisma/schema.prisma`

```prisma
model MinecraftPlayer {
  id            String   @id @default(cuid())
  uuid          String   @unique
  
  // From User model
  xp            Int      @default(0)
  level         Int      @default(1)
  prestige      Int      @default(0)
  streak        Int      @default(0)
  lastActiveAt  DateTime?
  
  // Minecraft-specific
  gamemode      String   @default("survival")
  currentDimension String @default("overworld")
  
  // Learning progress (from Lucineer schema)
  skillLevels   Json     // { "electronics": 5, "neural": 3, ... }
  completedLessons Json  // ["intro-neurons", "cell-formulas", ...]
  discoveredInsights Json // From MIST puppy
  achievements  Json     // Unlocked achievements
  
  // Chip designs
  chipDesigns   Json     // Saved chip configurations
  thermalSimulations Json // Saved thermal simulations
  
  // Assets created
  createdTextures Json   // Generated texture IDs
  
  createdAt     DateTime @default(now())
  updatedAt     DateTime @updatedAt
}

model ChipDesign {
  id          String   @id @default(cuid())
  playerId    String
  player      MinecraftPlayer @relation(fields: [playerId], references: [id])
  
  // From LUCI chip specifications
  variant     String   // LUCI-1, LUCI-3, LUCI-7, CUSTOM
  parameters  String   // 1B, 3B, 7B
  powerWatts  Float
  tokensPerSecond Int
  
  // Thermal design
  thermalProfile Json   // Heat source positions, materials
  
  // Performance
  validated   Boolean  @default(false)
  yieldEstimate Float?
  
  createdAt   DateTime @default(now())
}

model ThermalSimulation {
  id          String   @id @default(cuid())
  playerId    String
  
  // From SimulationConfig
  gridSize    Json     // { nx, ny, nz }
  cellSizeNm  Float
  materials   Json     // Material assignments
  heatSources Json     // Heat source definitions
  boundaries  Json     // Boundary conditions
  
  // Results
  maxTempK    Float?
  avgTempK    Float?
  converged   Boolean  @default(false)
  
  createdAt   DateTime @default(now())
}

model GeneratedAsset {
  id          String   @id @default(cuid())
  playerId    String
  
  // From image_request.json
  prompt      String
  style       String
  assetType   String   // block_texture, item_texture, entity_skin
  size        String   // 16x16, 32x32, 64x64
  
  // Generation info
  provider    String   // deepinfra, local_sd
  seed        Int?
  
  // Result
  texturePath String?
  
  createdAt   DateTime @default(now())
}
```

---

# PART 9: IMPLEMENTATION CHECKLIST

## Phase 1: Core Infrastructure
- [ ] NeoForge mod scaffolding
- [ ] Dimension registration (Silicon Foundry, Neural Synapse)
- [ ] Basic block/item registration
- [ ] Data component types

## Phase 2: Learning Systems
- [ ] Skill tree implementation
- [ ] XP/Level system
- [ ] Lesson generator integration
- [ ] Progress persistence

## Phase 3: Thermal Simulation
- [ ] Thermal material blocks
- [ ] Heat source blocks
- [ ] Thermal overlay renderer
- [ ] Simulation persistence

## Phase 4: Cell Builder
- [ ] Spreadsheet block grid
- [ ] AI function blocks
- [ ] Network visualization
- [ ] Formula execution

## Phase 5: MIST Companion
- [ ] Puppy mob entity
- [ ] Teaching system
- [ ] Discovery mechanics
- [ ] Breeding/inheritance

## Phase 6: Asset Generation
- [ ] API server integration
- [ ] Texture generator
- [ ] Model downloader
- [ ] Resource pack builder

## Phase 7: Vibe Engine
- [ ] Natural language processor
- [ ] Action execution
- [ ] Context awareness
- [ ] Suggestion system

---

# PART 10: ENVIRONMENT CONFIGURATION

## Required API Keys

```bash
# LLM Providers
DEEPSEEK_API_KEY=your-key      # For reasoning tasks
GROQ_API_KEY=your-key          # For fast responses
OPENAI_API_KEY=your-key        # For premium features

# Image Generation
DEEPINFRA_API_KEY=your-key     # For textures/assets

# Local Models
LOCAL_LLM_URL=http://localhost:11434
LOCAL_SD_URL=http://localhost:7860
```

## Minecraft Configuration

```toml
# lucineer-common.toml

[general]
  # Enable thermal simulation overlay
  thermalOverlay = true
  # Enable AI generation features
  aiGenerationEnabled = true
  # Cache generated assets
  assetCacheSize = 100

[learning]
  # Progressive difficulty scaling
  adaptiveDifficulty = true
  # XP multiplier
  xpMultiplier = 1.0
  # Enable endless lesson generation
  endlessLearning = true

[thermal]
  # Simulation update rate (ticks)
  updateInterval = 20
  # Maximum heat sources per chunk
  maxHeatSourcesPerChunk = 100
  # Visualize heat flow
  showHeatFlow = true

[vibe]
  # Enable natural language commands
  vibeEnabled = true
  # Provider priority (local first)
  preferLocalModels = true
  # Timeout for API calls (ms)
  apiTimeout = 30000
```

---

This design document provides the complete blueprint for integrating all Lucineer features into a Minecraft mod that teaches STEM progressively from age 5 to professional engineer level.
