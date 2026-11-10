"""
Asset Generator - Core Module
==============================
Multi-provider asset generation system for Minecraft mods.
Supports vibe-coding, progressive learning, and endless content generation.

Features:
- LLM abstraction (DeepSeek, Groq, OpenAI, local models)
- Image generation (DeepInfra, Stable Diffusion, local)
- Vibe-coding for in-game generation
- Progressive learning engine
"""

from dataclasses import dataclass, field
from typing import Dict, List, Optional, Any, Callable, Union
from enum import Enum
from abc import ABC, abstractmethod
import asyncio
import json
import hashlib
import time
from datetime import datetime
from pathlib import Path


class ProviderTier(Enum):
    """Provider capability tiers for intelligent routing."""
    LOCAL_FREE = 0      # Local models, no cost, lower quality
    CLOUD_FAST = 1      # Fast cloud APIs (Groq, Flash)
    CLOUD_BALANCED = 2  # Balanced quality/speed (DeepSeek, GPT-4o-mini)
    CLOUD_PREMIUM = 3   # Best quality (GPT-4, Claude)


class TaskComplexity(Enum):
    """Task complexity levels for routing decisions."""
    TRIVIAL = "trivial"      # Simple lookups, basic responses
    SIMPLE = "simple"        # Single-step reasoning
    MODERATE = "moderate"    # Multi-step with context
    COMPLEX = "complex"      # Chain-of-thought required
    CREATIVE = "creative"    # Novel generation, artistic


@dataclass
class GenerationRequest:
    """Request for asset generation."""
    prompt: str
    task_type: str  # "text", "image", "script", "structure", "npc"
    complexity: TaskComplexity = TaskComplexity.MODERATE
    context: Dict[str, Any] = field(default_factory=dict)
    player_id: Optional[str] = None
    world_id: Optional[str] = None
    temperature: float = 0.7
    max_tokens: int = 2048
    prefer_local: bool = False
    require_vision: bool = False
    image_input: Optional[str] = None  # Base64 for vision tasks
    metadata: Dict[str, Any] = field(default_factory=dict)


@dataclass
class GenerationResponse:
    """Response from asset generation."""
    content: Any  # Text, image data, JSON, etc.
    content_type: str
    provider: str
    model: str
    latency_ms: float
    tokens_used: int = 0
    cost_estimate: float = 0.0
    cached: bool = False
    quality_score: float = 0.0
    metadata: Dict[str, Any] = field(default_factory=dict)


@dataclass
class ProviderConfig:
    """Configuration for a generation provider."""
    name: str
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    models: List[str] = field(default_factory=list)
    default_model: str = ""
    tier: ProviderTier = ProviderTier.CLOUD_BALANCED
    max_requests_per_minute: int = 60
    supports_vision: bool = False
    supports_streaming: bool = True
    cost_per_1k_tokens: float = 0.0
    enabled: bool = True
    priority: int = 0  # Higher = preferred


class BaseProvider(ABC):
    """Abstract base class for generation providers."""
    
    def __init__(self, config: ProviderConfig):
        self.config = config
        self.request_count = 0
        self.error_count = 0
        self.total_latency_ms = 0.0
        
    @abstractmethod
    async def generate(self, request: GenerationRequest) -> GenerationResponse:
        """Generate content from a request."""
        pass
    
    @abstractmethod
    async def health_check(self) -> bool:
        """Check if the provider is healthy."""
        pass
    
    def get_stats(self) -> Dict[str, Any]:
        """Get provider statistics."""
        return {
            "name": self.config.name,
            "requests": self.request_count,
            "errors": self.error_count,
            "avg_latency_ms": self.total_latency_ms / max(1, self.request_count),
            "error_rate": self.error_count / max(1, self.request_count)
        }


class CascadeRouter:
    """
    Intelligent multi-tier routing with cascade fallback.
    
    Routing Strategy:
    1. Analyze task complexity
    2. Select appropriate tier
    3. Try providers in priority order
    4. Fall back on failure
    
    Based on minewright's SmartCascadeRouter pattern.
    """
    
    def __init__(self):
        self.providers: Dict[ProviderTier, List[BaseProvider]] = {
            tier: [] for tier in ProviderTier
        }
        self.complexity_analyzer = ComplexityAnalyzer()
        self.failure_tracker = FailureTracker()
        self.cache = GenerationCache()
        
    def register_provider(self, provider: BaseProvider):
        """Register a provider with the router."""
        tier = provider.config.tier
        self.providers[tier].append(provider)
        # Sort by priority within tier
        self.providers[tier].sort(key=lambda p: p.config.priority, reverse=True)
        
    async def route(self, request: GenerationRequest) -> GenerationResponse:
        """Route a request to the best available provider."""
        
        # Check cache first
        cache_key = self._make_cache_key(request)
        cached = await self.cache.get(cache_key)
        if cached:
            cached.cached = True
            return cached
            
        # Analyze complexity if not specified
        if request.complexity == TaskComplexity.MODERATE:
            request.complexity = self.complexity_analyzer.analyze(request.prompt)
            
        # Determine appropriate tier
        tier = self._select_tier(request)
        
        # Try providers in order
        errors = []
        for provider in self.providers[tier]:
            if self.failure_tracker.should_skip(provider.config.name):
                continue
                
            try:
                response = await provider.generate(request)
                self.failure_tracker.record_success(provider.config.name)
                await self.cache.set(cache_key, response)
                return response
            except Exception as e:
                self.failure_tracker.record_failure(provider.config.name)
                errors.append((provider.config.name, str(e)))
                
        # Fall back to lower tiers
        for fallback_tier in reversed(list(ProviderTier)):
            if fallback_tier.value >= tier.value:
                continue
            for provider in self.providers[fallback_tier]:
                if self.failure_tracker.should_skip(provider.config.name):
                    continue
                try:
                    response = await provider.generate(request)
                    self.failure_tracker.record_success(provider.config.name)
                    await self.cache.set(cache_key, response)
                    return response
                except Exception as e:
                    errors.append((provider.config.name, str(e)))
                    
        raise RuntimeError(f"All providers failed: {errors}")
    
    def _select_tier(self, request: GenerationRequest) -> ProviderTier:
        """Select appropriate tier based on request."""
        if request.prefer_local:
            return ProviderTier.LOCAL_FREE
            
        complexity_tier = {
            TaskComplexity.TRIVIAL: ProviderTier.LOCAL_FREE,
            TaskComplexity.SIMPLE: ProviderTier.CLOUD_FAST,
            TaskComplexity.MODERATE: ProviderTier.CLOUD_BALANCED,
            TaskComplexity.COMPLEX: ProviderTier.CLOUD_PREMIUM,
            TaskComplexity.CREATIVE: ProviderTier.CLOUD_PREMIUM,
        }
        
        return complexity_tier.get(request.complexity, ProviderTier.CLOUD_BALANCED)
    
    def _make_cache_key(self, request: GenerationRequest) -> str:
        """Generate cache key from request."""
        content = f"{request.prompt}:{request.task_type}:{request.complexity.value}"
        return hashlib.sha256(content.encode()).hexdigest()


class ComplexityAnalyzer:
    """Analyzes prompt complexity for routing decisions."""
    
    TRIVIAL_KEYWORDS = ["hello", "yes", "no", "simple", "basic", "quick"]
    COMPLEX_KEYWORDS = ["analyze", "design", "create", "explain", "reason", "chain of thought"]
    CREATIVE_KEYWORDS = ["imagine", "invent", "novel", "unique", "artistic", "creative"]
    
    def analyze(self, prompt: str) -> TaskComplexity:
        """Analyze prompt complexity."""
        prompt_lower = prompt.lower()
        
        # Check length
        word_count = len(prompt.split())
        
        # Check keywords
        trivial_score = sum(1 for kw in self.TRIVIAL_KEYWORDS if kw in prompt_lower)
        complex_score = sum(1 for kw in self.COMPLEX_KEYWORDS if kw in prompt_lower)
        creative_score = sum(1 for kw in self.CREATIVE_KEYWORDS if kw in prompt_lower)
        
        # Determine complexity
        if creative_score > 0:
            return TaskComplexity.CREATIVE
        if complex_score >= 2 or word_count > 100:
            return TaskComplexity.COMPLEX
        if complex_score > 0 or word_count > 30:
            return TaskComplexity.MODERATE
        if trivial_score > 0 or word_count < 10:
            return TaskComplexity.TRIVIAL
        return TaskComplexity.SIMPLE


class FailureTracker:
    """Tracks provider failures for circuit breaker pattern."""
    
    def __init__(self, threshold: int = 3, cooldown_seconds: int = 60):
        self.failures: Dict[str, int] = {}
        self.last_failure: Dict[str, float] = {}
        self.threshold = threshold
        self.cooldown = cooldown_seconds
        
    def record_failure(self, provider: str):
        """Record a provider failure."""
        self.failures[provider] = self.failures.get(provider, 0) + 1
        self.last_failure[provider] = time.time()
        
    def record_success(self, provider: str):
        """Record a provider success (resets failure count)."""
        self.failures[provider] = 0
        
    def should_skip(self, provider: str) -> bool:
        """Check if provider should be skipped due to failures."""
        failures = self.failures.get(provider, 0)
        if failures < self.threshold:
            return False
            
        # Check cooldown
        last = self.last_failure.get(provider, 0)
        if time.time() - last > self.cooldown:
            # Reset after cooldown
            self.failures[provider] = 0
            return False
            
        return True


class GenerationCache:
    """Simple in-memory cache for generation responses."""
    
    def __init__(self, max_size: int = 1000, ttl_seconds: int = 3600):
        self.cache: Dict[str, tuple] = {}
        self.max_size = max_size
        self.ttl = ttl_seconds
        
    async def get(self, key: str) -> Optional[GenerationResponse]:
        """Get cached response."""
        if key not in self.cache:
            return None
        response, timestamp = self.cache[key]
        if time.time() - timestamp > self.ttl:
            del self.cache[key]
            return None
        return response
        
    async def set(self, key: str, response: GenerationResponse):
        """Cache a response."""
        if len(self.cache) >= self.max_size:
            # Simple LRU: remove oldest
            oldest = min(self.cache.items(), key=lambda x: x[1][1])
            del self.cache[oldest[0]]
        self.cache[key] = (response, time.time())
