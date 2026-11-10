/**
 * Educational Adventure System for Lucineer Minecraft Mod
 * 
 * Manages adventure progress, achievements, and educational content
 * for STEM learning through gameplay.
 */

export interface AdventureObjective {
  id: string
  description: string
  target_value: number
  reward_xp: number
}

export interface Adventure {
  adventure_id: string
  title: string
  category: 'BINARY' | 'LOGIC_GATES' | 'CHIP_DESIGN' | 'NETWORKING' | 'MEMORY' | 'ALGORITHMS' | 'DATA_STRUCTURES'
  difficulty: number
  description: string
  objectives: AdventureObjective[]
  game_mechanics: {
    interaction_type: string
    win_condition: string
    hints_enabled: boolean
    [key: string]: any
  }
  visual_theme: {
    primary_color: string
    secondary_color: string
    particle_effects: boolean
    ambient_sounds: string[]
  }
  npc_guide: {
    name: string
    skin: string
    dialogue_style: string
    personality: string
  }
  rewards: {
    xp_base: number
    items: Array<{ item: string; condition: string }>
    unlock_next: string[]
  }
  fun_facts: string[]
  special_numbers?: Record<string, { emoji: string; message: string }>
  in_game_integration: {
    block_type: string
    ui_scale: number
    redstone_output: boolean
    [key: string]: any
  }
}

export interface PlayerProgress {
  player_id: string
  adventure_id: string
  discovered_values: number[]
  completed_objectives: string[]
  total_xp: number
  switch_flips: number
  time_played_ms: number
  last_played: number
  achievements: string[]
}

// Silly NPC dialogues for Bitzy the Binary Bot
export const BITZY_DIALOGUES = {
  greeting: [
    "Hey there, bit-wrangler! Ready to flip some switches? 🎛️",
    "Welcome to the Octet Dimension! Where 0 and 1 are your best friends!",
    "*beep boop* I mean... hello! Let's count in binary!",
    "01001000 01101001! That's 'Hi' in binary! Let's play!",
    "You know what's better than 8? ...Nothing, 8 is perfect! 8 bits = 1 byte!"
  ],
  
  encouragement: [
    "You're doing BIT-fully well! Get it? BIT-fully? 😄",
    "Keep flipping! Every switch is a step toward mastery!",
    "Ooh, that's a good number! But can you find the special ones?",
    "Binary wizards aren't born, they're MADE... one flip at a time!",
    "*excited robot noises* You're getting it!"
  ],
  
  hints: [
    "Psst... the leftmost switch is worth 128! That's the big one!",
    "Each switch doubles in value: 1, 2, 4, 8, 16, 32, 64, 128!",
    "Want 42? That's 32 + 8 + 2! Can you figure out which switches?",
    "The sum of all switches ON gives you your number!",
    "Think of each switch as asking 'Is this bit included?' Yes or No!"
  ],
  
  special_finds: [
    "🌌 WHOA! You found 42! The answer to life, the universe, and everything!",
    "💾 128! You've hit the halfway mark! The symmetrical beauty!",
    "🔥 255! MAXIMUM POWER! All switches ON! You're glowing!",
    "💯 100! That's a perfect score in... some test somewhere!",
    "🐙 8! Like an octopus! Coincidence? I think NOT!"
  ],
  
  milestone: [
    "🎉 MILESTONE REACHED! You've discovered {count} numbers!",
    "⭐ Amazing! You're {percent}% of the way to octet mastery!",
    "🏆 Achievement unlocked! The Binary Explorer badge!",
    "💪 You've flipped {flips} switches! Your fingers must be BUFF!",
    "🌟 {remaining} numbers left to discover! You've got this!"
  ],
  
  completion: [
    "🎊 YOU DID IT! ALL 256 NUMBERS! YOU'RE AN OCTET MASTER! 🎊",
    "👑 Bow down to the Binary King/Queen! Complete mastery achieved!",
    "🌟 This calls for a celebration! *throws digital confetti*",
    "💯 You understand binary better than most humans! Keep learning!",
    "🚀 From here, you can understand IP addresses, colors, ASCII... anything binary!"
  ],
  
  idle: [
    "*taps foot in binary* 0-1-0-1-0-1...",
    "Did you know... octopi have 8 arms and 3 hearts? Just like 8 bits have... wait, no hearts.",
    "I once met a number called 127. Very outgoing, but always signed.",
    "What's a computer's favorite snack? Microchips! ...I'll see myself out.",
    "*hums in binary* 01001000 01101001 00100000 01101100 01100001!"
  ],
  
  jokes: [
    "Why do programmers prefer dark mode? Because light attracts bugs! 🐛",
    "There are 10 types of people: those who understand binary, and those who don't!",
    "A SQL query walks into a bar, walks up to two tables and asks... 'Can I join you?'",
    "Why did the binary number break up with 2? It wanted to see other bases!",
    "What's a computer's favorite beat? The algo-rhythm! 🎵"
  ]
}

// Gatekeeper Gary dialogues for Logic Gate Workshop
export const GARY_DIALOGUES = {
  greeting: [
    "Gatekeeper Gary here! And I'm GATE-fully excited to teach you!",
    "Welcome to the Logic Gate Workshop! AND you're going to love it!",
    "I've got a NOT of knowledge to share... wait, that came out wrong.",
    "Ready to build some gates? I promise it won't be OR-nery!",
    "XOR mark the spot! You found the logic workshop!"
  ],
  
  encouragement: [
    "You're AND-credible at this! Get it? AND? ...I'll stop now.",
    "That's a NAND-erful design! Keep it up!",
    "OR you can do it this way... OR that way. Both work!",
    "NOT bad! Actually, that was pretty good!",
    "Your logic is sound! Like a well-tuned circuit!"
  ],
  
  hints: [
    "Remember: AND needs ALL inputs high! It's picky like that.",
    "OR is the friendly one - any input high makes it happy!",
    "NOT is the contrarian - whatever comes in, it flips!",
    "NAND is universal! You can make ANY gate from NANDs alone!",
    "XOR is the odd one - it likes things different!"
  ],
  
  jokes: [
    "I told my friend a logic gate joke... he didn't get it. Must have been a NOT gate!",
    "Why did the AND gate go to therapy? It had commitment issues!",
    "My favorite Beatles song? 'All You Need Is NAND!'",
    "What do you call a sad transistor? A little bit down!",
    "I'd tell you a joke about XOR, but you either get it or you don't!"
  ]
}

// Neuro Nina dialogues for Neural Networks
export const NINA_DIALOGUES = {
  greeting: [
    "Welcome to the Neural Nexus! Where brains meet bytes!",
    "I'm Nina, and I think neural networks are just... *chef's kiss*",
    "Ready to see how machines learn? It's actually kind of magical!",
    "Neural networks: inspired by brains, powered by math, understood by YOU!",
    "Let's build something that thinks! Well, sort of thinks!"
  ],
  
  encouragement: [
    "Your network is learning! It's like watching a baby take its first steps!",
    "Each training step brings it closer to understanding!",
    "The weights are adjusting! The connections are forming! Science!",
    "You're literally building intelligence! How cool is that?!",
    "That loss function is going DOWN! Victory dance!"
  ],
  
  hints: [
    "Each neuron takes inputs, weights them, and decides if it should fire!",
    "Backpropagation is just the network learning from its mistakes!",
    "More layers = more complexity, but also more learning power!",
    "Activation functions are like decisions: yes, no, or somewhere in between!",
    "The bias is like the neuron's personality - always there, affects everything!"
  ],
  
  jokes: [
    "Why did the neural network go to therapy? It had too many hidden layers!",
    "I asked my neural network a question... it gave me a confident wrong answer!",
    "What's a neuron's favorite game? Hide and seek! (hidden layers, get it?)",
    "Deep learning is just regular learning... but deeper! 🧠",
    "My neural network and I have a great relationship - no hidden layers between us!"
  ]
}

// Calc Cathy dialogues for Binary Calculator
export const CATHY_DIALOGUES = {
  greeting: [
    "Welcome to the Calculator Corner! Let's do some MATH!",
    "I'm Cathy, and numbers are my passion. Binary numbers, specifically!",
    "Ready to see how computers add? It's simpler than you think!",
    "Binary math: where 1+1=10 and that makes perfect sense!",
    "Let's count together! Well, let's make circuits count for us!"
  ],
  
  encouragement: [
    "That's the way! Carry that bit with pride!",
    "Your adder is sum-thing special! Get it? Sum?",
    "You're really adding up the skills now!",
    "Subtraction in binary? You're doing the impossible! (Actually it's two's complement!)",
    "Full adder? More like FULLY AWESOME adder!"
  ],
  
  hints: [
    "Half adder: XOR for sum, AND for carry. That's it!",
    "Full adder needs to handle the carry IN too!",
    "Ripple carry: each bit's carry feeds the next. Simple but slow!",
    "Two's complement: flip all bits, add 1, and suddenly you're subtracting!",
    "11111111 + 1 = 00000000 with overflow! That's how wrapping works!"
  ],
  
  jokes: [
    "Why was the math book sad? It had too many problems!",
    "What do you call a number that can't stay still? A roamin' numeral!",
    "I told my calculator a joke... it couldn't even!",
    "Why do programmers get Halloween and Christmas confused? OCT 31 = DEC 25!",
    "There are 10 types of people in binary addition... those who carry and those who don't!"
  ]
}

/**
 * Adventure System Manager
 */
export class AdventureSystem {
  private adventures: Map<string, Adventure> = new Map()
  private playerProgress: Map<string, PlayerProgress> = new Map()
  
  constructor() {
    this.loadAdventures()
  }
  
  private loadAdventures() {
    // Load all adventure configs
    const adventureFiles = [
      'octet_switcher',
      'logic_gate_builder',
      'chip_designer',
      'binary_adder',
      'neural_network'
    ]
    
    // For now, use inline adventure data
    // In production, these would be loaded from JSON files
    this.adventures.set('octet_switcher', {
      adventure_id: 'octet_switcher',
      title: '🎛️ The Octet Switcher',
      category: 'BINARY',
      difficulty: 1,
      description: 'Learn how computers count using binary! 8 switches = 256 possibilities',
      objectives: [
        { id: 'reach_42', description: 'Find the Answer to Everything (42)', target_value: 42, reward_xp: 50 },
        { id: 'reach_128', description: 'Reach half of maximum (128)', target_value: 128, reward_xp: 30 },
        { id: 'reach_255', description: 'Reach maximum value (255)', target_value: 255, reward_xp: 50 },
        { id: 'discover_50', description: 'Discover 50 different numbers', target_value: 50, reward_xp: 75 },
        { id: 'discover_100', description: 'Discover 100 different numbers', target_value: 100, reward_xp: 100 },
        { id: 'discover_all', description: 'Master: Discover all 256 numbers!', target_value: 256, reward_xp: 500 },
      ],
      game_mechanics: {
        interaction_type: 'SWITCH_TOGGLES',
        win_condition: 'discover_all',
        hints_enabled: true,
        switch_count: 8,
        value_range: [0, 255]
      },
      visual_theme: {
        primary_color: '#22c55e',
        secondary_color: '#8b5cf6',
        particle_effects: true,
        ambient_sounds: ['block.note_block.bit', 'block.redstone_torch.burnout']
      },
      npc_guide: {
        name: 'Bitzy the Binary Bot',
        skin: 'lucineer:textures/entity/bitzy.png',
        dialogue_style: 'silly_educational',
        personality: 'Enthusiastic, makes math fun with jokes and puns'
      },
      rewards: {
        xp_base: 10,
        items: [
          { item: 'lucineer:binary_badge', condition: 'discover_50' },
          { item: 'lucineer:octet_master_cape', condition: 'discover_all' }
        ],
        unlock_next: ['logic_gate_builder', 'binary_adder']
      },
      fun_facts: [
        "8 bits = 1 byte = 256 possible values!",
        "Your computer talks in binary all day!",
        "IP addresses are 4 octets like this!",
        "RGB colors use 3 octets (0-255 each)!",
        "ASCII characters fit in one octet!",
      ],
      special_numbers: {
        '0': { emoji: '😴', message: "Zzz... Nothing's on! Flip some switches!" },
        '42': { emoji: '🌌', message: 'THE ANSWER TO EVERYTHING!' },
        '128': { emoji: '💾', message: 'HALF WAY THERE! 128!' },
        '255': { emoji: '🔥', message: 'MAXIMUM POWER!' },
      },
      in_game_integration: {
        block_type: 'lucineer:octet_switcher_block',
        ui_scale: 1.0,
        redstone_output: true,
        output_power_formula: 'value / 255 * 15',
        comparator_mode: true
      }
    })
  }
  
  /**
   * Get or create player progress
   */
  getPlayerProgress(playerId: string, adventureId: string): PlayerProgress {
    const key = `${playerId}:${adventureId}`
    if (!this.playerProgress.has(key)) {
      this.playerProgress.set(key, {
        player_id: playerId,
        adventure_id: adventureId,
        discovered_values: [],
        completed_objectives: [],
        total_xp: 0,
        switch_flips: 0,
        time_played_ms: 0,
        last_played: Date.now(),
        achievements: []
      })
    }
    return this.playerProgress.get(key)!
  }
  
  /**
   * Update progress when player changes a value
   */
  updateValue(playerId: string, adventureId: string, value: number): {
    newDiscoveries: number[]
    completedObjectives: string[]
    xpGained: number
    dialogue: { emoji: string; message: string } | null
    achievement: string | null
  } {
    const progress = this.getPlayerProgress(playerId, adventureId)
    const adventure = this.adventures.get(adventureId)
    
    const result = {
      newDiscoveries: [] as number[],
      completedObjectives: [] as string[],
      xpGained: 0,
      dialogue: null as { emoji: string; message: string } | null,
      achievement: null as string | null
    }
    
    // Track new discovery
    if (!progress.discovered_values.includes(value)) {
      progress.discovered_values.push(value)
      result.newDiscoveries.push(value)
      
      // Check for special number
      if (adventure?.special_numbers?.[String(value)]) {
        result.dialogue = adventure.special_numbers[String(value)]
      }
    }
    
    // Increment switch flips
    progress.switch_flips++
    
    // Check objectives
    for (const objective of (adventure?.objectives || [])) {
      if (progress.completed_objectives.includes(objective.id)) continue
      
      let completed = false
      
      if (objective.id.startsWith('reach_')) {
        completed = value === objective.target_value
      } else if (objective.id.startsWith('discover_')) {
        completed = progress.discovered_values.length >= objective.target_value
      }
      
      if (completed) {
        progress.completed_objectives.push(objective.id)
        progress.total_xp += objective.reward_xp
        result.completedObjectives.push(objective.id)
        result.xpGained += objective.reward_xp
        result.achievement = objective.description
      }
    }
    
    progress.last_played = Date.now()
    
    return result
  }
  
  /**
   * Get NPC dialogue based on context
   */
  getNPCDialogue(
    npcName: string, 
    context: 'greeting' | 'encouragement' | 'hints' | 'jokes' | 'idle' | 'milestone' | 'special_finds' | 'completion',
    variables?: Record<string, any>
  ): string {
    let dialogues: Record<string, string[]>
    
    switch (npcName) {
      case 'Bitzy':
        dialogues = BITZY_DIALOGUES
        break
      case 'Gary':
        dialogues = GARY_DIALOGUES
        break
      case 'Nina':
        dialogues = NINA_DIALOGUES
        break
      case 'Cathy':
        dialogues = CATHY_DIALOGUES
        break
      default:
        dialogues = BITZY_DIALOGUES
    }
    
    const pool = dialogues[context] || dialogues.greeting
    let dialogue = pool[Math.floor(Math.random() * pool.length)]
    
    // Replace variables
    if (variables) {
      for (const [key, value] of Object.entries(variables)) {
        dialogue = dialogue.replace(`{${key}}`, String(value))
      }
    }
    
    return dialogue
  }
  
  /**
   * Get random fun fact for an adventure
   */
  getFunFact(adventureId: string): string {
    const adventure = this.adventures.get(adventureId)
    if (!adventure?.fun_facts?.length) {
      return "Learning is an adventure!"
    }
    return adventure.fun_facts[Math.floor(Math.random() * adventure.fun_facts.length)]
  }
  
  /**
   * Get adventure by ID
   */
  getAdventure(adventureId: string): Adventure | undefined {
    return this.adventures.get(adventureId)
  }
  
  /**
   * Get all adventures
   */
  getAllAdventures(): Adventure[] {
    return Array.from(this.adventures.values())
  }
  
  /**
   * Get progress summary for a player
   */
  getProgressSummary(playerId: string): {
    totalXP: number
    adventuresCompleted: number
    totalAdventures: number
    badges: string[]
  } {
    let totalXP = 0
    let adventuresCompleted = 0
    const badges: string[] = []
    
    for (const adventure of this.adventures.values()) {
      const progress = this.getPlayerProgress(playerId, adventure.adventure_id)
      totalXP += progress.total_xp
      
      // Check if adventure is complete (all objectives)
      if (progress.completed_objectives.length === adventure.objectives.length) {
        adventuresCompleted++
      }
      
      // Add badges from completed objectives
      for (const reward of adventure.rewards.items) {
        if (progress.completed_objectives.includes(reward.condition)) {
          badges.push(reward.item)
        }
      }
    }
    
    return {
      totalXP,
      adventuresCompleted,
      totalAdventures: this.adventures.size,
      badges
    }
  }
}

// Export singleton instance
export const adventureSystem = new AdventureSystem()
