/**
 * Agent Trigger System
 * 
 * Defines conditions under which script execution escalates to agent (LLM) calls.
 * This is the key innovation that minimizes API usage while maintaining flexibility.
 */

// Trigger types that cause agent escalation
export interface AgentTrigger {
  id: string;
  name: string;
  category: 'SAFETY' | 'PERFORMANCE' | 'LEARNING' | 'EDGE_CASE' | 'USER_REQUEST';
  condition: TriggerCondition;
  action: EscalationAction;
  cooldown: number; // ms before same trigger can fire again
  lastTriggered: number;
}

export interface TriggerCondition {
  type: 'THRESHOLD' | 'PATTERN' | 'STATE_CHANGE' | 'TIMEOUT' | 'ERROR';
  metric?: string;
  operator?: '<' | '>' | '==' | '!=' | 'includes' | 'matches';
  value?: any;
  fallbackScript?: string;
}

export interface EscalationAction {
  type: 'PAUSE_AND_ASK' | 'CONTINUE_WITH_AGENT' | 'SWITCH_TO_AGENT' | 'LOG_AND_CONTINUE';
  prompt: string;
  contextKeys: string[];
  fallbackBehavior: 'retry' | 'abort' | 'skip' | 'manual';
}

// Pre-defined triggers for STEM learning scenarios
export const STEM_TRIGGERS: AgentTrigger[] = [
  // === SAFETY TRIGGERS ===
  {
    id: 'lava_detected',
    name: 'Lava Safety Trigger',
    category: 'SAFETY',
    condition: {
      type: 'PATTERN',
      metric: 'nearby_blocks',
      operator: 'includes',
      value: 'lava'
    },
    action: {
      type: 'PAUSE_AND_ASK',
      prompt: 'Lava detected nearby! How should I proceed? Options: avoid, bridge_over, collect, or ask_for_help',
      contextKeys: ['position', 'nearby_blocks', 'current_task'],
      fallbackBehavior: 'abort'
    },
    cooldown: 5000,
    lastTriggered: 0
  },
  {
    id: 'health_critical',
    name: 'Critical Health',
    category: 'SAFETY',
    condition: {
      type: 'THRESHOLD',
      metric: 'health',
      operator: '<',
      value: 6 // 3 hearts
    },
    action: {
      type: 'SWITCH_TO_AGENT',
      prompt: 'Health is critical! Agent needs to assess situation and decide: retreat, use_potion, or call_for_help',
      contextKeys: ['position', 'health', 'nearby_entities', 'inventory'],
      fallbackBehavior: 'abort'
    },
    cooldown: 1000,
    lastTriggered: 0
  },
  {
    id: 'hostile_mob_nearby',
    name: 'Hostile Mob Detected',
    category: 'SAFETY',
    condition: {
      type: 'PATTERN',
      metric: 'nearby_entities',
      operator: 'includes',
      value: ['zombie', 'skeleton', 'creeper', 'spider', 'enderman']
    },
    action: {
      type: 'CONTINUE_WITH_AGENT',
      prompt: 'Hostile mob detected. Should I: fight, flee, or hide? Context: position, mob type, equipment',
      contextKeys: ['position', 'nearby_entities', 'equipment', 'health'],
      fallbackBehavior: 'manual'
    },
    cooldown: 10000,
    lastTriggered: 0
  },

  // === EDGE CASE TRIGGERS ===
  {
    id: 'unknown_block',
    name: 'Unknown Block Encountered',
    category: 'EDGE_CASE',
    condition: {
      type: 'PATTERN',
      metric: 'block_ahead',
      operator: '!=',
      value: ['stone', 'dirt', 'cobblestone', 'air', 'ore_*']
    },
    action: {
      type: 'PAUSE_AND_ASK',
      prompt: 'Found an unexpected block: {{block_ahead}}. What should I do? mine, avoid, or use_vlm_to_identify?',
      contextKeys: ['position', 'block_ahead', 'inventory'],
      fallbackBehavior: 'skip'
    },
    cooldown: 5000,
    lastTriggered: 0
  },
  {
    id: 'script_parameter_violation',
    name: 'Script Parameter Out of Bounds',
    category: 'EDGE_CASE',
    condition: {
      type: 'THRESHOLD',
      metric: 'parameter_deviation',
      operator: '>',
      value: 0.5 // 50% deviation from expected
    },
    action: {
      type: 'SWITCH_TO_AGENT',
      prompt: 'Script parameters are outside normal range. Agent should reassess and adapt.',
      contextKeys: ['script_id', 'expected_params', 'actual_params', 'context'],
      fallbackBehavior: 'abort'
    },
    cooldown: 0,
    lastTriggered: 0
  },
  {
    id: 'resource_depleted',
    name: 'Required Resource Depleted',
    category: 'EDGE_CASE',
    condition: {
      type: 'PATTERN',
      metric: 'inventory',
      operator: '==',
      value: 'empty:required_item'
    },
    action: {
      type: 'CONTINUE_WITH_AGENT',
      prompt: 'Out of {{required_item}}! Should I: gather_more, switch_task, or craft_more?',
      contextKeys: ['inventory', 'required_item', 'nearby_resources', 'current_task'],
      fallbackBehavior: 'retry'
    },
    cooldown: 30000,
    lastTriggered: 0
  },

  // === LEARNING TRIGGERS ===
  {
    id: 'learning_request',
    name: 'User Learning Request',
    category: 'LEARNING',
    condition: {
      type: 'PATTERN',
      metric: 'command',
      operator: 'matches',
      value: /^(why|how|explain|teach|learn|what|help)/i
    },
    action: {
      type: 'SWITCH_TO_AGENT',
      prompt: 'User wants to learn. Provide educational explanation about current topic with Minecraft examples.',
      contextKeys: ['command', 'current_context', 'player_knowledge_level'],
      fallbackBehavior: 'manual'
    },
    cooldown: 0,
    lastTriggered: 0
  },
  {
    id: 'skill_progression',
    name: 'Skill Progression Opportunity',
    category: 'LEARNING',
    condition: {
      type: 'THRESHOLD',
      metric: 'task_completion_streak',
      operator: '>',
      value: 5
    },
    action: {
      type: 'PAUSE_AND_ASK',
      prompt: 'You\'ve mastered this task! Would you like to learn a more advanced technique?',
      contextKeys: ['current_skill', 'completion_count', 'player_level'],
      fallbackBehavior: 'skip'
    },
    cooldown: 60000,
    lastTriggered: 0
  },
  {
    id: 'failure_learning',
    name: 'Learn from Failure',
    category: 'LEARNING',
    condition: {
      type: 'PATTERN',
      metric: 'script_result',
      operator: '==',
      value: 'failure'
    },
    action: {
      type: 'SWITCH_TO_AGENT',
      prompt: 'Script execution failed. Analyze why and either: fix approach, explain to player, or create new skill.',
      contextKeys: ['script_id', 'error_message', 'context_at_failure', 'player_level'],
      fallbackBehavior: 'manual'
    },
    cooldown: 5000,
    lastTriggered: 0
  },

  // === PERFORMANCE TRIGGERS ===
  {
    id: 'execution_timeout',
    name: 'Script Execution Timeout',
    category: 'PERFORMANCE',
    condition: {
      type: 'TIMEOUT',
      metric: 'script_duration_ms',
      operator: '>',
      value: 30000 // 30 seconds
    },
    action: {
      type: 'PAUSE_AND_ASK',
      prompt: 'Script taking longer than expected. Continue waiting, abort, or let agent investigate?',
      contextKeys: ['script_id', 'elapsed_time', 'progress', 'context'],
      fallbackBehavior: 'abort'
    },
    cooldown: 10000,
    lastTriggered: 0
  },
  {
    id: 'low_success_rate',
    name: 'Script Success Rate Low',
    category: 'PERFORMANCE',
    condition: {
      type: 'THRESHOLD',
      metric: 'script_success_rate',
      operator: '<',
      value: 0.7
    },
    action: {
      type: 'SWITCH_TO_AGENT',
      prompt: 'This script has a low success rate. Agent should analyze and improve or replace it.',
      contextKeys: ['script_id', 'success_rate', 'recent_failures'],
      fallbackBehavior: 'abort'
    },
    cooldown: 60000,
    lastTriggered: 0
  },

  // === USER REQUEST TRIGGERS ===
  {
    id: 'explicit_agent_request',
    name: 'User Requests Agent',
    category: 'USER_REQUEST',
    condition: {
      type: 'PATTERN',
      metric: 'command',
      operator: 'matches',
      value: /(agent|ai|smart|think|decide)/i
    },
    action: {
      type: 'SWITCH_TO_AGENT',
      prompt: 'User explicitly requested AI assistance. Provide intelligent response.',
      contextKeys: ['command', 'context'],
      fallbackBehavior: 'manual'
    },
    cooldown: 0,
    lastTriggered: 0
  },
  {
    id: 'creative_mode_request',
    name: 'Creative/Novel Request',
    category: 'USER_REQUEST',
    condition: {
      type: 'PATTERN',
      metric: 'command',
      operator: 'matches',
      value: /(create|design|invent|new|novel|unique|custom)/i
    },
    action: {
      type: 'SWITCH_TO_AGENT',
      prompt: 'User wants something creative/novel. Generate innovative solution using LLM.',
      contextKeys: ['command', 'available_resources', 'constraints'],
      fallbackBehavior: 'manual'
    },
    cooldown: 0,
    lastTriggered: 0
  }
];

// Trigger evaluator
export class TriggerEvaluator {
  private triggers: Map<string, AgentTrigger> = new Map();
  private lastTriggerTimes: Map<string, number> = new Map();

  constructor(triggers: AgentTrigger[] = STEM_TRIGGERS) {
    triggers.forEach(t => this.triggers.set(t.id, t));
  }

  evaluate(context: Record<string, any>): AgentTrigger[] {
    const now = Date.now();
    const triggered: AgentTrigger[] = [];

    for (const trigger of this.triggers.values()) {
      // Check cooldown
      const lastTime = this.lastTriggerTimes.get(trigger.id) || 0;
      if (now - lastTime < trigger.cooldown) continue;

      // Evaluate condition
      if (this.evaluateCondition(trigger.condition, context)) {
        trigger.lastTriggered = now;
        this.lastTriggerTimes.set(trigger.id, now);
        triggered.push(trigger);
      }
    }

    return triggered;
  }

  private evaluateCondition(condition: TriggerCondition, context: Record<string, any>): boolean {
    const value = this.getNestedValue(context, condition.metric || '');

    switch (condition.type) {
      case 'THRESHOLD':
        return this.compareThreshold(value, condition.operator!, condition.value);
      
      case 'PATTERN':
        return this.matchPattern(value, condition.operator!, condition.value);
      
      case 'STATE_CHANGE':
        return value !== context.previousState?.[condition.metric || ''];
      
      case 'TIMEOUT':
        return (value || 0) > (condition.value || 0);
      
      case 'ERROR':
        return context.error !== undefined;
      
      default:
        return false;
    }
  }

  private getNestedValue(obj: any, path: string): any {
    if (!path) return obj;
    return path.split('.').reduce((o, k) => o?.[k], obj);
  }

  private compareThreshold(value: any, op: string, threshold: any): boolean {
    switch (op) {
      case '<': return value < threshold;
      case '>': return value > threshold;
      case '==': return value === threshold;
      case '!=': return value !== threshold;
      default: return false;
    }
  }

  private matchPattern(value: any, op: string, pattern: any): boolean {
    if (Array.isArray(pattern)) {
      return pattern.some(p => this.matchPattern(value, op, p));
    }

    switch (op) {
      case 'includes':
        return Array.isArray(value) ? value.includes(pattern) : String(value).includes(pattern);
      case 'matches':
        return pattern instanceof RegExp ? pattern.test(String(value)) : new RegExp(pattern).test(String(value));
      case '==':
        return value === pattern;
      case '!=':
        return value !== pattern;
      default:
        return false;
    }
  }

  addTrigger(trigger: AgentTrigger): void {
    this.triggers.set(trigger.id, trigger);
  }

  removeTrigger(id: string): void {
    this.triggers.delete(id);
  }

  getTriggers(): AgentTrigger[] {
    return Array.from(this.triggers.values());
  }
}
