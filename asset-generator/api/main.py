"""
Asset Generator API
===================
FastAPI endpoints for the asset generator.
Provides endpoints for text generation, image generation, vibe-coding, and learning.
"""

from fastapi import FastAPI, HTTPException, Depends, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Dict, List, Optional, Any, Union
from enum import Enum
import asyncio
import time
import json
from datetime import datetime

# Import components
from api.providers import CascadeRouter, GenerationRequest, TaskComplexity
from llm.providers import DeepSeekProvider, GroqProvider, LocalLLMProvider, OpenAIProvider
from image.providers import DeepInfraImageProvider, LocalStableDiffusionProvider, ImageGenerationRequest, ImageStyle, ImageSize
from vibe.engine import VibeInterpreter, VibeRequest, VibeContext, ProgressiveLearningEngine
from config.settings import get_config, AssetGeneratorConfig


# ============================================================================
# Pydantic Models for API
# ============================================================================

class TaskTypeEnum(str, Enum):
    TEXT = "text"
    SCRIPT = "script"
    NPC = "npc"
    LESSON = "lesson"
    STRUCTURE = "structure"
    IMAGE_PROMPT = "image_prompt"


class ComplexityEnum(str, Enum):
    TRIVIAL = "trivial"
    SIMPLE = "simple"
    MODERATE = "moderate"
    COMPLEX = "complex"
    CREATIVE = "creative"


class GenerateRequest(BaseModel):
    """Request for text generation."""
    prompt: str = Field(..., description="The prompt to generate from")
    task_type: TaskTypeEnum = Field(default=TaskTypeEnum.TEXT)
    complexity: ComplexityEnum = Field(default=ComplexityEnum.MODERATE)
    context: Dict[str, Any] = Field(default_factory=dict)
    player_id: Optional[str] = None
    temperature: float = Field(default=0.7, ge=0.0, le=2.0)
    max_tokens: int = Field(default=2048, ge=1, le=32000)
    prefer_local: bool = Field(default=False)


class GenerateResponse(BaseModel):
    """Response from text generation."""
    content: str
    content_type: str
    provider: str
    model: str
    latency_ms: float
    tokens_used: int = 0
    cost_estimate: float = 0.0
    cached: bool = False
    quality_score: float = 0.0


class ImageStyleEnum(str, Enum):
    PIXEL_ART = "pixel_art"
    REALISTIC = "realistic"
    CARTOON = "cartoon"
    ANIME = "anime"
    FANTASY = "fantasy"
    SCI_FI = "sci_fi"
    NATURAL = "natural"
    INDUSTRIAL = "industrial"


class ImageSizeEnum(str, Enum):
    SIZE_16 = "16x16"
    SIZE_32 = "32x32"
    SIZE_64 = "64x64"
    SIZE_128 = "128x128"
    SIZE_256 = "256x256"
    SIZE_512 = "512x512"
    SIZE_1024 = "1024x1024"


class GenerateImageRequest(BaseModel):
    """Request for image generation."""
    prompt: str = Field(..., description="Image description")
    style: ImageStyleEnum = Field(default=ImageStyleEnum.PIXEL_ART)
    size: ImageSizeEnum = Field(default=ImageSizeEnum.SIZE_64)
    negative_prompt: str = Field(default="")
    steps: int = Field(default=30, ge=1, le=150)
    guidance_scale: float = Field(default=7.5, ge=1.0, le=20.0)
    seed: Optional[int] = None
    tileable: bool = Field(default=False)
    transparent_background: bool = Field(default=False)
    optimize_for_minecraft: bool = Field(default=True)


class GenerateImageResponse(BaseModel):
    """Response from image generation."""
    image_base64: str
    width: int
    height: int
    format: str
    provider: str
    model: str
    latency_ms: float
    seed_used: int
    minecraft_optimized: bool = False


class VibeGenerateRequest(BaseModel):
    """Request for vibe-coding generation."""
    natural_language: str = Field(..., description="Player's natural language request")
    player_id: str
    player_name: str
    world_id: str
    position: List[float] = Field(default=[0.0, 64.0, 0.0])
    dimension: str = Field(default="overworld")
    biome: str = Field(default="plains")
    inventory: List[Dict[str, Any]] = Field(default_factory=list)
    nearby_blocks: List[Dict[str, Any]] = Field(default_factory=list)
    nearby_entities: List[Dict[str, Any]] = Field(default_factory=list)
    time_of_day: int = Field(default=0)
    weather: str = Field(default="clear")
    recent_actions: List[str] = Field(default_factory=list)


class VibeGenerateResponse(BaseModel):
    """Response from vibe-coding generation."""
    success: bool
    message: str
    actions: List[Dict[str, Any]] = []
    generated_code: Optional[str] = None
    suggestions: List[str] = []
    learning_opportunities: List[str] = []
    confidence: float = 0.0
    model_used: str = ""
    latency_ms: float = 0.0


class LessonRequest(BaseModel):
    """Request for progressive lesson."""
    player_id: str
    topic: Optional[str] = None
    force_difficulty: Optional[int] = None


class LessonResponse(BaseModel):
    """Response with generated lesson."""
    title: str
    topic: str
    difficulty: int
    concepts: List[str]
    steps: List[str]
    challenge: str
    reward: str
    next_topic: Optional[str]


class ProviderStatus(BaseModel):
    """Status of a provider."""
    name: str
    healthy: bool
    tier: str
    priority: int
    request_count: int
    error_count: int
    avg_latency_ms: float


class HealthResponse(BaseModel):
    """Health check response."""
    status: str
    providers: List[ProviderStatus]
    cache_size: int
    uptime_seconds: float


# ============================================================================
# FastAPI Application
# ============================================================================

app = FastAPI(
    title="Lucineer Asset Generator",
    description="Multi-provider asset generation for Minecraft mods with vibe-coding and progressive learning",
    version="1.0.0"
)

# CORS for Minecraft mod access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global state
_start_time = time.time()
_router: Optional[CascadeRouter] = None
_vibe_interpreter: Optional[VibeInterpreter] = None
_learning_engine: Optional[ProgressiveLearningEngine] = None
_image_providers: Dict[str, Any] = {}


def get_router() -> CascadeRouter:
    """Get or create the cascade router."""
    global _router
    if _router is None:
        config = get_config()
        _router = CascadeRouter()
        
        # Register LLM providers
        for name, prov_config in config.llm_providers.items():
            if not prov_config.enabled or not prov_config.api_key:
                continue
                
            if name == "deepseek":
                _router.register_provider(DeepSeekProvider(prov_config.api_key, prov_config.base_url))
            elif name == "groq":
                _router.register_provider(GroqProvider(prov_config.api_key))
            elif name == "openai":
                _router.register_provider(OpenAIProvider(prov_config.api_key))
                
        # Register local provider (always available)
        local_config = config.llm_providers.get("local")
        if local_config and local_config.enabled:
            _router.register_provider(LocalLLMProvider(local_config.base_url, local_config.default_model))
            
    return _router


def get_vibe_interpreter() -> VibeInterpreter:
    """Get or create the vibe interpreter."""
    global _vibe_interpreter
    if _vibe_interpreter is None:
        _vibe_interpreter = VibeInterpreter(get_router())
    return _vibe_interpreter


def get_learning_engine() -> ProgressiveLearningEngine:
    """Get or create the learning engine."""
    global _learning_engine
    if _learning_engine is None:
        _learning_engine = ProgressiveLearningEngine(get_router())
    return _learning_engine


# ============================================================================
# API Endpoints
# ============================================================================

@app.get("/", response_model=Dict[str, str])
async def root():
    """Root endpoint."""
    return {
        "name": "Lucineer Asset Generator",
        "version": "1.0.0",
        "status": "operational",
        "endpoints": ["/generate", "/generate-image", "/vibe", "/lesson", "/health"]
    }


@app.post("/generate", response_model=GenerateResponse)
async def generate_text(request: GenerateRequest):
    """Generate text using the best available LLM."""
    router = get_router()
    
    complexity_map = {
        ComplexityEnum.TRIVIAL: TaskComplexity.TRIVIAL,
        ComplexityEnum.SIMPLE: TaskComplexity.SIMPLE,
        ComplexityEnum.MODERATE: TaskComplexity.MODERATE,
        ComplexityEnum.COMPLEX: TaskComplexity.COMPLEX,
        ComplexityEnum.CREATIVE: TaskComplexity.CREATIVE,
    }
    
    gen_request = GenerationRequest(
        prompt=request.prompt,
        task_type=request.task_type.value,
        complexity=complexity_map[request.complexity],
        context=request.context,
        player_id=request.player_id,
        temperature=request.temperature,
        max_tokens=request.max_tokens,
        prefer_local=request.prefer_local
    )
    
    try:
        response = await router.route(gen_request)
        return GenerateResponse(
            content=str(response.content),
            content_type=response.content_type,
            provider=response.provider,
            model=response.model,
            latency_ms=response.latency_ms,
            tokens_used=response.tokens_used,
            cost_estimate=response.cost_estimate,
            cached=response.cached,
            quality_score=response.quality_score
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/generate-image", response_model=GenerateImageResponse)
async def generate_image(request: GenerateImageRequest):
    """Generate an image for Minecraft assets."""
    config = get_config()
    
    # Select provider
    provider = None
    for name, prov_config in sorted(config.image_providers.items(), key=lambda x: x[1].priority, reverse=True):
        if prov_config.enabled:
            if name == "local_sd":
                provider = LocalStableDiffusionProvider(prov_config.base_url)
                if await provider.health_check():
                    break
                provider = None
            elif name == "deepinfra" and prov_config.api_key:
                provider = DeepInfraImageProvider(prov_config.api_key)
                break
                
    if not provider:
        raise HTTPException(status_code=503, detail="No image generation provider available")
    
    # Map size enum
    size_map = {
        ImageSizeEnum.SIZE_16: ImageSize.TILE_16,
        ImageSizeEnum.SIZE_32: ImageSize.TILE_32,
        ImageSizeEnum.SIZE_64: ImageSize.TILE_64,
        ImageSizeEnum.SIZE_128: ImageSize.TILE_128,
        ImageSizeEnum.SIZE_256: ImageSize.TILE_256,
        ImageSizeEnum.SIZE_512: ImageSize.TILE_512,
        ImageSizeEnum.SIZE_1024: ImageSize.TILE_1024,
    }
    
    style_map = {
        ImageStyleEnum.PIXEL_ART: ImageStyle.PIXEL_ART,
        ImageStyleEnum.REALISTIC: ImageStyle.REALISTIC,
        ImageStyleEnum.CARTOON: ImageStyle.CARTOON,
        ImageStyleEnum.ANIME: ImageStyle.ANIME,
        ImageStyleEnum.FANTASY: ImageStyle.FANTASY,
        ImageStyleEnum.SCI_FI: ImageStyle.SCI_FI,
        ImageStyleEnum.NATURAL: ImageStyle.NATURAL,
        ImageStyleEnum.INDUSTRIAL: ImageStyle.INDUSTRIAL,
    }
    
    img_request = ImageGenerationRequest(
        prompt=request.prompt,
        size=size_map[request.size],
        style=style_map[request.style],
        negative_prompt=request.negative_prompt,
        steps=request.steps,
        guidance_scale=request.guidance_scale,
        seed=request.seed,
        tileable=request.tileable,
        transparent_background=request.transparent_background
    )
    
    try:
        response = await provider.generate(img_request)
        
        # Optimize for Minecraft if requested
        minecraft_optimized = False
        if request.optimize_for_minecraft:
            from image.providers import MinecraftTextureOptimizer
            size_int = size_map[request.size].value[0]
            response.image_data = MinecraftTextureOptimizer.to_minecraft_texture(
                response.image_data, size_int
            )
            response.image_base64 = __import__('base64').b64encode(response.image_data).decode()
            minecraft_optimized = True
        
        return GenerateImageResponse(
            image_base64=response.image_base64,
            width=response.width,
            height=response.height,
            format=response.format,
            provider=response.provider,
            model=response.model,
            latency_ms=response.latency_ms,
            seed_used=response.seed_used,
            minecraft_optimized=minecraft_optimized
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/vibe", response_model=VibeGenerateResponse)
async def vibe_generate(request: VibeGenerateRequest):
    """Generate from natural language vibe-coding."""
    interpreter = get_vibe_interpreter()
    
    context = VibeContext(
        player_id=request.player_id,
        player_name=request.player_name,
        world_id=request.world_id,
        position=tuple(request.position),
        dimension=request.dimension,
        biome=request.biome,
        inventory=request.inventory,
        nearby_blocks=request.nearby_blocks,
        nearby_entities=request.nearby_entities,
        time_of_day=request.time_of_day,
        weather=request.weather,
        recent_actions=request.recent_actions
    )
    
    vibe_request = VibeRequest(
        natural_language=request.natural_language,
        context=context
    )
    
    try:
        response = await interpreter.interpret(vibe_request)
        return VibeGenerateResponse(
            success=response.success,
            message=response.message,
            actions=response.actions,
            generated_code=response.generated_code,
            suggestions=response.suggestions,
            learning_opportunities=response.learning_opportunities,
            confidence=response.confidence,
            model_used=response.model_used,
            latency_ms=response.latency_ms
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/lesson", response_model=LessonResponse)
async def generate_lesson(request: LessonRequest):
    """Generate a progressive lesson."""
    engine = get_learning_engine()
    
    try:
        lesson = await engine.generate_next_lesson(request.player_id, request.topic)
        
        if request.force_difficulty:
            lesson["difficulty"] = request.force_difficulty
            
        return LessonResponse(
            title=lesson.get("title", "Untitled Lesson"),
            topic=lesson.get("topic", request.topic or "general"),
            difficulty=lesson.get("difficulty", 1),
            concepts=lesson.get("concepts", []),
            steps=lesson.get("steps", []),
            challenge=lesson.get("challenge", ""),
            reward=lesson.get("reward", ""),
            next_topic=lesson.get("next_topic")
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/lesson/complete")
async def complete_lesson(player_id: str, topic: str, success: bool = True):
    """Record lesson completion."""
    engine = get_learning_engine()
    delta = 1 if success else 0
    engine.update_skill_level(player_id, topic, delta)
    if success:
        engine.record_interest(player_id, topic)
    return {"status": "recorded", "player_id": player_id, "topic": topic, "delta": delta}


@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Check system health."""
    router = get_router()
    
    providers = []
    for tier, prov_list in router.providers.items():
        for prov in prov_list:
            stats = prov.get_stats()
            providers.append(ProviderStatus(
                name=prov.config.name,
                healthy=await prov.health_check() if hasattr(prov, 'health_check') else True,
                tier=tier.name,
                priority=prov.config.priority,
                request_count=stats.get("requests", 0),
                error_count=stats.get("errors", 0),
                avg_latency_ms=stats.get("avg_latency_ms", 0.0)
            ))
    
    return HealthResponse(
        status="healthy" if any(p.healthy for p in providers) else "degraded",
        providers=providers,
        cache_size=len(router.cache.cache) if hasattr(router.cache, 'cache') else 0,
        uptime_seconds=time.time() - _start_time
    )


@app.post("/cache/clear")
async def clear_cache():
    """Clear the generation cache."""
    router = get_router()
    router.cache.cache.clear()
    return {"status": "cache cleared"}


# ============================================================================
# Startup/Shutdown
# ============================================================================

@app.on_event("startup")
async def startup():
    """Initialize on startup."""
    # Pre-warm the router
    get_router()
    get_vibe_interpreter()
    get_learning_engine()
    print("🚀 Lucineer Asset Generator started")


@app.on_event("shutdown")
async def shutdown():
    """Cleanup on shutdown."""
    print("🛑 Lucineer Asset Generator stopped")


# ============================================================================
# Run Server
# ============================================================================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)
