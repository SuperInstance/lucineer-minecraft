# Bot-Script-Agent Integration Design

## Executive Summary

This document describes a **dual-process cognitive architecture** for Minecraft STEM learning that dramatically reduces API costs (by ~97%) while teaching players the fundamental difference between bots (rule-based automation) and agents (AI-powered reasoning).

## Core Philosophy

### The Problem
- Traditional AI assistants call LLM APIs for every action
- This is expensive (~$0.002 per call) and slow (~500-3000ms)
- Most Minecraft actions are repetitive and don't need AI

### The Solution
- **Scripts (Bots)**: Handle routine tasks locally, instantly, for free
- **Agents**: Handle novel situations, learning, and edge cases
- **Triggers**: Automatically escalate when scripts can't proceed

```
Cost Comparison (per hour of gameplay):
┌─────────────────────────────────────────────────────┐
│  WITHOUT SYSTEM: 200 actions × $0.002 = $0.40/hour │
│  WITH SYSTEM:    5 actions × $0.002   = $0.01/hour │
│  SAVINGS:                                97.5%     │
└─────────────────────────────────────────────────────┘
```

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      MINECRAFT CLIENT                           │
│  Player commands, GUI interactions, in-game events              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NEOFORGE MOD LAYER                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ BotScript    │  │ AgentTrigger │  │ MCPConnection│          │
│  │ Component    │  │ Component    │  │ Component    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                              │                                   │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              ExecutionStateManager                      │    │
│  │  • Maintains action queue                               │    │
│  │  • Tick-based execution (non-blocking)                  │    │
│  │  • Context gathering for triggers                       │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/WebSocket
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    LOCAL MCP SERVER (Port 3031)                 │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Complexity   │  │ ScriptCache  │  │ SkillLibrary │          │
│  │ Analyzer     │  │              │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                              │                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Trigger      │  │ AgentEscal.  │  │ LLM Provider │          │
│  │ Evaluator    │  │ Manager      │  │ (fallback)   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                 │
│  ENDPOINTS:                                                     │
│  POST /process  - Process command (script or agent)            │
│  GET  /metrics  - Performance metrics                           │
│  GET  /scripts  - List available scripts                        │
│  GET  /learn/*  - Educational content                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ (only when needed)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CLOUD LLM PROVIDERS                          │
│  DeepSeek (logic) | Groq (speed) | OpenAI (quality)            │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

### 1. Command Processing (Fast Path - Script)

```
Player: "/bot strip mine 100 blocks"
    │
    ▼
ComplexityAnalyzer: "SIMPLE" (pattern match: "strip.*mine")
    │
    ▼
ScriptCache.findSimilar("strip mine"): Match! (95% confidence)
    │
    ▼
Execute Script: mining_strip_tunnel_v1
    │   - length: 100
    │   - y_level: -59
    │   - torch_interval: 7
    │
    ▼
Result: 100 blocks mined, 14 torches placed
    │
    ▼
Metrics: scripts_executed++, api_calls_saved++
    │
    ▼
Educational Note: "🤖 BOT: This task was handled by a pre-programmed 
script. Fast (50ms), Free ($0), Reliable (98% success)."
```

### 2. Command Processing (Slow Path - Agent)

```
Player: "/bot design a novel redstone calculator"
    │
    ▼
ComplexityAnalyzer: "COMPLEX" (keyword: "novel")
    │
    ▼
ScriptCache.findSimilar(): No match found
    │
    ▼
TriggerEvaluator: "creative_request" trigger fires
    │
    ▼
AgentEscalationManager: Create escalation request
    │
    ▼
LLM Provider: Generate novel design
    │
    ▼
Response: Creative calculator design
    │
    ▼
SkillGenerator: Extract pattern for future use
    │
    ▼
Metrics: agents_triggered++, new skill created
    │
    ▼
Educational Note: "🧠 AGENT: This required AI creativity.
Slower (2000ms), Cost ($0.002), but handles novel situations."
```

### 3. Edge Case Escalation (Script → Agent)

```
Script: mining_strip_tunnel_v1 executing...
    │
    ▼
Context Check: nearby_blocks includes "minecraft:lava"
    │
    ▼
TriggerEvaluator: "lava_safety" trigger fires!
    │
    ▼
Script: PAUSED
    │
    ▼
AgentEscalationManager: Create safety escalation
    │
    ▼
LLM Provider: "Lava detected! Options: avoid, bridge_over, collect"
    │
    ▼
Player Response: "bridge over"
    │
    ▼
Script: RESUMED with new parameter: {lava_handling: "bridge_over"}
    │
    ▼
Educational Note: "⚠️ ESCALATION: Script encountered unexpected 
condition. Agent provided creative solution. Script continues."
```

## Component Specifications

### BotScript Component
```json
{
  "script_id": "string",
  "script_name": "string",
  "script_type": "ATOMIC | SEQUENCE | PARALLEL | LOOP | CONDITIONAL",
  "actions": "ActionNode[]",
  "parameters": "ParameterDefinition[]",
  "triggers": "TriggerCondition[]",
  "execution_metrics": {
    "success_rate": "float",
    "execution_count": "int"
  },
  "tier": "BUILT_IN | LEARNED | PROPOSED"
}
```

### AgentTrigger Component
```json
{
  "trigger_id": "string",
  "category": "SAFETY | PERFORMANCE | LEARNING | EDGE_CASE | USER_REQUEST",
  "condition": {
    "type": "THRESHOLD | PATTERN | STATE_CHANGE | TIMEOUT | ERROR",
    "metric": "string",
    "operator": "< | > | == | != | includes | matches",
    "value": "any"
  },
  "escalation_action": {
    "type": "PAUSE_AND_ASK | CONTINUE_WITH_AGENT | SWITCH_TO_AGENT",
    "prompt_template": "string",
    "fallback_behavior": "retry | abort | skip | manual"
  }
}
```

### ExecutionState Component
```json
{
  "current_mode": "IDLE | SCRIPT_RUNNING | AGENT_THINKING | WAITING_INPUT | ERROR_RECOVERY",
  "current_script": "string?",
  "action_queue": "Action[]",
  "context": {
    "position": "BlockPos",
    "health": "float",
    "nearby_entities": "Entity[]",
    "nearby_blocks": "Block[]"
  },
  "performance_metrics": {
    "scripts_executed": "int",
    "agents_triggered": "int",
    "api_calls_saved": "int"
  }
}
```

## Educational Integration

### Learning Objectives
1. **Understand automation vs. intelligence**
   - Bots follow rules blindly (efficient but rigid)
   - Agents reason and adapt (flexible but costly)

2. **Recognize when to use each approach**
   - Routine tasks → Scripts (cost-effective)
   - Novel problems → Agents (adaptable)

3. **Learn about performance trade-offs**
   - Speed: Scripts (ms) vs Agents (s)
   - Cost: Scripts (free) vs Agents ($$)
   - Flexibility: Scripts (limited) vs Agents (unlimited)

### In-Game Teaching Moments

```
┌────────────────────────────────────────────────────────────┐
│  📚 LEARNING MOMENT: Bot vs Agent                          │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  You just strip-mined 100 blocks using a BOT SCRIPT.      │
│                                                            │
│  🤖 Bot characteristics:                                   │
│     • Speed: 50ms (instant)                               │
│     • Cost: $0.00 (free)                                  │
│     • Success rate: 98% (reliable)                        │
│                                                            │
│  🧠 If we used an AGENT instead:                          │
│     • Speed: ~50 seconds (100× slower)                    │
│     • Cost: $0.20 (expensive at scale)                    │
│     • Flexibility: Could handle surprises                 │
│                                                            │
│  💡 Lesson: Use bots for routine, agents for novelty!     │
│                                                            │
│  [Learn More] [Try Agent Version] [Continue Mining]        │
└────────────────────────────────────────────────────────────┘
```

### Skill Progression System

```
TIER 1: BEGINNER
├── Basic mining (bot)
├── Simple building (bot)
└── Asking questions (agent)

TIER 2: INTERMEDIATE  
├── Strip mining optimization (bot)
├── Redstone basics (bot)
├── Error recovery (agent)
└── Learning explanations (agent)

TIER 3: ADVANCED
├── Logic gate design (bot)
├── Circuit building (bot)
├── Creative projects (agent)
└── Teaching others (agent)

TIER 4: EXPERT
├── Chip design basics (bot)
├── Thermal simulation (bot)
├── Novel architectures (agent)
└── System optimization (agent)

TIER 5: MASTER
├── Writing new scripts (agent)
├── Teaching the system (agent)
└── Performance analysis (agent)
```

## MCP Server API Reference

### POST /process
Process a command, returning either script execution or agent request.

**Request:**
```json
{
  "command": "strip mine 100 blocks at y -59",
  "context": {
    "position": {"x": 100, "y": -59, "z": -200},
    "inventory": {"pickaxe": 1, "torches": 50},
    "nearby_blocks": ["stone", "stone", "diamond_ore"]
  }
}
```

**Response (Script):**
```json
{
  "type": "script_execution",
  "scriptId": "mining_strip_tunnel_v1",
  "scriptName": "Strip Mining Tunnel",
  "actions": [...],
  "parameters": {"length": 100, "yLevel": -59},
  "estimatedTime": 5000,
  "educationalNote": "🤖 BOT EXECUTION...",
  "cost": 0,
  "source": "local_script"
}
```

**Response (Agent):**
```json
{
  "type": "agent_execution",
  "requestId": "agent_1234567890_abc",
  "command": "design a novel redstone calculator",
  "status": "pending_llm_call",
  "educationalNote": "🧠 AGENT EXECUTION...",
  "cost": "api_call_required",
  "source": "cloud_agent"
}
```

### GET /metrics
Get performance metrics.

**Response:**
```json
{
  "scriptsExecuted": 500,
  "agentsTriggered": 12,
  "apiCallsSaved": 488,
  "cacheHitRate": 0.976,
  "estimatedSavings": "$0.976"
}
```

### GET /learn/bot-vs-agent
Educational content about the difference.

### GET /scripts
List all available scripts with success rates.

## Implementation Checklist

- [x] Design architecture
- [x] Create MCP server
- [x] Create trigger system
- [x] Create skill library
- [x] Create data components
- [ ] Integrate with NeoForge mod
- [ ] Create in-game GUI
- [ ] Add educational tutorials
- [ ] Performance testing
- [ ] Documentation

## Future Enhancements

1. **Visual Script Editor**: Drag-and-drop script creation
2. **Agent Personality**: Different agent "personalities" for different tasks
3. **Multi-Agent Coordination**: Multiple agents working together
4. **Replay System**: Watch and learn from agent decisions
5. **Cost Dashboard**: Real-time cost tracking in-game
6. **Skill Marketplace**: Share learned skills with other players
