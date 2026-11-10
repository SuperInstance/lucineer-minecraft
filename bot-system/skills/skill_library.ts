/**
 * Skill Library
 * 
 * A collection of reusable skills that can be executed without LLM calls.
 * Skills are learned from successful agent executions and can be promoted
 * from PROPOSED → LEARNED → BUILT_IN based on success rate.
 */

export interface Skill {
  id: string;
  name: string;
  description: string;
  codeTemplate: string; // JavaScript template with {{param}} substitution
  parameters: SkillParameter[];
  applicabilityPattern: RegExp;
  requiredActions: string[];
  tier: 'BUILT_IN' | 'LEARNED' | 'PROPOSED';
  metrics: SkillMetrics;
  educationalContext: string;
}

export interface SkillParameter {
  name: string;
  type: 'number' | 'string' | 'boolean' | 'array' | 'position';
  default?: any;
  min?: number;
  max?: number;
  description: string;
}

export interface SkillMetrics {
  executionCount: number;
  successCount: number;
  failureCount: number;
  successRate: number;
  averageExecutionTime: number;
  lastExecuted: number;
  createdAt: number;
}

// Built-in skills for STEM learning
export const BUILT_IN_SKILLS: Skill[] = [
  {
    id: 'strip_mine_optimized',
    name: 'Optimized Strip Mining',
    description: 'Dig an optimal Y=-59 strip mine with branch tunnels for maximum ore yield',
    codeTemplate: `
// Optimized strip mining at Y=-59 for diamonds
var y = {{yLevel}};
var mainLength = {{mainLength}};
var branchLength = {{branchLength}};
var branchSpacing = {{branchSpacing}};

// Dig main tunnel
for (var i = 0; i < mainLength; i++) {
  agent.dig(startX + i, y, startZ);
  agent.dig(startX + i, y + 1, startZ); // 2 blocks tall
  
  // Place torches every 10 blocks
  if (i % 10 === 0) {
    agent.place('torch', startX + i, y + 2, startZ);
  }
  
  // Create branch tunnels
  if (i % branchSpacing === 0) {
    for (var j = 0; j < branchLength; j++) {
      agent.dig(startX + i, y, startZ + j + 1);
      agent.dig(startX + i, y + 1, startZ + j + 1);
    }
  }
}
`,
    parameters: [
      { name: 'yLevel', type: 'number', default: -59, min: -64, max: 320, description: 'Y level to mine at' },
      { name: 'mainLength', type: 'number', default: 100, min: 10, max: 1000, description: 'Length of main tunnel' },
      { name: 'branchLength', type: 'number', default: 20, min: 5, max: 50, description: 'Length of branch tunnels' },
      { name: 'branchSpacing', type: 'number', default: 3, min: 2, max: 10, description: 'Blocks between branches' }
    ],
    applicabilityPattern: /strip.*mine|branch.*mine|diamond.*mine|ore.*mine/i,
    requiredActions: ['dig', 'place'],
    tier: 'BUILT_IN',
    metrics: { executionCount: 0, successCount: 0, failureCount: 0, successRate: 0.98, averageExecutionTime: 0, lastExecuted: 0, createdAt: Date.now() },
    educationalContext: 'This skill teaches optimization: branch mining at Y=-59 maximizes diamond finds. The 3-block spacing ensures no ores are missed while minimizing wasted effort.'
  },
  {
    id: 'redstone_clock_builder',
    name: 'Redstone Clock Builder',
    description: 'Build various types of redstone clocks with adjustable timing',
    codeTemplate: `
// Build a redstone clock circuit
var clockType = "{{clockType}}";
var delay = {{delay}};
var size = {{size}};

if (clockType === "repeater") {
  // 4-repeater clock loop
  var directions = ['north', 'east', 'south', 'west'];
  for (var i = 0; i < 4; i++) {
    agent.place('repeater', startX + (i % 2) * 2, startY, startZ + Math.floor(i / 2) * 2);
    agent.setRepeaterDelay(startX + (i % 2) * 2, startY, startZ + Math.floor(i / 2) * 2, delay);
    
    // Redstone dust between repeaters
    agent.place('redstone_wire', startX + (i % 2) * 2 + 1, startY, startZ + Math.floor(i / 2) * 2);
  }
  
  // Activate with lever
  agent.place('lever', startX, startY + 1, startZ);
  agent.activate(startX, startY + 1, startZ);
}

if (clockType === "observer") {
  // Observer-based clock (faster)
  agent.place('observer', startX, startY, startZ, 'south');
  agent.place('observer', startX, startY, startZ + 1, 'north');
  agent.place('redstone_wire', startX, startY + 1, startZ);
}
`,
    parameters: [
      { name: 'clockType', type: 'string', default: 'repeater', description: 'Type of clock: repeater, observer, comparator' },
      { name: 'delay', type: 'number', default: 2, min: 1, max: 4, description: 'Repeater delay setting' },
      { name: 'size', type: 'number', default: 4, min: 2, max: 10, description: 'Clock loop size' }
    ],
    applicabilityPattern: /clock|pulse|oscillator|timer|redstone.*loop/i,
    requiredActions: ['place', 'setRepeaterDelay', 'activate'],
    tier: 'BUILT_IN',
    metrics: { executionCount: 0, successCount: 0, failureCount: 0, successRate: 0.95, averageExecutionTime: 0, lastExecuted: 0, createdAt: Date.now() },
    educationalContext: 'Redstone clocks are oscillators! They demonstrate frequency and timing circuits. Real chips use similar clock signals to synchronize operations.'
  },
  {
    id: 'logic_gate_builder',
    name: 'Logic Gate Builder',
    description: 'Build AND, OR, NOT, XOR, NAND, NOR gates using redstone',
    codeTemplate: `
// Build a logic gate
var gateType = "{{gateType}}";
var inputCount = {{inputCount}};

switch (gateType) {
  case "NOT":
    // Simple NOT gate with redstone torch
    agent.place('block', startX, startY, startZ);
    agent.place('redstone_torch', startX, startY + 1, startZ);
    agent.place('redstone_wire', startX - 1, startY, startZ); // Input
    agent.place('redstone_wire', startX + 1, startY, startZ); // Output
    break;
    
  case "AND":
    // AND gate: both inputs must be on
    agent.place('block', startX, startY, startZ);
    agent.place('block', startX + 1, startY, startZ);
    agent.place('redstone_torch', startX, startY + 1, startZ);
    agent.place('redstone_torch', startX + 1, startY + 1, startZ);
    agent.place('redstone_wire', startX + 2, startY, startZ); // Output
    agent.place('redstone_wire', startX - 1, startY, startZ); // Input A
    agent.place('redstone_wire', startX, startY, startZ + 1); // Input B
    break;
    
  case "OR":
    // OR gate: either input on
    agent.place('block', startX, startY, startZ);
    agent.place('redstone_wire', startX - 1, startY, startZ); // Input A
    agent.place('redstone_wire', startX, startY, startZ + 1); // Input B
    agent.place('redstone_wire', startX + 1, startY, startZ); // Output
    // Wire combines signals naturally
    break;
    
  case "XOR":
    // XOR: exactly one input on (more complex)
    // Build using AND, OR, NOT combination
    // ... (simplified for template)
    break;
}
`,
    parameters: [
      { name: 'gateType', type: 'string', default: 'AND', description: 'Type of logic gate' },
      { name: 'inputCount', type: 'number', default: 2, min: 1, max: 4, description: 'Number of inputs' }
    ],
    applicabilityPattern: /logic.*gate|AND|OR|NOT|XOR|NAND|NOR|boolean/i,
    requiredActions: ['place'],
    tier: 'BUILT_IN',
    metrics: { executionCount: 0, successCount: 0, failureCount: 0, successRate: 0.92, averageExecutionTime: 0, lastExecuted: 0, createdAt: Date.now() },
    educationalContext: 'Logic gates are the building blocks of computers! AND, OR, NOT can create any circuit. CPUs are made of billions of these gates.'
  },
  {
    id: 'thermal_simulation',
    name: 'Thermal Heat Simulation',
    description: 'Simulate heat distribution in a chip design',
    codeTemplate: `
// Finite element heat equation solver (simplified)
var gridSize = {{gridSize}};
var iterations = {{iterations}};
var thermalConductivity = {{thermalConductivity}};
var dt = 0.1; // Time step

// Initialize grid
var grid = [];
for (var x = 0; x < gridSize; x++) {
  grid[x] = [];
  for (var y = 0; y < gridSize; y++) {
    grid[x][y] = {{initialTemp}};
  }
}

// Set heat sources
{{#heatSources}}
grid[{{x}}][{{y}}] = {{temp}}; // Heat source
{{/heatSources}}

// Simulate heat diffusion
for (var iter = 0; iter < iterations; iter++) {
  var newGrid = [];
  for (var x = 1; x < gridSize - 1; x++) {
    newGrid[x] = [];
    for (var y = 1; y < gridSize - 1; y++) {
      // Heat equation: dT/dt = k * (d²T/dx² + d²T/dy²)
      var laplacian = 
        grid[x-1][y] + grid[x+1][y] + 
        grid[x][y-1] + grid[x][y+1] - 
        4 * grid[x][y];
      
      newGrid[x][y] = grid[x][y] + thermalConductivity * dt * laplacian;
    }
  }
  grid = newGrid;
}

// Output thermal map
agent.setOutput('thermalMap', grid);
`,
    parameters: [
      { name: 'gridSize', type: 'number', default: 16, min: 4, max: 64, description: 'Size of simulation grid' },
      { name: 'iterations', type: 'number', default: 100, min: 10, max: 1000, description: 'Number of simulation steps' },
      { name: 'thermalConductivity', type: 'number', default: 0.5, min: 0.1, max: 1.0, description: 'Heat transfer coefficient' },
      { name: 'initialTemp', type: 'number', default: 20, description: 'Initial temperature (°C)' }
    ],
    applicabilityPattern: /thermal|heat|simulation|temperature|cooling|chip.*temp/i,
    requiredActions: ['setOutput'],
    tier: 'BUILT_IN',
    metrics: { executionCount: 0, successCount: 0, failureCount: 0, successRate: 0.99, averageExecutionTime: 0, lastExecuted: 0, createdAt: Date.now() },
    educationalContext: 'This is real computational physics! The heat equation models how temperature spreads. Chip designers use this to prevent overheating. The same math describes diffusion and wave propagation.'
  },
  {
    id: 'neural_network_layer',
    name: 'Neural Network Layer Visualization',
    description: 'Visualize a single neural network layer in Minecraft',
    codeTemplate: `
// Build a visual neural network layer
var inputSize = {{inputSize}};
var outputSize = {{outputSize}};
var activation = "{{activation}}";

// Build input neurons (left column)
for (var i = 0; i < inputSize; i++) {
  agent.place('glowstone', startX, startY + i * 2, startZ);
  agent.label(startX, startY + i * 2, startZ, 'Input ' + i);
}

// Build output neurons (right column)
for (var j = 0; j < outputSize; j++) {
  agent.place('sea_lantern', startX + 10, startY + j * 2, startZ);
  agent.label(startX + 10, startY + j * 2, startZ, 'Output ' + j);
}

// Build connections (redstone dust for weights)
for (var i = 0; i < inputSize; i++) {
  for (var j = 0; j < outputSize; j++) {
    // Visualize weight strength with repeater delay
    var weight = {{weights}}[i][j];
    var delay = Math.floor(Math.abs(weight) * 4);
    agent.place('repeater', startX + 5, startY + (i + j) / 2, startZ);
    agent.setRepeaterDelay(startX + 5, startY + (i + j) / 2, startZ, delay);
  }
}

// Apply activation function visualization
if (activation === 'relu') {
  // ReLU: blocks negative values
  agent.place('piston', startX + 8, startY, startZ, 'east');
} else if (activation === 'sigmoid') {
  // Sigmoid: smooth transition
  agent.place('redstone_lamp', startX + 8, startY, startZ);
}
`,
    parameters: [
      { name: 'inputSize', type: 'number', default: 4, min: 2, max: 8, description: 'Number of input neurons' },
      { name: 'outputSize', type: 'number', default: 4, min: 2, max: 8, description: 'Number of output neurons' },
      { name: 'activation', type: 'string', default: 'relu', description: 'Activation function type' },
      { name: 'weights', type: 'array', default: [[0.5, 0.3, -0.2, 0.8]], description: 'Weight matrix' }
    ],
    applicabilityPattern: /neural.*network|layer|neuron|deep.*learning|mlp|perceptron/i,
    requiredActions: ['place', 'label', 'setRepeaterDelay'],
    tier: 'BUILT_IN',
    metrics: { executionCount: 0, successCount: 0, failureCount: 0, successRate: 0.90, averageExecutionTime: 0, lastExecuted: 0, createdAt: Date.now() },
    educationalContext: 'Neural networks are layers of interconnected "neurons." Each connection has a weight (strength). The activation function decides if a neuron "fires" - like a brain cell!'
  }
];

// Skill Library Manager
export class SkillLibrary {
  private skills: Map<string, Skill> = new Map();
  private readonly PROMOTION_THRESHOLD = 10; // Executions needed for promotion
  private readonly SUCCESS_THRESHOLD = 0.85; // Success rate for promotion

  constructor(initialSkills: Skill[] = BUILT_IN_SKILLS) {
    initialSkills.forEach(s => this.skills.set(s.id, s));
  }

  findApplicable(context: string): Skill | null {
    for (const skill of this.skills.values()) {
      if (skill.applicabilityPattern.test(context)) {
        return skill;
      }
    }
    return null;
  }

  generateCode(skillId: string, params: Record<string, any>): string {
    const skill = this.skills.get(skillId);
    if (!skill) throw new Error(`Skill not found: ${skillId}`);

    let code = skill.codeTemplate;
    
    // Substitute parameters
    for (const param of skill.parameters) {
      const value = params[param.name] ?? param.default;
      code = code.replace(new RegExp(`{{${param.name}}}`, 'g'), String(value));
    }

    return code;
  }

  recordExecution(skillId: string, success: boolean, executionTime: number): void {
    const skill = this.skills.get(skillId);
    if (!skill) return;

    skill.metrics.executionCount++;
    if (success) {
      skill.metrics.successCount++;
    } else {
      skill.metrics.failureCount++;
    }
    skill.metrics.successRate = skill.metrics.successCount / skill.metrics.executionCount;
    skill.metrics.averageExecutionTime = 
      (skill.metrics.averageExecutionTime * (skill.metrics.executionCount - 1) + executionTime) / 
      skill.metrics.executionCount;
    skill.metrics.lastExecuted = Date.now();

    // Check for promotion
    this.checkPromotion(skillId);
  }

  private checkPromotion(skillId: string): void {
    const skill = this.skills.get(skillId);
    if (!skill) return;

    if (skill.tier === 'PROPOSED' && 
        skill.metrics.executionCount >= this.PROMOTION_THRESHOLD &&
        skill.metrics.successRate >= this.SUCCESS_THRESHOLD) {
      skill.tier = 'LEARNED';
      console.log(`[SkillLibrary] Skill ${skillId} promoted to LEARNED`);
    }

    if (skill.tier === 'LEARNED' &&
        skill.metrics.executionCount >= this.PROMOTION_THRESHOLD * 3 &&
        skill.metrics.successRate >= this.SUCCESS_THRESHOLD + 0.05) {
      skill.tier = 'BUILT_IN';
      console.log(`[SkillLibrary] Skill ${skillId} promoted to BUILT_IN`);
    }
  }

  addSkill(skill: Skill): void {
    this.skills.set(skill.id, skill);
  }

  createSkillFromPattern(name: string, codeTemplate: string, pattern: RegExp): Skill {
    const skill: Skill = {
      id: `learned_${name.toLowerCase().replace(/\s+/g, '_')}_${Date.now()}`,
      name,
      description: `Auto-generated skill from successful execution patterns`,
      codeTemplate,
      parameters: this.extractParameters(codeTemplate),
      applicabilityPattern: pattern,
      requiredActions: this.extractActions(codeTemplate),
      tier: 'PROPOSED',
      metrics: {
        executionCount: 0,
        successCount: 0,
        failureCount: 0,
        successRate: 0,
        averageExecutionTime: 0,
        lastExecuted: 0,
        createdAt: Date.now()
      },
      educationalContext: 'This skill was learned from observing successful task patterns.'
    };

    this.addSkill(skill);
    return skill;
  }

  private extractParameters(template: string): SkillParameter[] {
    const params: SkillParameter[] = [];
    const matches = template.matchAll(/{{(\w+)}}/g);
    const seen = new Set<string>();

    for (const match of matches) {
      const name = match[1];
      if (!seen.has(name)) {
        seen.add(name);
        params.push({
          name,
          type: 'number',
          description: `Parameter: ${name}`
        });
      }
    }

    return params;
  }

  private extractActions(template: string): string[] {
    const actions: string[] = [];
    const matches = template.matchAll(/agent\.(\w+)\(/g);
    const seen = new Set<string>();

    for (const match of matches) {
      const action = match[1];
      if (!seen.has(action)) {
        seen.add(action);
        actions.push(action);
      }
    }

    return actions;
  }

  getAll(): Skill[] {
    return Array.from(this.skills.values());
  }

  getByTier(tier: 'BUILT_IN' | 'LEARNED' | 'PROPOSED'): Skill[] {
    return this.getAll().filter(s => s.tier === tier);
  }

  getMetrics(): { total: number; byTier: Record<string, number>; avgSuccessRate: number } {
    const all = this.getAll();
    return {
      total: all.length,
      byTier: {
        BUILT_IN: all.filter(s => s.tier === 'BUILT_IN').length,
        LEARNED: all.filter(s => s.tier === 'LEARNED').length,
        PROPOSED: all.filter(s => s.tier === 'PROPOSED').length
      },
      avgSuccessRate: all.reduce((sum, s) => sum + s.metrics.successRate, 0) / all.length
    };
  }
}
