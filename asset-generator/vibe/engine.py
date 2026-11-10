"""
Vibe-Coding System
==================
In-game natural language generation with progressive learning.
Players describe what they want, system generates it intelligently.

Features:
- Natural language to Minecraft commands
- Progressive skill learning
- Context-aware generation
- Multi-step task planning
"""

import asyncio
import json
import re
from typing import Dict, List, Optional, Any, Tuple, Callable
from dataclasses import dataclass, field
from enum import Enum
from datetime import datetime
import hashlib

from api.providers import CascadeRouter, GenerationRequest, GenerationResponse, TaskComplexity
from llm.providers import DeepSeekProvider, GroqProvider, LocalLLMProvider


class VibeIntent(Enum):
    """Types of vibe-coding intents."""
    BUILD = "build"           # Create structures
    GENERATE = "generate"     # Create assets
    AUTOMATE = "automate"     # Create scripts/macros
    MODIFY = "modify"         # Change existing things
    QUERY = "query"           # Ask questions
    LEARN = "learn"           # Learning request
    EXPLORE = "explore"       # Discovery/experimentation
    COLLABORATE = "collaborate"  # Multi-player interaction


@dataclass
class VibeContext:
    """Context for vibe-coding request."""
    player_id: str
    player_name: str
    world_id: str
    position: Tuple[float, float, float]
    dimension: str
    biome: str
    inventory: List[Dict] = field(default_factory=list)
    nearby_entities: List[Dict] = field(default_factory=list)
    nearby_blocks: List[Dict] = field(default_factory=list)
    time_of_day: int = 0
    weather: str = "clear"
    game_mode: str = "survival"
    recent_actions: List[str] = field(default_factory=list)
    player_preferences: Dict[str, Any] = field(default_factory=dict)
    session_id: str = ""
    
    def to_prompt_context(self) -> str:
        """Convert to prompt-friendly context string."""
        lines = [
            f"Player: {self.player_name}",
            f"Location: ({self.position[0]:.0f}, {self.position[1]:.0f}, {self.position[2]:.0f}) in {self.dimension}",
            f"Biome: {self.biome}",
            f"Time: {self.time_of_day // 1000}:00",
            f"Weather: {self.weather}",
        ]
        
        if self.nearby_blocks:
            blocks_summary = {}
            for b in self.nearby_blocks[:20]:
                block_type = b.get("type", "unknown")
                blocks_summary[block_type] = blocks_summary.get(block_type, 0) + 1
            lines.append(f"Nearby blocks: {blocks_summary}")
            
        if self.nearby_entities:
            entities = [e.get("type", "unknown") for e in self.nearby_entities[:10]]
            lines.append(f"Nearby entities: {entities}")
            
        if self.recent_actions:
            lines.append(f"Recent actions: {', '.join(self.recent_actions[-5:])}")
            
        return "\n".join(lines)


@dataclass
class VibeRequest:
    """A vibe-coding request from a player."""
    natural_language: str
    context: VibeContext
    intent: Optional[VibeIntent] = None
    constraints: Dict[str, Any] = field(default_factory=dict)
    reference_image: Optional[str] = None  # Base64
    
    def __post_init__(self):
        if self.intent is None:
            self.intent = self._detect_intent()
    
    def _detect_intent(self) -> VibeIntent:
        """Detect intent from natural language."""
        text = self.natural_language.lower()
        
        build_keywords = ["build", "create", "make", "construct", "place", "put"]
        generate_keywords = ["generate", "design", "draw", "texture", "skin"]
        automate_keywords = ["automate", "script", "program", "repeat", "loop", "macro"]
        modify_keywords = ["change", "modify", "edit", "update", "transform"]
        query_keywords = ["what", "how", "why", "where", "when", "tell me", "explain"]
        learn_keywords = ["teach", "learn", "lesson", "tutorial", "guide", "show me"]
        explore_keywords = ["explore", "discover", "find", "search", "experiment"]
        
        for kw in build_keywords:
            if kw in text:
                return VibeIntent.BUILD
        for kw in generate_keywords:
            if kw in text:
                return VibeIntent.GENERATE
        for kw in automate_keywords:
            if kw in text:
                return VibeIntent.AUTOMATE
        for kw in modify_keywords:
            if kw in text:
                return VibeIntent.MODIFY
        for kw in query_keywords:
            if kw in text:
                return VibeIntent.QUERY
        for kw in learn_keywords:
            if kw in text:
                return VibeIntent.LEARN
        for kw in explore_keywords:
            if kw in text:
                return VibeIntent.EXPLORE
                
        return VibeIntent.BUILD  # Default


@dataclass
class VibeResponse:
    """Response to a vibe-coding request."""
    success: bool
    message: str
    actions: List[Dict] = field(default_factory=list)
    generated_code: Optional[str] = None
    generated_assets: List[Dict] = field(default_factory=list)
    suggestions: List[str] = field(default_factory=list)
    learning_opportunities: List[str] = field(default_factory=list)
    confidence: float = 0.0
    model_used: str = ""
    latency_ms: float = 0.0


class VibeInterpreter:
    """
    Interprets natural language into actionable Minecraft commands.
    Uses cascaded LLM routing for optimal quality/speed.
    """
    
    def __init__(self, router: CascadeRouter):
        self.router = router
        self.action_templates = self._load_action_templates()
        self.player_histories: Dict[str, List[Dict]] = {}
        
    async def interpret(self, request: VibeRequest) -> VibeResponse:
        """Interpret a vibe request into Minecraft actions."""
        start_time = asyncio.get_event_loop().time()
        
        # Build context-aware prompt
        system_prompt = self._build_system_prompt(request)
        user_prompt = self._build_user_prompt(request)
        
        # Route to appropriate LLM
        gen_request = GenerationRequest(
            prompt=user_prompt,
            task_type="vibe_code",
            complexity=self._estimate_complexity(request),
            context={
                "system": system_prompt,
                "history": self._get_history(request.context.player_id),
                "intent": request.intent.value
            },
            player_id=request.context.player_id,
            prefer_local=True
        )
        
        response = await self.router.route(gen_request)
        
        # Parse response into actions
        actions = self._parse_actions(response.content, request)
        
        # Update player history
        self._update_history(request.context.player_id, request, response)
        
        latency_ms = (asyncio.get_event_loop().time() - start_time) * 1000
        
        return VibeResponse(
            success=True,
            message=self._extract_message(response.content),
            actions=actions,
            generated_code=self._extract_code(response.content),
            suggestions=self._extract_suggestions(response.content),
            confidence=self._estimate_confidence(response),
            model_used=response.model,
            latency_ms=latency_ms
        )
    
    def _build_system_prompt(self, request: VibeRequest) -> str:
        """Build system prompt based on intent and context."""
        base_prompts = {
            VibeIntent.BUILD: """You are a Minecraft architect assistant. Convert player requests into structured build plans.
Output format:
{
  "interpretation": "What you understood",
  "plan": [
    {"action": "place", "block": "stone", "x": 0, "y": 0, "z": 0},
    {"action": "place", "block": "oak_planks", "x": 1, "y": 0, "z": 0}
  ],
  "estimated_blocks": 50,
  "tips": ["Start from the corner", "Use scaffolding"]
}""",
            
            VibeIntent.GENERATE: """You are a Minecraft asset designer. Generate specifications for textures, items, or structures.
Output format:
{
  "asset_type": "texture|item|structure",
  "specifications": {...},
  "style_notes": "...",
  "generation_prompt": "Stable Diffusion prompt for textures"
}""",
            
            VibeIntent.AUTOMATE: """You are a Minecraft automation expert. Create JavaScript code for the mineflayer bot.
Output clean, efficient code with comments. Include:
- Clear variable names
- Error handling
- Progress logging""",
            
            VibeIntent.LEARN: """You are a patient Minecraft tutor. Create progressive learning experiences.
Structure lessons with:
1. Concept introduction
2. Guided practice
3. Challenge
4. Real-world application""",
        }
        
        return base_prompts.get(request.intent, base_prompts[VibeIntent.BUILD])
    
    def _build_user_prompt(self, request: VibeRequest) -> str:
        """Build user prompt with context."""
        lines = [
            f"Player request: {request.natural_language}",
            "",
            "Context:",
            request.context.to_prompt_context()
        ]
        
        if request.constraints:
            lines.append("")
            lines.append("Constraints:")
            for key, value in request.constraints.items():
                lines.append(f"- {key}: {value}")
                
        return "\n".join(lines)
    
    def _estimate_complexity(self, request: VibeRequest) -> TaskComplexity:
        """Estimate task complexity for routing."""
        text = request.natural_language.lower()
        word_count = len(text.split())
        
        # Intent-based complexity
        intent_complexity = {
            VibeIntent.QUERY: TaskComplexity.SIMPLE,
            VibeIntent.BUILD: TaskComplexity.MODERATE,
            VibeIntent.GENERATE: TaskComplexity.CREATIVE,
            VibeIntent.AUTOMATE: TaskComplexity.COMPLEX,
            VibeIntent.LEARN: TaskComplexity.COMPLEX,
        }
        
        base = intent_complexity.get(request.intent, TaskComplexity.MODERATE)
        
        # Adjust for word count
        if word_count > 100:
            base = TaskComplexity(max(base.value, TaskComplexity.COMPLEX.value))
        elif word_count < 10:
            base = TaskComplexity(min(base.value, TaskComplexity.SIMPLE.value))
            
        # Adjust for multi-step requests
        multi_step_indicators = ["and then", "also", "after that", "next", "finally"]
        if any(ind in text for ind in multi_step_indicators):
            base = TaskComplexity(max(base.value, TaskComplexity.MODERATE.value))
            
        return base
    
    def _parse_actions(self, content: str, request: VibeRequest) -> List[Dict]:
        """Parse LLM response into executable actions."""
        actions = []
        
        # Try to extract JSON
        try:
            # Find JSON in response
            json_match = re.search(r'\{[\s\S]*\}', content)
            if json_match:
                data = json.loads(json_match.group())
                if "plan" in data:
                    actions = data["plan"]
                elif "actions" in data:
                    actions = data["actions"]
        except json.JSONDecodeError:
            # Fall back to action extraction heuristics
            actions = self._extract_actions_heuristic(content)
            
        return actions
    
    def _extract_actions_heuristic(self, content: str) -> List[Dict]:
        """Extract actions using pattern matching."""
        actions = []
        
        # Pattern for block placement
        place_pattern = r'place\s+(\w+)\s+at\s+\(?(-?\d+),\s*(-?\d+),\s*(-?\d+)\)?'
        for match in re.finditer(place_pattern, content, re.IGNORECASE):
            actions.append({
                "action": "place",
                "block": match.group(1),
                "x": int(match.group(2)),
                "y": int(match.group(3)),
                "z": int(match.group(4))
            })
            
        return actions
    
    def _extract_message(self, content: str) -> str:
        """Extract human-readable message."""
        # Remove JSON blocks
        message = re.sub(r'\{[\s\S]*?\}', '', content)
        # Remove code blocks
        message = re.sub(r'```[\s\S]*?```', '', message)
        return message.strip()
    
    def _extract_code(self, content: str) -> Optional[str]:
        """Extract code from response."""
        code_match = re.search(r'```(?:javascript|js)?\s*([\s\S]*?)```', content)
        if code_match:
            return code_match.group(1).strip()
        return None
    
    def _extract_suggestions(self, content: str) -> List[str]:
        """Extract suggestions from response."""
        suggestions = []
        
        # Look for suggestion patterns
        patterns = [
            r'Suggestion[s]?:?\s*(.+?)(?:\n|$)',
            r'Tip[s]?:?\s*(.+?)(?:\n|$)',
            r'Recommendation[s]?:?\s*(.+?)(?:\n|$)',
        ]
        
        for pattern in patterns:
            for match in re.finditer(pattern, content, re.IGNORECASE):
                suggestions.append(match.group(1).strip())
                
        return suggestions
    
    def _estimate_confidence(self, response: GenerationResponse) -> float:
        """Estimate confidence in the response."""
        # Base confidence on latency (faster = more confident for simple tasks)
        # And on whether JSON was properly formed
        content = str(response.content)
        
        confidence = 0.5
        
        # Boost for valid JSON
        if '{' in content and '}' in content:
            try:
                json.loads(re.search(r'\{[\s\S]*\}', content).group())
                confidence += 0.3
            except:
                pass
                
        # Boost for specific actions
        if '"action"' in content:
            confidence += 0.1
            
        return min(1.0, confidence)
    
    def _get_history(self, player_id: str) -> List[Dict]:
        """Get player's conversation history."""
        return self.player_histories.get(player_id, [])[-5:]  # Last 5 interactions
    
    def _update_history(self, player_id: str, request: VibeRequest, response: GenerationResponse):
        """Update player's conversation history."""
        if player_id not in self.player_histories:
            self.player_histories[player_id] = []
            
        self.player_histories[player_id].append({
            "timestamp": datetime.now().isoformat(),
            "request": request.natural_language,
            "intent": request.intent.value,
            "response": response.content[:500]  # Truncate
        })
        
        # Keep last 20 interactions
        self.player_histories[player_id] = self.player_histories[player_id][-20:]
    
    def _load_action_templates(self) -> Dict[str, Any]:
        """Load action templates for common tasks."""
        return {
            "build_house": {
                "description": "Build a simple house",
                "template": [
                    {"action": "fill", "block": "stone", "from": [0, 0, 0], "to": [5, 3, 5]},
                    {"action": "fill", "block": "air", "from": [1, 1, 1], "to": [4, 2, 4]},
                ]
            },
            "farm_9x9": {
                "description": "Create a 9x9 farm plot",
                "template": [
                    {"action": "till", "area": [[0, 0], [8, 8]]},
                    {"action": "plant", "crop": "wheat", "area": [[1, 1], [7, 7]]},
                ]
            }
        }


class ProgressiveLearningEngine:
    """
    Endless learning engine - generates progressive content forever.
    Based on the minewright SkillGenerator pattern.
    
    Features:
    - Tracks player interests
    - Generates novel lessons
    - Adapts to skill level
    - Creates infinite progression
    """
    
    def __init__(self, router: CascadeRouter):
        self.router = router
        self.player_profiles: Dict[str, Dict] = {}
        self.lesson_history: Dict[str, List[Dict]] = {}
        self.skill_trees: Dict[str, Dict] = {}
        
    async def generate_next_lesson(self, player_id: str, topic: Optional[str] = None) -> Dict:
        """Generate the next progressive lesson for a player."""
        profile = self._get_or_create_profile(player_id)
        history = self.lesson_history.get(player_id, [])
        
        # Determine next topic based on interests and progress
        if topic is None:
            topic = self._select_next_topic(profile, history)
            
        # Calculate difficulty
        difficulty = self._calculate_difficulty(profile, history, topic)
        
        # Generate lesson
        lesson = await self._generate_lesson(topic, difficulty, profile, history)
        
        # Track lesson
        self._track_lesson(player_id, lesson)
        
        return lesson
    
    async def _generate_lesson(self, topic: str, difficulty: int, profile: Dict, history: List[Dict]) -> Dict:
        """Generate a lesson using LLM."""
        
        system_prompt = """You are a progressive Minecraft education designer. Create engaging lessons that:
1. Build on previous knowledge
2. Introduce one new concept
3. Include hands-on practice
4. Connect to real-world applications

Output as JSON with: title, concepts[], steps[], challenge, reward, next_topic"""

        user_prompt = f"""Create a {difficulty}/10 difficulty lesson about: {topic}

Player profile:
- Interests: {profile.get('interests', [])}
- Completed lessons: {len(history)}
- Preferred learning style: {profile.get('learning_style', 'visual')}

Recent completed topics: {[h.get('topic') for h in history[-3:]]}

Make it novel and engaging - avoid repeating concepts from recent lessons."""

        request = GenerationRequest(
            prompt=user_prompt,
            task_type="lesson",
            complexity=TaskComplexity.CREATIVE,
            context={"system": system_prompt},
            temperature=0.8
        )
        
        response = await self.router.route(request)
        
        try:
            json_match = re.search(r'\{[\s\S]*\}', response.content)
            lesson = json.loads(json_match.group()) if json_match else {}
        except:
            lesson = {
                "title": f"Learning {topic}",
                "concepts": [topic],
                "steps": [response.content],
                "challenge": "Practice what you learned!",
                "reward": "XP + Knowledge",
                "next_topic": None
            }
            
        lesson["difficulty"] = difficulty
        lesson["topic"] = topic
        return lesson
    
    def _get_or_create_profile(self, player_id: str) -> Dict:
        """Get or create player learning profile."""
        if player_id not in self.player_profiles:
            self.player_profiles[player_id] = {
                "interests": [],
                "skill_levels": {},
                "learning_style": "visual",
                "session_count": 0,
                "total_xp": 0,
                "achievements": []
            }
        return self.player_profiles[player_id]
    
    def _select_next_topic(self, profile: Dict, history: List[Dict]) -> str:
        """Select next topic based on interests and progress."""
        # Get topics from interests
        interests = profile.get("interests", ["building", "redstone", "farming"])
        
        # Get recently covered topics
        recent_topics = {h.get("topic") for h in history[-10:]}
        
        # Filter out recent topics
        available = [i for i in interests if i not in recent_topics]
        
        if not available:
            # Generate novel topic based on progression
            base_topics = ["mining", "building", "farming", "redstone", "combat", "exploration", "enchanting", "brewing", "automation"]
            advanced_topics = ["farming_designs", "redstone_computers", "mob_farms", "storage_systems", "transportation", "trading_halls"]
            expert_topics = ["sculk_farms", "raid_farms", "iron_farms", "gold_farms", "wither_skeleton_farms"]
            
            # Progress through tiers
            completed = len(history)
            if completed < 10:
                pool = base_topics
            elif completed < 30:
                pool = base_topics + advanced_topics
            else:
                pool = base_topics + advanced_topics + expert_topics
                
            available = [t for t in pool if t not in recent_topics]
            
        return available[0] if available else "building"
    
    def _calculate_difficulty(self, profile: Dict, history: List[Dict], topic: str) -> int:
        """Calculate appropriate difficulty level."""
        base_difficulty = 1
        
        # Adjust for total lessons completed
        base_difficulty += len(history) // 5
        
        # Adjust for topic familiarity
        topic_lessons = [h for h in history if h.get("topic") == topic]
        base_difficulty += len(topic_lessons)
        
        # Adjust for skill level
        skill_level = profile.get("skill_levels", {}).get(topic, 1)
        base_difficulty += skill_level - 1
        
        return min(10, max(1, base_difficulty))
    
    def _track_lesson(self, player_id: str, lesson: Dict):
        """Track completed lesson."""
        if player_id not in self.lesson_history:
            self.lesson_history[player_id] = []
            
        self.lesson_history[player_id].append({
            "topic": lesson.get("topic"),
            "title": lesson.get("title"),
            "difficulty": lesson.get("difficulty"),
            "timestamp": datetime.now().isoformat()
        })
    
    def record_interest(self, player_id: str, topic: str):
        """Record player interest for better recommendations."""
        profile = self._get_or_create_profile(player_id)
        
        if topic not in profile["interests"]:
            profile["interests"].append(topic)
        else:
            # Move to front (higher priority)
            profile["interests"].remove(topic)
            profile["interests"].insert(0, topic)
            
    def update_skill_level(self, player_id: str, topic: str, delta: int = 1):
        """Update skill level after completing lesson."""
        profile = self._get_or_create_profile(player_id)
        
        current = profile["skill_levels"].get(topic, 1)
        profile["skill_levels"][topic] = current + delta
        profile["total_xp"] += delta * 100
