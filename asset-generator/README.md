# Lucineer Asset Generator

**Version:** 1.0.0  
**For:** Minecraft 1.21.1 NeoForge Mod  
**Architecture:** Multi-provider cascade with vibe-coding

---

## Overview

The Asset Generator is a comprehensive system for creating Minecraft content through natural language, powered by multiple AI providers with intelligent routing.

### Features

| Feature | Description |
|---------|-------------|
| **Multi-LLM Routing** | DeepSeek (logic), Groq (speed), OpenAI (quality), Local (free) |
| **Image Generation** | DeepInfra, local Stable Diffusion, Minecraft-optimized |
| **Vibe-Coding** | Natural language to Minecraft commands |
| **Progressive Learning** | Endless lesson generation |
| **Circuit Breaker** | Automatic failover on provider errors |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Minecraft Mod (NeoForge)                  │
├─────────────────────────────────────────────────────────────┤
│  Vibe Engine │ Learning Engine │ Image Generator │ LLM Gen  │
├─────────────────────────────────────────────────────────────┤
│                      Cascade Router                          │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ Tier 0: LOCAL (Ollama/LM Studio)     - FREE, FAST      ││
│  │ Tier 1: FAST (Groq Llama)            - ~100ms          ││
│  │ Tier 2: BALANCED (DeepSeek)          - Good quality    ││
│  │ Tier 3: PREMIUM (OpenAI GPT-4o)      - Best quality    ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│                   Failure Tracker + Cache                    │
└─────────────────────────────────────────────────────────────┘
```

---

## API Endpoints

### Text Generation
```http
POST /generate
{
  "prompt": "Create a script to auto-farm wheat",
  "task_type": "script",
  "complexity": "moderate",
  "player_id": "player123"
}
```

### Image Generation
```http
POST /generate-image
{
  "prompt": "Diamond ore texture",
  "style": "pixel_art",
  "size": "64x64",
  "optimize_for_minecraft": true
}
```

### Vibe-Coding
```http
POST /vibe
{
  "natural_language": "Build me a small house with a garden",
  "player_id": "player123",
  "player_name": "Steve",
  "position": [100, 64, 200],
  "biome": "plains"
}
```

### Learning
```http
POST /lesson
{
  "player_id": "player123",
  "topic": "redstone_basics"
}
```

---

## Provider Configuration

### Environment Variables

```bash
# LLM Providers
DEEPSEEK_API_KEY=your-key
GROQ_API_KEY=your-key
OPENAI_API_KEY=your-key

# Image Providers
DEEPINFRA_API_KEY=your-key

# Local Models
LOCAL_LLM_URL=http://localhost:11434
LOCAL_SD_URL=http://localhost:7860
```

### Priority Order

1. **Local** (priority: 10) - Free, private, unlimited
2. **Groq** (priority: 9) - Fastest cloud (~100ms)
3. **DeepSeek** (priority: 8) - Balanced, great for reasoning
4. **OpenAI** (priority: 7) - Premium quality

---

## Minecraft Integration

### Data Components

| Component | Purpose |
|-----------|---------|
| `lucineer:vibe_request` | Natural language generation request |
| `lucineer:learning_session` | Progressive lesson tracking |
| `lucineer:image_request` | Asset generation request |

### Example: Vibe Request in-game

```java
// Create vibe request
VibeRequest request = new VibeRequest(
    "Build me a 9x9 wheat farm with water in the center",
    player.getUUID(),
    player.blockPosition(),
    player.level.dimension().location().toString()
);

// Send to backend
AssetGeneratorAPI.sendVibeRequest(request, response -> {
    if (response.success()) {
        // Execute generated actions
        for (Action action : response.actions()) {
            action.execute(player.level, player);
        }
        player.displayClientMessage(
            Component.literal(response.message()), 
            false
        );
    }
});
```

---

## Complexity-Based Routing

| Complexity | Keywords | Routed To |
|------------|----------|-----------|
| TRIVIAL | "hello", "yes", "no" | Local model |
| SIMPLE | Short prompts <30 words | Groq (fast) |
| MODERATE | "create", "build", "generate" | DeepSeek (balanced) |
| COMPLEX | "analyze", "design", "reason" | DeepSeek/OpenAI |
| CREATIVE | "imagine", "invent", "novel" | GPT-4o (best) |

---

## Progressive Learning

### Topic Tiers

1. **Beginner**: mining, building, farming, combat_basics
2. **Intermediate**: redstone_basics, enchanting, brewing, mob_farms
3. **Advanced**: redstone_computers, iron_farms, raid_farms
4. **Expert**: wither_skeleton_farms, gold_farms, mega_builds
5. **Master**: optimization_theory, teaching_others

### XP System

| Action | XP |
|--------|-----|
| Lesson Complete | 100 |
| Challenge Complete | 250 |
| Perfect Score | 500 |
| Teach Another Player | 1000 |

---

## Running the Server

```bash
# Install dependencies
pip install fastapi uvicorn httpx pydantic

# Run the API server
python -m asset_generator.api.main

# Or with uvicorn directly
uvicorn asset_generator.api.main:app --host 0.0.0.0 --port 8080
```

---

## Directory Structure

```
minecraft/
├── asset-generator/
│   ├── api/
│   │   ├── main.py          # FastAPI endpoints
│   │   └── providers.py     # Core provider abstractions
│   ├── llm/
│   │   └── providers.py     # DeepSeek, Groq, OpenAI, Local
│   ├── image/
│   │   └── providers.py     # DeepInfra, local SD
│   ├── vibe/
│   │   └── engine.py        # Vibe-coding + Learning engine
│   └── config/
│       └── settings.py      # Configuration management
│
├── data/lucineer/
│   ├── data_component_type/
│   │   ├── vibe_request.json
│   │   ├── learning_session.json
│   │   └── image_request.json
│   └── config/
│       └── cascade_router.json
│
├── validate_schemas.py
├── worldgen_test.py
└── README.md
```

---

## Based On

- **Minewright** (`github.com/superinstance/minewright`) - API abstraction patterns
- **Lucineer** (`github.com/superinstance/lucineer`) - Thermal simulation concepts

---

## License

MIT License
