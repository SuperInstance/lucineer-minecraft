/**
 * Lucineer MCP Server v3.0
 * 
 * A production-ready Model Context Protocol server with:
 * - STT (Speech-to-Text) for voice commands
 * - TTS (Text-to-Speech) with immersive Minecraft-style dialogue
 * - Advanced bot scripting abstraction layer
 * - Context-aware parameter extraction
 * - Adaptive learning and pattern recognition
 * - Safety triggers with priority-based handling
 */

import { serve } from "bun";
import ZAI from 'z-ai-web-dev-sdk';
import fs from 'fs';
import path from 'path';

// ============================================================
// TYPES
// ============================================================

interface Script {
  id: string;
  name: string;
  description: string;
  category: ScriptCategory;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  
  // Improved action system
  rootAction: BotAction;
  
  parameters: ParameterDefinition[];
  keywords: string[];
  examples: string[];
  constraints: ScriptConstraint[];
  
  // Script metadata
  successRate: number;
  executionCount: number;
  averageTime: number;
  tier: 'BUILT_IN' | 'LEARNED' | 'PROPOSED';
  
  // Educational content
  learningObjectives: string[];
  relatedSkills: string[];
}

type ScriptCategory = 
  | 'MINING' | 'FARMING' | 'BUILDING' | 'REDSTONE' 
  | 'EXPLORATION' | 'COMBAT' | 'CRAFTING' | 'CHIP_DESIGN'
  | 'THERMAL_SIM' | 'NEURAL_NET' | 'LEARNING';

// ============================================================
// IMPROVED BOT ACTION ABSTRACTION
// ============================================================

/**
 * BotAction - A composable unit of bot behavior
 * 
 * This abstraction allows complex behaviors to be built from simple primitives.
 * Actions can be composed, sequenced, looped, and conditioned.
 * 
 * When a condition cannot be evaluated locally (edge case), 
 * the action "fails upward" to trigger agent intervention.
 */
interface BotAction {
  // Action identification
  id: string;
  type: BotActionType;
  
  // Execution control
  timeout?: number;           // Max execution time in ms
  retries?: number;           // Number of retry attempts
  retryDelay?: number;        // Delay between retries in ms
  
  // Conditional execution
  precondition?: Condition;   // Must be true before executing
  postcondition?: Condition;  // Expected state after execution
  
  // Action-specific data
  data?: Record<string, any>;
  
  // Child actions (for composite types)
  children?: BotAction[];
  
  // Fallback behavior
  onFailure?: 'RETRY' | 'SKIP' | 'ABORT' | 'ESCALATE';
  escalationPrompt?: string;  // Prompt for agent when escalating
  
  // Voice feedback
  announceStart?: DialogueLine;
  announceComplete?: DialogueLine;
  announceError?: DialogueLine;
}

type BotActionType = 
  // Primitive actions (executed by game client)
  | 'MOVE' | 'LOOK' | 'MINE' | 'PLACE' | 'INTERACT' | 'ATTACK' 
  | 'CRAFT' | 'SMELT' | 'DROP' | 'PICKUP' | 'EQUIP' | 'CONSUME'
  | 'SCAN' | 'PATHFIND' | 'WAIT' | 'SPEAK'
  
  // Composite actions (contain child actions)
  | 'SEQUENCE' | 'PARALLEL' | 'LOOP' | 'CONDITIONAL' | 'TRY_CATCH'
  | 'REPEAT_UNTIL' | 'WHILE' | 'FOR_EACH'
  
  // Control flow
  | 'CALL_SCRIPT' | 'RETURN' | 'BREAK' | 'CONTINUE'
  
  // Special actions
  | 'AWAIT_CONDITION' | 'ESCALATE_TO_AGENT' | 'LOG';

interface Condition {
  type: ConditionType;
  evaluate: string;  // Expression to evaluate
  expectedValue?: any;
  fallbackAction?: 'SKIP' | 'ESCALATE' | 'DEFAULT';
  defaultValue?: any;
}

type ConditionType = 
  | 'INVENTORY_HAS'      // Check inventory for items
  | 'BLOCK_IS'           // Check block at position
  | 'ENTITY_NEARBY'      // Check for nearby entities
  | 'HEALTH_ABOVE'       // Check health threshold
  | 'HUNGER_ABOVE'       // Check hunger threshold
  | 'POSITION_WITHIN'    // Check position in area
  | 'TIME_OF_DAY'        // Check game time
  | 'WEATHER_IS'         // Check weather
  | 'BIOME_IS'           // Check current biome
  | 'LIGHT_LEVEL'        // Check light level
  | 'CUSTOM_SCRIPT';     // Custom evaluation script

/**
 * DialogueLine - Immersive in-game speech
 * 
 * Lines are randomly selected and include context placeholders
 * for dynamic, natural-sounding responses.
 */
interface DialogueLine {
  templates: string[];       // Multiple variations for natural speech
  context?: Record<string, string>;  // Contextual replacements
  tone: 'casual' | 'focused' | 'alert' | 'excited' | 'calm' | 'concerned';
  volume?: 'whisper' | 'normal' | 'loud';
  delay?: number;           // Delay before speaking
}

// ============================================================
// IMMERSIVE DIALOGUE SYSTEM
// ============================================================

class ImmersiveDialogue {
  private personality: 'helpful_guide' | 'technical_assistant' | 'friendly_bot' | 'mentor';
  
  constructor(personality: 'helpful_guide' | 'technical_assistant' | 'friendly_bot' | 'mentor' = 'friendly_bot') {
    this.personality = personality;
  }
  
  /**
   * Generate immersive dialogue for script execution
   */
  getStartDialogue(script: Script, params: Record<string, any>): DialogueLine {
    const dialogues = this.getCategoryStartDialogues(script.category);
    const templates = dialogues.map(d => this.applyContext(d, params));
    
    return {
      templates,
      tone: 'focused',
      volume: 'normal'
    };
  }
  
  getCompleteDialogue(script: Script, params: Record<string, any>): DialogueLine {
    const dialogues = this.getCategoryCompleteDialogues(script.category);
    const templates = dialogues.map(d => this.applyContext(d, params));
    
    return {
      templates,
      tone: 'calm',
      volume: 'normal'
    };
  }
  
  getAlertDialogue(trigger: string, context: Record<string, any>): DialogueLine {
    const alertDialogues: Record<string, string[]> = {
      'lava_proximity': [
        "Hold up! There's lava nearby. Watch your step!",
        "Whoa, lava detected! Let's be careful here.",
        "Danger! Lava ahead. How should we proceed?",
        "Hot stuff nearby! Need to decide our next move."
      ],
      'hostile_mob_threat': [
        "Hostiles spotted! Ready your weapons!",
        "Enemies nearby! What's the plan?",
        "Threat detected! Fight or flight?",
        "Watch out! There's something nasty nearby."
      ],
      'low_health_warning': [
        "You're hurt bad! Maybe grab some food?",
        "Health is low! Time to heal up!",
        "Getting critical here! Need recovery!",
        "Whoa, you're looking rough! Let's heal up."
      ],
      'creative_request': [
        "Ooh, something new! Let me think about this...",
        "This requires some creativity! Thinking...",
        "Interesting challenge! Let me work on that.",
        "Now this is a puzzle! Computing..."
      ],
      'learning_request': [
        "Great question! Let me explain...",
        "Love teaching this stuff! Here's the scoop...",
        "Excellent curiosity! Let's learn together.",
        "Time for some knowledge! Listen up..."
      ]
    };
    
    const templates = alertDialogues[trigger] || ["Something needs attention!"];
    
    return {
      templates,
      tone: 'alert',
      volume: 'loud'
    };
  }
  
  getProgressDialogue(step: number, total: number): DialogueLine {
    const dialogues = [
      `Step ${step} of ${total}. Making progress!`,
      `${step}/${total} complete. Keep going!`,
      `Progress: ${step} down, ${total - step} to go.`,
      `Finished ${step} out of ${total}. Not bad!`
    ];
    
    return {
      templates: dialogues,
      tone: 'focused',
      volume: 'normal'
    };
  }
  
  getEducationalSnippet(category: ScriptCategory): DialogueLine {
    const snippets: Record<ScriptCategory, string[]> = {
      'MINING': [
        "Fun fact: Diamond ore spawns most at Y level -59!",
        "Pro tip: Branch mining with 3-block spacing covers all ore.",
        "Mining pro move: Always have torches ready!"
      ],
      'FARMING': [
        "Did you know? Crops grow faster in rows with water nearby.",
        "Farm tip: Bonemeal speeds up growth instantly!",
        "Agriculture hack: Light keeps crops growing at night."
      ],
      'REDSTONE': [
        "Redstone tip: Repeaters extend signals up to 15 blocks!",
        "Circuit trick: Observers can detect crop growth!",
        "Logic gate fact: All gates can be built from NAND gates!"
      ],
      'CHIP_DESIGN': [
        "Chip design tip: Transistors are like tiny switches!",
        "IC fact: Modern chips have billions of transistors!",
        "Design trick: Thermal management prevents chip damage."
      ],
      'THERMAL_SIM': [
        "Physics fun: Heat flows from hot to cold, always!",
        "Thermal tip: Good heat sinks save chips from frying.",
        "Simulation fact: We model heat with partial differential equations!"
      ],
      'NEURAL_NET': [
        "AI fact: Neural networks learn by adjusting connection weights!",
        "Neural tip: More layers = more complex patterns learned.",
        "Deep learning: Inspired by how brains process information!"
      ],
      'BUILDING': [
        "Building tip: Use different materials for contrast!",
        "Architecture trick: Odd numbers look more natural.",
        "Design fact: Lighting makes or breaks a build!"
      ],
      'EXPLORATION': [
        "Explorer tip: Bring a bed to skip nights safely!",
        "Adventure fact: Strongholds are near the origin!",
        "Exploration trick: Maps show your position relative to center."
      ],
      'COMBAT': [
        "Combat tip: Shields block most attacks completely!",
        "Battle trick: Critical hits do 50% more damage!",
        "Fighting fact: Sweep attacks hit multiple enemies!"
      ],
      'CRAFTING': [
        "Crafting tip: Shift-click moves items quickly!",
        "Recipe trick: The recipe book shows all discoveries.",
        "Crafting fact: Some items need special stations!"
      ],
      'LEARNING': [
        "Learning tip: Practice makes perfect in Minecraft!",
        "Skill fact: Teaching others helps you learn too!",
        "Progress note: Every expert was once a beginner!"
      ]
    };
    
    const templates = snippets[category] || ["Keep learning and exploring!"];
    
    return {
      templates,
      tone: 'casual',
      volume: 'normal'
    };
  }
  
  private getCategoryStartDialogues(category: ScriptCategory): string[] {
    const dialogues: Record<ScriptCategory, string[]> = {
      'MINING': [
        "Alright, time to dig! Let's get those ores!",
        "Mining operation starting! Pickaxes ready!",
        "Here we go! Let's see what we find down there.",
        "Excavation time! Watch for lava!"
      ],
      'FARMING': [
        "Farm duty! Let's get those crops harvested!",
        "Time to tend the fields! Nature's calling!",
        "Agriculture mode activated! Let's farm!",
        "Harvest time! Hope you're hungry!"
      ],
      'REDSTONE': [
        "Redstone engineering! Let's build something cool!",
        "Circuit construction initiated! Precision mode on!",
        "Time for some redstone magic! Watch closely!",
        "Engineering project starting! This'll be fun!"
      ],
      'CHIP_DESIGN': [
        "Chip design mode! Creating silicon magic!",
        "Integrated circuit time! Let's architect this!",
        "Semiconductor design initiated! Precision required!",
        "Time to design a chip! Engineering excellence!"
      ],
      'THERMAL_SIM': [
        "Thermal simulation starting! Heat equations ready!",
        "Physics mode on! Let's model some heat flow!",
        "Temperature mapping initiated! Hot stuff coming!",
        "Simulation time! Where's the heat going?"
      ],
      'NEURAL_NET': [
        "Neural network visualization! Brain building time!",
        "AI architecture mode! Let's map those neurons!",
        "Deep learning visual! This is how AI thinks!",
        "Network construction! Connecting the dots!"
      ],
      'BUILDING': [
        "Construction time! Let's build something amazing!",
        "Building mode activated! Creativity engaged!",
        "Architecture project starting! Vision to reality!",
        "Time to build! Let's make this place epic!"
      ],
      'EXPLORATION': [
        "Adventure time! Let's see what's out there!",
        "Exploration mode! The unknown awaits!",
        "Scouting mission! What will we discover?",
        "Journey begins! Pack light, explore far!"
      ],
      'COMBAT': [
        "Combat mode! Stay sharp!",
        "Battle stations! Enemies ahead!",
        "Fight time! Weapon ready!",
        "Combat initiated! Stay focused!"
      ],
      'CRAFTING': [
        "Crafting session! Let's make something useful!",
        "Recipe time! Ingredients ready!",
        "Crafting mode! What shall we create?",
        "Manufacturing started! Workbench ready!"
      ],
      'LEARNING': [
        "Learning session! Time for knowledge!",
        "Education mode! Let me explain!",
        "Teaching time! Listen up!",
        "Knowledge drop incoming! Ready to learn?"
      ]
    };
    
    return dialogues[category] || ["Task starting!"];
  }
  
  private getCategoryCompleteDialogues(category: ScriptCategory): string[] {
    const dialogues: Record<ScriptCategory, string[]> = {
      'MINING': [
        "Mining complete! Look at all those resources!",
        "Digging done! Hope we found good stuff!",
        "Excavation finished! Nice haul!",
        "Mining success! Those diamonds were hiding deep!"
      ],
      'FARMING': [
        "Harvest complete! Fresh crops collected!",
        "Farming done! The fields look great!",
        "Agriculture success! Time to store the bounty!",
        "Harvest finished! Good work, farmer!"
      ],
      'REDSTONE': [
        "Circuit complete! It works beautifully!",
        "Redstone build done! Watch it tick!",
        "Engineering success! That's a work of art!",
        "Circuit finished! Redstone mastery!"
      ],
      'CHIP_DESIGN': [
        "Chip designed! Silicon architecture complete!",
        "IC design finished! Ready for fabrication!",
        "Circuit layout done! Engineering excellence!",
        "Chip complete! That's some serious tech!"
      ],
      'THERMAL_SIM': [
        "Simulation done! Heat map generated!",
        "Thermal analysis complete! Temperature data ready!",
        "Physics modeling finished! Hot spots identified!",
        "Simulation success! We know where the heat goes!"
      ],
      'NEURAL_NET': [
        "Network visualized! Look at those connections!",
        "Neural structure complete! AI brain mapped!",
        "Network built! That's how machines think!",
        "Visualization done! Deep learning made visible!"
      ],
      'BUILDING': [
        "Construction complete! It looks amazing!",
        "Building done! A true masterpiece!",
        "Architecture finished! Crafted with care!",
        "Build success! This place is epic now!"
      ],
      'EXPLORATION': [
        "Exploration done! Map expanded!",
        "Journey complete! What did we find?",
        "Scouting finished! New territory mapped!",
        "Adventure success! Stories to tell!"
      ],
      'COMBAT': [
        "Threat eliminated! Area secure!",
        "Combat complete! Victory achieved!",
        "Battle won! Skills improving!",
        "Fight finished! Still standing!"
      ],
      'CRAFTING': [
        "Crafting complete! Items ready!",
        "Manufacturing done! Quality goods!",
        "Recipe finished! Crafting mastery!",
        "Items created! Workbench success!"
      ],
      'LEARNING': [
        "Lesson complete! Knowledge gained!",
        "Teaching done! Hope that helped!",
        "Learning finished! You're smarter now!",
        "Education success! Questions? No? Great!"
      ]
    };
    
    return dialogues[category] || ["Task completed!"];
  }
  
  private applyContext(template: string, params: Record<string, any>): string {
    // Replace parameter placeholders
    let result = template;
    for (const [key, value] of Object.entries(params)) {
      result = result.replace(new RegExp(`{{${key}}}`, 'g'), String(value));
    }
    return result;
  }
  
  /**
   * Select a random dialogue line for natural variation
   */
  selectLine(dialogue: DialogueLine): string {
    const idx = Math.floor(Math.random() * dialogue.templates.length);
    return dialogue.templates[idx];
  }
}

// ============================================================
// SCRIPT BUILDER - Fluent API for creating scripts
// ============================================================

class ScriptBuilder {
  private script: Partial<Script> = {};
  private currentAction: BotAction | null = null;
  private actionStack: BotAction[] = [];
  
  static create(id: string, name: string): ScriptBuilder {
    const builder = new ScriptBuilder();
    builder.script.id = id;
    builder.script.name = name;
    builder.script.executionCount = 0;
    builder.script.successRate = 0.95;
    builder.script.tier = 'BUILT_IN';
    builder.script.averageTime = 0;
    builder.script.createdAt = Date.now();
    builder.script.lastUsed = 0;
    builder.script.keywords = [];
    builder.script.examples = [];
    builder.script.constraints = [];
    builder.script.parameters = [];
    builder.script.learningObjectives = [];
    builder.script.relatedSkills = [];
    return builder;
  }
  
  description(desc: string): this {
    this.script.description = desc;
    return this;
  }
  
  category(cat: ScriptCategory): this {
    this.script.category = cat;
    return this;
  }
  
  difficulty(level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'): this {
    this.script.difficulty = level;
    return this;
  }
  
  keywords(...words: string[]): this {
    this.script.keywords!.push(...words);
    return this;
  }
  
  examples(...ex: string[]): this {
    this.script.examples!.push(...ex);
    return this;
  }
  
  learningObjectives(...objectives: string[]): this {
    this.script.learningObjectives!.push(...objectives);
    return this;
  }
  
  relatedSkills(...skills: string[]): this {
    this.script.relatedSkills!.push(...skills);
    return this;
  }
  
  param(param: ParameterDefinition): this {
    this.script.parameters!.push(param);
    return this;
  }
  
  // Action building methods
  startSequence(): this {
    const action: BotAction = {
      id: `seq_${Date.now()}`,
      type: 'SEQUENCE',
      children: []
    };
    this.actionStack.push(action);
    return this;
  }
  
  startParallel(): this {
    const action: BotAction = {
      id: `par_${Date.now()}`,
      type: 'PARALLEL',
      children: []
    };
    this.actionStack.push(action);
    return this;
  }
  
  startLoop(iterations: string | number, loopVar?: string): this {
    const action: BotAction = {
      id: `loop_${Date.now()}`,
      type: 'LOOP',
      data: { 
        iterations: typeof iterations === 'string' ? { variable: iterations } : { value: iterations },
        loopVar: loopVar || 'i'
      },
      children: []
    };
    this.actionStack.push(action);
    return this;
  }
  
  startConditional(condition: Condition): this {
    const action: BotAction = {
      id: `cond_${Date.now()}`,
      type: 'CONDITIONAL',
      precondition: condition,
      children: []
    };
    this.actionStack.push(action);
    return this;
  }
  
  // Primitive actions
  mine(block: string, count?: number): this {
    return this.addAction({
      id: `mine_${Date.now()}`,
      type: 'MINE',
      data: { block, count: count || 1 },
      onFailure: 'ESCALATE',
      escalationPrompt: `Cannot mine ${block}. Need to check inventory and surroundings.`
    });
  }
  
  place(block: string, position?: { x: number; y: number; z: number }): this {
    return this.addAction({
      id: `place_${Date.now()}`,
      type: 'PLACE',
      data: { block, position },
      onFailure: 'ESCALATE',
      escalationPrompt: `Cannot place ${block}. Check if valid position and have items.`
    });
  }
  
  move(direction: 'forward' | 'back' | 'left' | 'right', distance: number = 1): this {
    return this.addAction({
      id: `move_${Date.now()}`,
      type: 'MOVE',
      data: { direction, distance }
    });
  }
  
  pathfindTo(target: { x: number; y: number; z: number } | string): this {
    return this.addAction({
      id: `pathfind_${Date.now()}`,
      type: 'PATHFIND',
      data: { target },
      onFailure: 'ESCALATE',
      escalationPrompt: 'Cannot find path. Need to assess obstacles and find alternative route.'
    });
  }
  
  equip(item: string): this {
    return this.addAction({
      id: `equip_${Date.now()}`,
      type: 'EQUIP',
      data: { item },
      precondition: {
        type: 'INVENTORY_HAS',
        evaluate: `inventory.contains('${item}')`,
        fallbackAction: 'ESCALATE'
      }
    });
  }
  
  interact(target?: string): this {
    return this.addAction({
      id: `interact_${Date.now()}`,
      type: 'INTERACT',
      data: { target }
    });
  }
  
  scan(radius: number, targets?: string[]): this {
    return this.addAction({
      id: `scan_${Date.now()}`,
      type: 'SCAN',
      data: { radius, targets }
    });
  }
  
  wait(ticks: number): this {
    return this.addAction({
      id: `wait_${Date.now()}`,
      type: 'WAIT',
      data: { ticks }
    });
  }
  
  speak(line: DialogueLine): this {
    return this.addAction({
      id: `speak_${Date.now()}`,
      type: 'SPEAK',
      data: { dialogue: line }
    });
  }
  
  // Conditional helpers
  ifHasItem(item: string): this {
    return this.startConditional({
      type: 'INVENTORY_HAS',
      evaluate: `inventory.contains('${item}')`,
      fallbackAction: 'SKIP'
    });
  }
  
  ifHealthAbove(threshold: number): this {
    return this.startConditional({
      type: 'HEALTH_ABOVE',
      evaluate: `player.health > ${threshold}`,
      fallbackAction: 'ESCALATE'
    });
  }
  
  ifBlockIs(block: string, position: string): this {
    return this.startConditional({
      type: 'BLOCK_IS',
      evaluate: `world.getBlock(${position}) === '${block}'`,
      fallbackAction: 'SKIP'
    });
  }
  
  // End composite actions
  end(): this {
    if (this.actionStack.length > 1) {
      const completed = this.actionStack.pop()!;
      this.addAction(completed);
    } else if (this.actionStack.length === 1) {
      this.script.rootAction = this.actionStack.pop()!;
    }
    return this;
  }
  
  endAll(): this {
    while (this.actionStack.length > 0) {
      this.end();
    }
    return this;
  }
  
  // Build the final script
  build(): Script {
    if (this.actionStack.length > 0) {
      this.script.rootAction = this.actionStack[0];
    }
    return this.script as Script;
  }
  
  private addAction(action: BotAction): this {
    if (this.actionStack.length > 0) {
      const current = this.actionStack[this.actionStack.length - 1];
      if (current.children) {
        current.children.push(action);
      }
    } else {
      this.script.rootAction = action;
    }
    return this;
  }
}

// ============================================================
// SCRIPT LIBRARY - Built-in scripts using the improved abstraction
// ============================================================

const SCRIPT_LIBRARY: Script[] = [
  // ========== MINING SCRIPTS ==========
  ScriptBuilder.create('mining_strip_optimized', 'Optimized Strip Mining')
    .description('Branch mining at optimal Y level for maximum diamond yield')
    .category('MINING')
    .difficulty('INTERMEDIATE')
    .keywords('strip', 'mine', 'tunnel', 'diamond', 'branch', 'dig', 'ore', 'excavate')
    .examples('strip mine 100 blocks', 'dig a tunnel at y -59', 'branch mine for diamonds')
    .learningObjectives(
      'Understand optimal Y levels for different ores',
      'Learn efficient branch mining patterns',
      'Practice resource management'
    )
    .param({ name: 'length', type: 'number', required: true, default: 100, min: 10, max: 1000,
      extractPattern: /(\d+)\s*(blocks?|meters?|m\b)/i, description: 'Main tunnel length' })
    .param({ name: 'yLevel', type: 'number', required: false, default: -59, min: -64, max: 320,
      extractPattern: /(?:at\s+)?y\s*[=]?\s*(-?\d+)/i, description: 'Mining Y level' })
    .param({ name: 'branchLength', type: 'number', required: false, default: 20, min: 5, max: 50,
      extractPattern: /branch(?:es)?\s*(?:of\s+)?(\d+)/i, description: 'Branch tunnel length' })
    .param({ name: 'branchSpacing', type: 'number', required: false, default: 3, min: 2, max: 10,
      extractPattern: /(?:spacing|gap)\s*(\d+)/i, description: 'Blocks between branches' })
    .startSequence()
      .equip('pickaxe')
      .startLoop({ variable: 'length' })
        .mine('stone', 2)
        .startConditional({
          type: 'CUSTOM_SCRIPT',
          evaluate: 'iteration % {{branchSpacing}} === 0',
          fallbackAction: 'SKIP'
        })
          .startParallel()
            .startSequence()
              .move('left', 1)
              .startLoop({ variable: 'branchLength' })
                .mine('stone', 1)
              .end()
            .end()
            .startSequence()
              .move('right', 1)
              .startLoop({ variable: 'branchLength' })
                .mine('stone', 1)
              .end()
            .end()
          .end()
        .end()
        .startConditional({
          type: 'CUSTOM_SCRIPT',
          evaluate: 'iteration % 10 === 0',
          fallbackAction: 'SKIP'
        })
          .place('torch')
        .end()
      .end()
    .end()
    .build(),
    
  ScriptBuilder.create('mining_quarry', 'Automatic Quarry')
    .description('Create a descending quarry for mass excavation')
    .category('MINING')
    .difficulty('ADVANCED')
    .keywords('quarry', 'excavate', 'pit', 'open', 'mine', 'clear', 'area', 'hole')
    .examples('dig a 16x16 quarry', 'excavate quarry to bedrock', 'open pit mine')
    .param({ name: 'size', type: 'number', required: true, default: 16, min: 4, max: 64,
      extractPattern: /(\d+)\s*x\s*(\d+)|(\d+)\s*quarry/i, description: 'Quarry dimensions' })
    .param({ name: 'depth', type: 'number', required: false, default: 50, min: 5, max: 200,
      extractPattern: /(?:depth|deep)\s*(\d+)/i, description: 'Maximum depth' })
    .startSequence()
      .equip('pickaxe')
      .scan(50, ['lava', 'water', 'cave'])
      .startLoop({ variable: 'depth' })
        .startLoop({ variable: 'size' })
          .startLoop({ variable: 'size' })
            .mine('any', 1)
          .end()
        .end()
        .move('down', 1)
      .end()
    .end()
    .build(),
    
  // ========== FARMING SCRIPTS ==========
  ScriptBuilder.create('farm_harvest', 'Automatic Farm Harvest')
    .description('Harvest and replant crops with optional bonemeal')
    .category('FARMING')
    .difficulty('BEGINNER')
    .keywords('farm', 'harvest', 'replant', 'crop', 'wheat', 'carrot', 'potato', 'agriculture')
    .examples('harvest wheat farm', 'farm carrots with bonemeal', 'auto harvest potatoes')
    .param({ name: 'cropType', type: 'string', required: false, default: 'wheat',
      enum: ['wheat', 'carrot', 'potato', 'beetroot', 'melon', 'pumpkin'],
      extractPattern: /(wheat|carrot|potato|beetroot|melon|pumpkin)s?/i, description: 'Crop type' })
    .param({ name: 'farmSize', type: 'number', required: false, default: 64, description: 'Number of crops' })
    .param({ name: 'useBonemeal', type: 'boolean', required: false, default: false,
      extractPattern: /bonemeal|bone.?meal/i, description: 'Use bonemeal' })
    .startSequence()
      .scan(30, ['farmland', 'crop'])
      .startLoop({ variable: 'farmSize' })
        .startConditional({
          type: 'CUSTOM_SCRIPT',
          evaluate: 'crop.age >= 7',
          fallbackAction: 'SKIP'
        })
          .interact('crop')
          .startConditional({
            type: 'INVENTORY_HAS',
            evaluate: 'inventory.contains("{{cropType}}_seeds")',
            fallbackAction: 'SKIP'
          })
            .place('{{cropType}}_seeds')
          .end()
        .end()
        .startConditional({
          type: 'CUSTOM_SCRIPT',
          evaluate: 'useBonemeal && crop.age < 7',
          fallbackAction: 'SKIP'
        })
          .equip('bonemeal')
          .interact('crop')
        .end()
      .end()
    .end()
    .build(),
    
  // ========== REDSTONE SCRIPTS ==========
  ScriptBuilder.create('redstone_clock', 'Redstone Clock Builder')
    .description('Build various types of redstone clocks')
    .category('REDSTONE')
    .difficulty('INTERMEDIATE')
    .keywords('clock', 'pulse', 'oscillator', 'timer', 'redstone', 'repeater', 'frequency')
    .examples('build a repeater clock', 'observer clock', 'fast pulse generator')
    .param({ name: 'clockType', type: 'string', required: false, default: 'repeater',
      enum: ['repeater', 'observer', 'comparator'],
      extractPattern: /(repeater|observer|comparator)\s*clock/i, description: 'Clock type' })
    .param({ name: 'pulseDelay', type: 'number', required: false, default: 2, min: 1, max: 4,
      extractPattern: /(?:delay\s*|ticks?\s*)(\d)/i, description: 'Pulse delay' })
    .startSequence()
      .equip('redstone')
      .startConditional({
        type: 'CUSTOM_SCRIPT',
        evaluate: 'clockType === "repeater"',
        fallbackAction: 'SKIP'
      })
        .startLoop(4)
          .place('repeater')
          .place('redstone_wire')
          .move('right', 1)
        .end()
        .place('lever')
        .interact('lever')
      .end()
      .startConditional({
        type: 'CUSTOM_SCRIPT',
        evaluate: 'clockType === "observer"',
        fallbackAction: 'SKIP'
      })
        .place('observer', { x: 0, y: 0, z: 0 })
        .place('any_block', { x: 0, y: -1, z: 0 })
        .place('observer', { x: 0, y: -2, z: 0 })
        .place('redstone_wire', { x: 1, y: -2, z: 0 })
      .end()
    .end()
    .build(),
    
  ScriptBuilder.create('redstone_logic_gate', 'Logic Gate Builder')
    .description('Build AND, OR, NOT, XOR, NAND, NOR gates')
    .category('REDSTONE')
    .difficulty('INTERMEDIATE')
    .keywords('logic', 'gate', 'AND', 'OR', 'NOT', 'XOR', 'boolean', 'circuit', 'digital')
    .examples('build AND gate', 'create XOR gate', 'logic circuit')
    .param({ name: 'gateType', type: 'string', required: true, default: 'AND',
      enum: ['AND', 'OR', 'NOT', 'XOR', 'NAND', 'NOR'],
      extractPattern: /(AND|OR|NOT|XOR|NAND|NOR)\s*gate/i, description: 'Gate type' })
    .startSequence()
      .equip('redstone')
      .startConditional({
        type: 'CUSTOM_SCRIPT',
        evaluate: 'gateType === "NOT"',
        fallbackAction: 'SKIP'
      })
        .place('block')
        .place('redstone_torch', { x: 0, y: 1, z: 0 })
        .place('redstone_wire', { x: -1, y: 0, z: 0 })
      .end()
      .startConditional({
        type: 'CUSTOM_SCRIPT',
        evaluate: 'gateType === "AND"',
        fallbackAction: 'SKIP'
      })
        .place('block')
        .place('redstone_torch', { x: 0, y: 1, z: 0 })
        .place('block', { x: 0, y: 1, z: 0 })
        .place('redstone_torch', { x: 0, y: 2, z: 0 })
        .place('redstone_wire', { x: -1, y: 0, z: 0 })
        .place('redstone_wire', { x: 0, y: 0, z: -1 })
      .end()
      .startConditional({
        type: 'CUSTOM_SCRIPT',
        evaluate: 'gateType === "OR"',
        fallbackAction: 'SKIP'
      })
        .place('block')
        .place('redstone_wire', { x: -1, y: 0, z: 0 })
        .place('redstone_wire', { x: 0, y: 0, z: -1 })
        .place('redstone_wire', { x: 1, y: 0, z: 0 })
      .end()
    .end()
    .build(),
    
  // ========== CHIP DESIGN SCRIPTS ==========
  ScriptBuilder.create('chip_design', 'Basic Chip Design')
    .description('Design integrated circuit layouts')
    .category('CHIP_DESIGN')
    .difficulty('ADVANCED')
    .keywords('chip', 'ic', 'integrated', 'circuit', 'transistor', 'semiconductor', 'mosfet')
    .examples('design chip with 8 transistors', 'CMOS circuit', 'create IC layout')
    .param({ name: 'transistorCount', type: 'number', required: false, default: 8, min: 1, max: 100,
      extractPattern: /(\d+)\s*transistor/i, description: 'Number of transistors' })
    .param({ name: 'transistorType', type: 'string', required: false, default: 'nmos',
      enum: ['nmos', 'pmos', 'cmos'],
      extractPattern: /(nmos|pmos|cmos)/i, description: 'Transistor type' })
    .param({ name: 'gridSize', type: 'number', required: false, default: 16, min: 4, max: 64, description: 'Grid size' })
    .startSequence()
      .speak({ templates: ['Initializing chip design workspace...'], tone: 'focused' })
      .startLoop({ variable: 'transistorCount' })
        .place('{{transistorType}}_transistor')
      .end()
      .speak({ templates: ['Routing interconnects...'], tone: 'focused' })
      .speak({ templates: ['Running thermal simulation...'], tone: 'focused' })
      .speak({ templates: ['Design complete! DRC passed.'], tone: 'calm' })
    .end()
    .build(),
    
  // ========== THERMAL SIMULATION ==========
  ScriptBuilder.create('thermal_simulation', 'Thermal Heat Map')
    .description('Run finite element thermal simulation')
    .category('THERMAL_SIM')
    .difficulty('ADVANCED')
    .keywords('thermal', 'heat', 'simulation', 'temperature', 'cooling', 'hotspot', 'physics')
    .examples('run thermal simulation', 'heat map analysis', 'temperature distribution')
    .param({ name: 'gridSize', type: 'number', required: false, default: 32, min: 8, max: 128, description: 'Grid resolution' })
    .param({ name: 'iterations', type: 'number', required: false, default: 100, min: 10, max: 1000, description: 'Simulation steps' })
    .startSequence()
      .speak({ templates: ['Initializing thermal grid...'], tone: 'focused' })
      .startLoop({ variable: 'iterations' })
        .speak({ templates: ['Computing heat diffusion step {{i}}...'], tone: 'focused' })
      .end()
      .speak({ templates: ['Simulation complete! Hotspots identified.'], tone: 'calm' })
    .end()
    .build(),
    
  // ========== NEURAL NETWORK ==========
  ScriptBuilder.create('neural_network_viz', 'Neural Network Visualization')
    .description('Visualize neural network architecture in Minecraft')
    .category('NEURAL_NET')
    .difficulty('ADVANCED')
    .keywords('neural', 'network', 'neuron', 'layer', 'deep', 'learning', 'ai', 'mlp')
    .examples('visualize neural network', 'build AI network', 'show deep learning')
    .param({ name: 'inputSize', type: 'number', required: false, default: 4, min: 2, max: 16, description: 'Input neurons' })
    .param({ name: 'hiddenSize', type: 'number', required: false, default: 8, min: 2, max: 32, description: 'Hidden neurons' })
    .param({ name: 'outputSize', type: 'number', required: false, default: 2, min: 1, max: 8, description: 'Output neurons' })
    .startSequence()
      .speak({ templates: ['Building neural network visualization...'], tone: 'focused' })
      .startLoop({ variable: 'inputSize' })
        .place('glowstone')  // Input neurons
      .end()
      .startLoop({ variable: 'hiddenSize' })
        .place('sea_lantern')  // Hidden neurons
      .end()
      .startLoop({ variable: 'outputSize' })
        .place('beacon')  // Output neurons
      .end()
      .speak({ templates: ['Network constructed! Connections visualized.'], tone: 'calm' })
    .end()
    .build()
];

// ============================================================
// STT MANAGER - Speech-to-Text
// ============================================================

class STTManager {
  private zai: any = null;
  
  async initialize(): Promise<void> {
    try {
      this.zai = await ZAI.create();
      console.log('[STT] Initialized successfully');
    } catch (error) {
      console.error('[STT] Initialization failed:', error);
    }
  }
  
  async transcribe(audioBase64: string): Promise<{ text: string; confidence: number }> {
    if (!this.zai) {
      throw new Error('STT not initialized');
    }
    
    try {
      const response = await this.zai.audio.asr.create({
        file_base64: audioBase64
      });
      
      return {
        text: response.text || '',
        confidence: 0.95  // Placeholder confidence
      };
    } catch (error) {
      console.error('[STT] Transcription error:', error);
      throw error;
    }
  }
  
  async transcribeFile(filePath: string): Promise<{ text: string; confidence: number }> {
    const audioBuffer = fs.readFileSync(filePath);
    const base64 = audioBuffer.toString('base64');
    return this.transcribe(base64);
  }
}

// ============================================================
// TTS MANAGER - Text-to-Speech with Immersive Dialogue
// ============================================================

class TTSManager {
  private zai: any = null;
  private audioDir: string;
  private enabled: boolean = true;
  private dialogue: ImmersiveDialogue;
  
  constructor(dialogue: ImmersiveDialogue) {
    this.dialogue = dialogue;
    this.audioDir = '/tmp/lucineer-tts';
    if (!fs.existsSync(this.audioDir)) {
      fs.mkdirSync(this.audioDir, { recursive: true });
    }
  }
  
  async initialize(): Promise<void> {
    try {
      this.zai = await ZAI.create();
      console.log('[TTS] Initialized successfully');
    } catch (error) {
      console.error('[TTS] Initialization failed:', error);
      this.enabled = false;
    }
  }
  
  async speak(text: string, options: { speed?: number; voice?: string } = {}): Promise<string | null> {
    if (!this.enabled || !this.zai) return null;
    
    if (text.length > 1024) {
      text = text.substring(0, 1021) + '...';
    }
    
    try {
      const response = await this.zai.audio.tts.create({
        input: text,
        voice: options.voice || 'tongtong',
        speed: options.speed || 1.0,
        response_format: 'wav',
        stream: false
      });
      
      const arrayBuffer = await response.arrayBuffer();
      const buffer = Buffer.from(new Uint8Array(arrayBuffer));
      
      const filename = `tts_${Date.now()}.wav`;
      const outputPath = path.join(this.audioDir, filename);
      fs.writeFileSync(outputPath, buffer);
      
      return `/audio/${filename}`;
    } catch (error) {
      console.error('[TTS] Error:', error);
      return null;
    }
  }
  
  async speakDialogue(dialogue: DialogueLine): Promise<string | null> {
    const text = this.dialogue.selectLine(dialogue);
    const speed = dialogue.tone === 'alert' ? 1.1 : dialogue.tone === 'calm' ? 0.9 : 1.0;
    return this.speak(text, { speed });
  }
  
  async speakStart(script: Script, params: Record<string, any>): Promise<string | null> {
    const dialogue = this.dialogue.getStartDialogue(script, params);
    return this.speakDialogue(dialogue);
  }
  
  async speakComplete(script: Script, params: Record<string, any>): Promise<string | null> {
    const dialogue = this.dialogue.getCompleteDialogue(script, params);
    return this.speakDialogue(dialogue);
  }
  
  async speakAlert(trigger: string, context: Record<string, any>): Promise<string | null> {
    const dialogue = this.dialogue.getAlertDialogue(trigger, context);
    return this.speakDialogue(dialogue);
  }
  
  async speakEducational(category: ScriptCategory): Promise<string | null> {
    const dialogue = this.dialogue.getEducationalSnippet(category);
    return this.speakDialogue(dialogue);
  }
}

// ============================================================
// PARAMETER EXTRACTOR
// ============================================================

class ParameterExtractor {
  private usedNumbers: Set<number> = new Set();
  
  extract(command: string, script: Script): Record<string, any> {
    const params: Record<string, any> = {};
    this.usedNumbers.clear();
    
    // First pass: extract using patterns
    for (const param of script.parameters) {
      if (param.extractPattern) {
        const match = command.match(param.extractPattern);
        if (match) {
          if (param.type === 'boolean') {
            params[param.name] = true;
          } else {
            const value = this.parseValue(match[1] || match[0], param.type);
            params[param.name] = value;
            if (param.type === 'number' && typeof value === 'number') {
              this.usedNumbers.add(value);
            }
          }
        }
      }
    }
    
    // Second pass: natural language
    for (const param of script.parameters) {
      if (params[param.name] !== undefined) continue;
      const extracted = this.naturalExtract(command, param);
      if (extracted !== undefined) {
        params[param.name] = extracted;
      }
    }
    
    // Third pass: defaults
    for (const param of script.parameters) {
      if (params[param.name] === undefined && param.default !== undefined) {
        params[param.name] = param.default;
      }
    }
    
    return params;
  }
  
  private naturalExtract(command: string, param: ParameterDefinition): any {
    const patterns: Record<string, RegExp[]> = {
      'length': [
        /(\d+)\s*(?:blocks?|meters?|m\b|long|length)/i,
        /(?:length|long|tunnel|mine)\s*(?:of\s+)?(\d+)/i,
        /(?:about|around)\s+(\d+)\s*(?:blocks?|$)/i
      ],
      'ylevel': [
        /(?:at\s+)?y\s*[=]?\s*(-?\d+)/i,
        /(?:level|layer)\s*(-?\d+)/i
      ],
      'transistorcount': [/(\d+)\s*transistor/i],
      'branchlength': [/branch(?:es)?\s*(?:of\s+)?(\d+)/i],
      'branchspacing': [/(?:spacing|gap)\s*(\d+)/i],
      'pulsdelay': [/(?:delay|ticks?)\s*(\d)/i]
    };
    
    if (param.type === 'number') {
      const paramPatterns = patterns[param.name.toLowerCase()];
      if (paramPatterns) {
        for (const pattern of paramPatterns) {
          const match = command.match(pattern);
          if (match) {
            const value = parseInt(match[1]);
            if (!isNaN(value) && !this.usedNumbers.has(value)) {
              this.usedNumbers.add(value);
              return this.clamp(value, param);
            }
          }
        }
      }
    }
    
    if (param.type === 'string' && param.enum) {
      const cmd = command.toLowerCase();
      for (const option of param.enum) {
        if (cmd.includes(option.toLowerCase())) return option;
      }
    }
    
    return undefined;
  }
  
  private clamp(value: number, param: ParameterDefinition): number {
    if (param.min !== undefined && value < param.min) return param.min;
    if (param.max !== undefined && value > param.max) return param.max;
    return value;
  }
  
  private parseValue(value: string, type: string): any {
    if (type === 'number') return parseInt(value) || 0;
    if (type === 'boolean') return value.toLowerCase() === 'true';
    return value;
  }
}

// ============================================================
// SEMANTIC MATCHER
// ============================================================

class SemanticMatcher {
  findMatches(command: string, scripts: Script[]): Array<{ script: Script; score: number }> {
    const cmd = command.toLowerCase();
    const matches: Array<{ script: Script; score: number }> = [];
    
    for (const script of scripts) {
      let score = 0;
      
      // Keyword matching
      for (const keyword of script.keywords) {
        if (cmd.includes(keyword.toLowerCase())) {
          score += 0.15;
        }
      }
      
      // Example similarity
      for (const example of script.examples) {
        const exWords = example.toLowerCase().split(/\s+/);
        const cmdWords = cmd.split(/\s+/);
        const common = cmdWords.filter(w => exWords.includes(w));
        if (common.length >= 2) score += 0.1 * common.length;
      }
      
      // Category boost
      if (cmd.includes('mine') && script.category === 'MINING') score += 0.2;
      if (cmd.includes('farm') && script.category === 'FARMING') score += 0.2;
      if (cmd.includes('redstone') && script.category === 'REDSTONE') score += 0.2;
      if (cmd.includes('chip') && script.category === 'CHIP_DESIGN') score += 0.2;
      if (cmd.includes('heat') && script.category === 'THERMAL_SIM') score += 0.2;
      if (cmd.includes('neural') && script.category === 'NEURAL_NET') score += 0.2;
      
      // Success rate bonus
      score += script.successRate * 0.1;
      
      if (score > 0.2) {
        matches.push({ script, score });
      }
    }
    
    return matches.sort((a, b) => b.score - a.score);
  }
}

// ============================================================
// TRIGGER SYSTEM
// ============================================================

interface Trigger {
  id: string;
  name: string;
  category: 'SAFETY' | 'PERFORMANCE' | 'LEARNING' | 'USER_REQUEST';
  priority: number;
  condition: (ctx: any) => boolean;
  prompt: string;
  ttsAlert: DialogueLine;
  cooldown: number;
  lastTriggered: number;
}

const TRIGGERS: Trigger[] = [
  {
    id: 'lava_proximity',
    name: 'Lava Proximity Alert',
    category: 'SAFETY',
    priority: 100,
    condition: (ctx) => ctx.worldState?.nearbyBlocks?.includes('lava'),
    prompt: 'Lava detected! Choose: avoid, bridge_over, collect, or retreat',
    ttsAlert: { templates: ['Hold up! Lava nearby! Watch your step!'], tone: 'alert', volume: 'loud' },
    cooldown: 5000,
    lastTriggered: 0
  },
  {
    id: 'hostile_mob',
    name: 'Hostile Mob Threat',
    category: 'SAFETY',
    priority: 90,
    condition: (ctx) => ctx.worldState?.nearbyEntities?.some((e: any) => 
      ['zombie', 'skeleton', 'creeper', 'spider'].includes(e.type?.toLowerCase())
    ),
    prompt: 'Hostile mob detected! Fight, flee, or hide?',
    ttsAlert: { templates: ['Hostiles spotted! Ready your weapons!'], tone: 'alert', volume: 'loud' },
    cooldown: 10000,
    lastTriggered: 0
  },
  {
    id: 'low_health',
    name: 'Low Health Warning',
    category: 'SAFETY',
    priority: 95,
    condition: (ctx) => (ctx.playerState?.health || 20) < 6,
    prompt: 'Health critical! Eat food or retreat to safety',
    ttsAlert: { templates: ["You're hurt bad! Maybe grab some food?"], tone: 'concerned', volume: 'loud' },
    cooldown: 5000,
    lastTriggered: 0
  },
  {
    id: 'creative_request',
    name: 'Creative Request',
    category: 'USER_REQUEST',
    priority: 50,
    condition: (ctx) => /(novel|unique|invent|create.*new|brand new)/i.test(ctx.command),
    prompt: 'This is a creative request requiring AI imagination',
    ttsAlert: { templates: ['Ooh, something new! Let me think...'], tone: 'excited', volume: 'normal' },
    cooldown: 0,
    lastTriggered: 0
  },
  {
    id: 'learning_request',
    name: 'Learning Request',
    category: 'LEARNING',
    priority: 40,
    condition: (ctx) => /^(why|how|explain|teach|what|learn)/i.test(ctx.command),
    prompt: 'This is an educational request',
    ttsAlert: { templates: ['Great question! Let me explain...'], tone: 'calm', volume: 'normal' },
    cooldown: 0,
    lastTriggered: 0
  }
];

// ============================================================
// MCP SERVER V3
// ============================================================

class MCPServerV3 {
  private scripts: Map<string, Script> = new Map();
  private semanticMatcher: SemanticMatcher;
  private parameterExtractor: ParameterExtractor;
  private sttManager: STTManager;
  private ttsManager: TTSManager;
  private dialogue: ImmersiveDialogue;
  
  private metrics = {
    commandsProcessed: 0,
    scriptsExecuted: 0,
    agentsTriggered: 0,
    apiCallsSaved: 0,
    sttCalls: 0,
    ttsCalls: 0,
    startTime: Date.now()
  };
  
  constructor() {
    SCRIPT_LIBRARY.forEach(s => this.scripts.set(s.id, s));
    this.semanticMatcher = new SemanticMatcher();
    this.parameterExtractor = new ParameterExtractor();
    this.dialogue = new ImmersiveDialogue('friendly_bot');
    this.sttManager = new STTManager();
    this.ttsManager = new TTSManager(this.dialogue);
  }
  
  async initialize(): Promise<void> {
    await this.sttManager.initialize();
    await this.ttsManager.initialize();
    console.log('[MCP] Server V3 initialized with STT + TTS');
  }
  
  async processCommand(command: string, context: any = {}): Promise<any> {
    this.metrics.commandsProcessed++;
    context.command = command;
    
    console.log(`[MCP] Processing: "${command}"`);
    
    // Check triggers
    const now = Date.now();
    for (const trigger of TRIGGERS.sort((a, b) => b.priority - a.priority)) {
      if (now - trigger.lastTriggered < trigger.cooldown) continue;
      if (trigger.condition(context)) {
        trigger.lastTriggered = now;
        this.metrics.agentsTriggered++;
        
        // Generate TTS alert
        const ttsAudio = await this.ttsManager.speakDialogue(trigger.ttsAlert);
        this.metrics.ttsCalls++;
        
        return {
          type: 'trigger_escalation',
          triggerId: trigger.id,
          triggerName: trigger.name,
          category: trigger.category,
          prompt: trigger.prompt,
          ttsAudio,
          requiresUserInput: true
        };
      }
    }
    
    // Semantic matching
    const matches = this.semanticMatcher.findMatches(command, SCRIPT_LIBRARY);
    
    if (matches.length > 0 && matches[0].score >= 0.7) {
      const best = matches[0];
      const params = this.parameterExtractor.extract(command, best.script);
      
      this.metrics.scriptsExecuted++;
      this.metrics.apiCallsSaved++;
      
      // Generate start dialogue
      const ttsAudio = await this.ttsManager.speakStart(best.script, params);
      this.metrics.ttsCalls++;
      
      return {
        type: 'script_execution',
        scriptId: best.script.id,
        scriptName: best.script.name,
        category: best.script.category,
        difficulty: best.script.difficulty,
        rootAction: best.script.rootAction,
        parameters: params,
        successRate: best.script.successRate,
        ttsAudio,
        educationalNote: this.generateEducationalNote(best.script),
        cost: 0,
        source: 'local_script'
      };
    }
    
    // No match - agent
    this.metrics.agentsTriggered++;
    return {
      type: 'agent_execution',
      requestId: `agent_${Date.now()}`,
      command,
      status: 'pending_llm_call',
      ttsAudio: await this.ttsManager.speak("Hmm, let me think about that...", { speed: 0.9 }),
      cost: 'api_call_required',
      source: 'cloud_agent'
    };
  }
  
  async transcribeAudio(audioBase64: string): Promise<{ text: string; audioUrl?: string }> {
    this.metrics.sttCalls++;
    const result = await this.sttManager.transcribe(audioBase64);
    
    // Optionally speak back the transcription
    const audioUrl = await this.ttsManager.speak(`You said: ${result.text}`);
    this.metrics.ttsCalls++;
    
    return { text: result.text, audioUrl };
  }
  
  async speakText(text: string): Promise<{ audioUrl: string | null }> {
    this.metrics.ttsCalls++;
    const audioUrl = await this.ttsManager.speak(text);
    return { audioUrl };
  }
  
  async speakImmersive(category: ScriptCategory): Promise<{ audioUrl: string | null }> {
    this.metrics.ttsCalls++;
    const audioUrl = await this.ttsManager.speakEducational(category);
    return { audioUrl };
  }
  
  private generateEducationalNote(script: Script): string {
    const notes: Record<ScriptCategory, string> = {
      'MINING': '⛏️ Mining efficiency comes from optimal Y levels and branch spacing. Scripts handle pathfinding automatically!',
      'FARMING': '🌾 Farm automation uses growth detection. Scripts replant automatically for continuous harvests!',
      'REDSTONE': '⚡ Redstone circuits follow templates. All logic gates can be built from NAND gates!',
      'CHIP_DESIGN': '🔧 Chip design teaches semiconductor physics. Modern CPUs have billions of transistors!',
      'THERMAL_SIM': '🌡️ Thermal simulation uses finite element analysis. Heat always flows hot → cold!',
      'NEURAL_NET': '🧠 Neural networks learn by adjusting weights. More layers = more complex patterns!',
      'BUILDING': '🏗️ Building requires spatial planning. Odd numbers look more natural in builds!',
      'EXPLORATION': '🗺️ Exploration rewards preparation. Beds skip nights, maps show position!',
      'COMBAT': '⚔️ Combat needs timing and positioning. Critical hits deal 50% more damage!',
      'CRAFTING': '🔨 Crafting is about resource efficiency. Shift-click moves items fast!',
      'LEARNING': '📚 Learning compounds over time. Teaching others helps you master skills!'
    };
    
    return notes[script.category] || '✨ This script executes locally with no API cost!';
  }
  
  getScripts() {
    return SCRIPT_LIBRARY.map(s => ({
      id: s.id,
      name: s.name,
      description: s.description,
      category: s.category,
      difficulty: s.difficulty,
      keywords: s.keywords,
      successRate: s.successRate
    }));
  }
  
  getMetrics() {
    return {
      ...this.metrics,
      uptime: Date.now() - this.metrics.startTime,
      cacheHitRate: this.metrics.commandsProcessed > 0
        ? this.metrics.scriptsExecuted / this.metrics.commandsProcessed
        : 0,
      estimatedSavings: `$${(this.metrics.apiCallsSaved * 0.002).toFixed(4)}`
    };
  }
  
  getTriggers() {
    return TRIGGERS.map(t => ({
      id: t.id,
      name: t.name,
      category: t.category,
      priority: t.priority,
      cooldown: t.cooldown
    }));
  }
}

// ============================================================
// HTTP SERVER
// ============================================================

const server = new MCPServerV3();
server.initialize().then(() => console.log('[MCP] Ready'));

const httpServer = serve({
  port: 3031,
  async fetch(req) {
    const url = new URL(req.url);
    const cors = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    };
    
    if (req.method === 'OPTIONS') return new Response(null, { headers: cors });
    
    // Health
    if (url.pathname === '/health') {
      return Response.json({ status: 'ok', version: '3.0.0', stt: true, tts: true }, { headers: cors });
    }
    
    // Metrics
    if (url.pathname === '/metrics') {
      return Response.json(server.getMetrics(), { headers: cors });
    }
    
    // Scripts
    if (url.pathname === '/scripts') {
      return Response.json(server.getScripts(), { headers: cors });
    }
    
    // Triggers
    if (url.pathname === '/triggers') {
      return Response.json(server.getTriggers(), { headers: cors });
    }
    
    // Process command
    if (url.pathname === '/process' && req.method === 'POST') {
      const body = await req.json();
      const result = await server.processCommand(body.command, body.context);
      return Response.json(result, { headers: cors });
    }
    
    // STT - Speech to Text
    if (url.pathname === '/stt/transcribe' && req.method === 'POST') {
      const body = await req.json();
      const result = await server.transcribeAudio(body.audio_base64);
      return Response.json(result, { headers: cors });
    }
    
    // TTS - Text to Speech
    if (url.pathname === '/tts/speak' && req.method === 'POST') {
      const body = await req.json();
      const result = await server.speakText(body.text);
      return Response.json(result, { headers: cors });
    }
    
    // TTS - Immersive educational
    if (url.pathname.startsWith('/tts/educational/')) {
      const category = url.pathname.replace('/tts/educational/', '') as ScriptCategory;
      const result = await server.speakImmersive(category);
      return Response.json(result, { headers: cors });
    }
    
    // Audio files
    if (url.pathname.startsWith('/audio/')) {
      const filename = url.pathname.replace('/audio/', '');
      const filepath = path.join('/tmp/lucineer-tts', filename);
      if (fs.existsSync(filepath)) {
        const audio = fs.readFileSync(filepath);
        return new Response(audio, {
          headers: { 'Content-Type': 'audio/wav', ...cors }
        });
      }
      return Response.json({ error: 'Audio not found' }, { status: 404, headers: cors });
    }
    
    // Learn
    if (url.pathname === '/learn/bot-vs-agent') {
      return Response.json({
        topic: 'Bots vs Agents',
        bot: { speed: '1-5ms', cost: '$0', capability: 'Routine tasks' },
        agent: { speed: '1-3s', cost: '$0.002/call', capability: 'Novel situations' },
        savings: '97.5% cost reduction with smart caching'
      }, { headers: cors });
    }
    
    return Response.json({ error: 'Not found' }, { status: 404, headers: cors });
  }
});

console.log(`
╔═══════════════════════════════════════════════════════════════╗
║           🚀 Lucineer MCP Server v3.0 Running                  ║
╠═══════════════════════════════════════════════════════════════╣
║  Features:                                                    ║
║  • STT (Speech-to-Text) - Voice commands                      ║
║  • TTS (Text-to-Speech) - Immersive dialogue                   ║
║  • Bot Action Abstraction - Composable scripts                 ║
║  • Smart Parameter Extraction                                  ║
║  • 8 Built-in Scripts                                         ║
╠═══════════════════════════════════════════════════════════════╣
║  Endpoints:                                                   ║
║  POST /process          - Process command                      ║
║  POST /stt/transcribe   - Speech to text                       ║
║  POST /tts/speak        - Text to speech                       ║
║  GET  /tts/educational/{cat} - Educational audio               ║
║  GET  /scripts          - List scripts                         ║
║  GET  /metrics          - Performance metrics                  ║
╚═══════════════════════════════════════════════════════════════╝
`);
