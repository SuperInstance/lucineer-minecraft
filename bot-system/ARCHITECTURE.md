# Bot-Script-Agent Architecture

## Overview

This system implements a **dual-process cognitive architecture** for Minecraft STEM learning:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PLAYER INTERACTION                           │
│  /bot <command> or GUI interaction or in-game events           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  COMPLEXITY ANALYZER                            │
│  • Pattern matching against known scripts                       │
│  • Keyword analysis for intent detection                        │
│  • Historical success rate lookup                               │
└─────────────────────────────────────────────────────────────────┘
          │                                       │
          │ TRIVIAL/SIMPLE                        │ MODERATE/COMPLEX
          │ (Cache Hit)                           │ (Cache Miss)
          ▼                                       ▼
┌───────────────────────┐           ┌───────────────────────────┐
│    SCRIPT ENGINE      │           │      AGENT LAYER          │
│    (System 1 - Fast)  │           │    (System 2 - Slow)      │
│                       │           │                           │
│ • Execute instantly   │           │ • Call LLM API            │
│ • No network calls    │           │ • Generate new plan       │
│ • ~1-5ms response     │           │ • Learn from result       │
│                       │           │ • ~500-3000ms response    │
└───────────────────────┘           └───────────────────────────┘
          │                                       │
          │                                       ▼
          │                           ┌───────────────────────────┐
          │                           │    SCRIPT GENERATOR       │
          │                           │ • Extract pattern         │
          │                           │ • Create reusable script  │
          │                           │ • Add to skill library    │
          │                           └───────────────────────────┘
          │                                       │
          └───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    LOCAL MCP SERVER                             │
│  • Coordinates scripts and agents                               │
│  • Maintains state cache                                        │
│  • Handles inter-process communication                          │
│  • Provides debugging interface                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Key Concepts

### Bots vs Agents

| Aspect | Bot (Script) | Agent (LLM) |
|--------|--------------|-------------|
| **Execution** | Rule-based, deterministic | AI-powered, adaptive |
| **Speed** | Milliseconds | Seconds |
| **Cost** | Free (local) | Paid (API) |
| **Learning** | Pre-programmed | Learns from context |
| **Edge Cases** | Fails or escalates | Handles creatively |
| **Use Case** | Routine tasks | Novel situations |

### When Scripts Handle Execution (No API Call)

1. **Exact match** - Same command seen before
2. **Semantic similarity** - Similar command (>0.75 similarity)
3. **Skill applicability** - Known skill pattern matches
4. **Trivial complexity** - Simple keyword patterns

### When Agents Handle Execution (API Call Triggered)

1. **Cache miss** - No similar script found
2. **Edge case detected** - Parameters outside script bounds
3. **Failure recovery** - Script execution failed
4. **Multi-step coordination** - Complex planning needed
5. **Learning request** - User asks to learn something new

## Cost Reduction

| Scenario | Without System | With System | Savings |
|----------|---------------|-------------|---------|
| Mining tunnel (100 actions) | 100 API calls | 0 API calls | 100% |
| Farm management (daily) | 50 API calls | 1 API call | 98% |
| Novel exploration | 1 API call | 1 API call | 0% |
| **Average gameplay** | ~200 calls/hour | ~5 calls/hour | **97.5%** |

## Educational Value

This architecture teaches players:

1. **Difference between automation and intelligence**
   - Bots follow rules blindly
   - Agents reason and adapt

2. **When to use which approach**
   - Routine tasks → Scripts (efficient)
   - Complex problems → Agents (flexible)

3. **Performance vs. capability tradeoffs**
   - Fast but rigid vs. slow but smart

4. **Cost optimization**
   - Every API call has a cost
   - Caching saves money and time
