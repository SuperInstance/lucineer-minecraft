"""
LLM Providers - Multi-provider abstraction for language models.
Supports DeepSeek (logic), Groq (speed), OpenAI, local models.
"""

import asyncio
import json
import httpx
from typing import Dict, List, Optional, Any
from dataclasses import dataclass

from api.providers import (
    BaseProvider, ProviderConfig, GenerationRequest, GenerationResponse,
    ProviderTier, TaskComplexity
)


class DeepSeekProvider(BaseProvider):
    """
    DeepSeek provider - excellent for reasoning and logic tasks.
    
    Models:
    - deepseek-chat: General purpose, fast
    - deepseek-reasoner: Chain-of-thought reasoning
    
    Strengths:
    - Complex reasoning
    - Code generation
    - Mathematical tasks
    """
    
    def __init__(self, api_key: Optional[str] = None, base_url: str = "https://api.deepseek.com/v1"):
        config = ProviderConfig(
            name="deepseek",
            api_key=api_key,
            base_url=base_url,
            models=["deepseek-chat", "deepseek-reasoner"],
            default_model="deepseek-chat",
            tier=ProviderTier.CLOUD_BALANCED,
            max_requests_per_minute=60,
            supports_vision=False,
            supports_streaming=True,
            cost_per_1k_tokens=0.001,  # Very cost-effective
            priority=8
        )
        super().__init__(config)
        
    async def generate(self, request: GenerationRequest) -> GenerationResponse:
        """Generate using DeepSeek API."""
        start_time = asyncio.get_event_loop().time()
        
        # Select model based on complexity
        model = self.config.default_model
        if request.complexity in [TaskComplexity.COMPLEX, TaskComplexity.CREATIVE]:
            model = "deepseek-reasoner"
            
        headers = {
            "Authorization": f"Bearer {self.config.api_key}",
            "Content-Type": "application/json"
        }
        
        messages = self._build_messages(request)
        
        payload = {
            "model": model,
            "messages": messages,
            "temperature": request.temperature,
            "max_tokens": request.max_tokens
        }
        
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{self.config.base_url}/chat/completions",
                headers=headers,
                json=payload,
                timeout=60.0
            )
            response.raise_for_status()
            data = response.json()
            
        self.request_count += 1
        latency_ms = (asyncio.get_event_loop().time() - start_time) * 1000
        self.total_latency_ms += latency_ms
        
        content = data["choices"][0]["message"]["content"]
        tokens_used = data.get("usage", {}).get("total_tokens", 0)
        
        return GenerationResponse(
            content=content,
            content_type="text",
            provider="deepseek",
            model=model,
            latency_ms=latency_ms,
            tokens_used=tokens_used,
            cost_estimate=tokens_used * self.config.cost_per_1k_tokens / 1000
        )
    
    def _build_messages(self, request: GenerationRequest) -> List[Dict]:
        """Build message list from request."""
        messages = []
        
        # Add system context
        system_prompt = self._build_system_prompt(request)
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
            
        # Add context
        if request.context.get("history"):
            messages.extend(request.context["history"])
            
        # Add main prompt
        messages.append({"role": "user", "content": request.prompt})
        
        return messages
    
    def _build_system_prompt(self, request: GenerationRequest) -> str:
        """Build system prompt based on task type."""
        base_prompts = {
            "text": "You are a helpful AI assistant for a Minecraft mod.",
            "script": "You are a script generator for Minecraft automation. Generate clean, efficient JavaScript code.",
            "npc": "You are an NPC dialogue generator. Create engaging, character-appropriate responses.",
            "structure": "You are a Minecraft structure designer. Create detailed block-by-block blueprints.",
            "lesson": "You are an educational content creator. Design engaging, progressive learning experiences."
        }
        return base_prompts.get(request.task_type, base_prompts["text"])
    
    async def health_check(self) -> bool:
        """Check DeepSeek API health."""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.config.base_url}/models",
                    headers={"Authorization": f"Bearer {self.config.api_key}"},
                    timeout=10.0
                )
                return response.status_code == 200
        except:
            return False


class GroqProvider(BaseProvider):
    """
    Groq provider - fastest inference, ideal for real-time responses.
    
    Models:
    - llama-3.3-70b-versatile: Fast, capable
    - llama-3.1-8b-instant: Ultra-fast, simple tasks
    - mixtral-8x7b-32768: Large context
    
    Strengths:
    - Ultra-low latency (~100ms)
    - Real-time interactions
    - High throughput
    """
    
    def __init__(self, api_key: Optional[str] = None):
        config = ProviderConfig(
            name="groq",
            api_key=api_key,
            base_url="https://api.groq.com/openai/v1",
            models=["llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768"],
            default_model="llama-3.3-70b-versatile",
            tier=ProviderTier.CLOUD_FAST,
            max_requests_per_minute=30,
            supports_vision=False,
            supports_streaming=True,
            cost_per_1k_tokens=0.0,  # Has free tier
            priority=9
        )
        super().__init__(config)
        
    async def generate(self, request: GenerationRequest) -> GenerationResponse:
        """Generate using Groq API."""
        start_time = asyncio.get_event_loop().time()
        
        # Select model based on needs
        model = self._select_model(request)
        
        headers = {
            "Authorization": f"Bearer {self.config.api_key}",
            "Content-Type": "application/json"
        }
        
        messages = self._build_messages(request)
        
        payload = {
            "model": model,
            "messages": messages,
            "temperature": request.temperature,
            "max_tokens": request.max_tokens
        }
        
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{self.config.base_url}/chat/completions",
                headers=headers,
                json=payload,
                timeout=30.0
            )
            response.raise_for_status()
            data = response.json()
            
        self.request_count += 1
        latency_ms = (asyncio.get_event_loop().time() - start_time) * 1000
        self.total_latency_ms += latency_ms
        
        content = data["choices"][0]["message"]["content"]
        tokens_used = data.get("usage", {}).get("total_tokens", 0)
        
        return GenerationResponse(
            content=content,
            content_type="text",
            provider="groq",
            model=model,
            latency_ms=latency_ms,
            tokens_used=tokens_used,
            cost_estimate=0.0  # Free tier
        )
    
    def _select_model(self, request: GenerationRequest) -> str:
        """Select best model for the task."""
        if request.complexity == TaskComplexity.TRIVIAL:
            return "llama-3.1-8b-instant"
        if request.context.get("long_context"):
            return "mixtral-8x7b-32768"
        return self.config.default_model
    
    def _build_messages(self, request: GenerationRequest) -> List[Dict]:
        """Build messages for Groq (OpenAI-compatible)."""
        messages = []
        
        system_prompt = f"You are an AI assistant for Minecraft. Task: {request.task_type}"
        messages.append({"role": "system", "content": system_prompt})
        
        if request.context.get("history"):
            messages.extend(request.context["history"])
            
        messages.append({"role": "user", "content": request.prompt})
        
        return messages
    
    async def health_check(self) -> bool:
        """Check Groq API health."""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.config.base_url}/models",
                    headers={"Authorization": f"Bearer {self.config.api_key}"},
                    timeout=10.0
                )
                return response.status_code == 200
        except:
            return False


class LocalLLMProvider(BaseProvider):
    """
    Local LLM provider - uses local models via Ollama or LM Studio.
    
    Supported models:
    - Llama 3.2 (3B, 11B)
    - Mistral (7B)
    - Phi-3 (3.8B)
    - Gemma 2 (9B)
    
    Strengths:
    - Zero cost
    - Privacy
    - No rate limits
    - Offline capable
    """
    
    def __init__(self, base_url: str = "http://localhost:11434", model: str = "llama3.2"):
        config = ProviderConfig(
            name="local",
            base_url=base_url,
            models=[model],
            default_model=model,
            tier=ProviderTier.LOCAL_FREE,
            max_requests_per_minute=1000,
            supports_vision=False,
            supports_streaming=True,
            cost_per_1k_tokens=0.0,
            priority=10  # Highest priority when available
        )
        super().__init__(config)
        self._healthy = None
        
    async def generate(self, request: GenerationRequest) -> GenerationResponse:
        """Generate using local LLM (Ollama)."""
        start_time = asyncio.get_event_loop().time()
        
        # Ollama API format
        payload = {
            "model": self.config.default_model,
            "prompt": request.prompt,
            "stream": False,
            "options": {
                "temperature": request.temperature,
                "num_predict": request.max_tokens
            }
        }
        
        # Add system context
        if request.context.get("system"):
            payload["system"] = request.context["system"]
            
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{self.config.base_url}/api/generate",
                json=payload,
                timeout=120.0
            )
            response.raise_for_status()
            data = response.json()
            
        self.request_count += 1
        latency_ms = (asyncio.get_event_loop().time() - start_time) * 1000
        self.total_latency_ms += latency_ms
        
        content = data.get("response", "")
        tokens_used = data.get("eval_count", 0) + data.get("prompt_eval_count", 0)
        
        return GenerationResponse(
            content=content,
            content_type="text",
            provider="local",
            model=self.config.default_model,
            latency_ms=latency_ms,
            tokens_used=tokens_used,
            cost_estimate=0.0
        )
    
    async def health_check(self) -> bool:
        """Check if local LLM is running."""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.config.base_url}/api/tags",
                    timeout=5.0
                )
                self._healthy = response.status_code == 200
                return self._healthy
        except:
            self._healthy = False
            return False
    
    def is_healthy(self) -> bool:
        """Return cached health status."""
        return self._healthy if self._healthy is not None else False


class OpenAIProvider(BaseProvider):
    """
    OpenAI provider - premium quality option.
    
    Models:
    - gpt-4o: Multimodal, best quality
    - gpt-4o-mini: Fast, cost-effective
    - o1-preview: Advanced reasoning
    - o1-mini: Fast reasoning
    """
    
    def __init__(self, api_key: Optional[str] = None):
        config = ProviderConfig(
            name="openai",
            api_key=api_key,
            base_url="https://api.openai.com/v1",
            models=["gpt-4o", "gpt-4o-mini", "o1-preview", "o1-mini"],
            default_model="gpt-4o-mini",
            tier=ProviderTier.CLOUD_PREMIUM,
            max_requests_per_minute=500,
            supports_vision=True,
            supports_streaming=True,
            cost_per_1k_tokens=0.01,
            priority=7
        )
        super().__init__(config)
        
    async def generate(self, request: GenerationRequest) -> GenerationResponse:
        """Generate using OpenAI API."""
        start_time = asyncio.get_event_loop().time()
        
        model = self._select_model(request)
        
        headers = {
            "Authorization": f"Bearer {self.config.api_key}",
            "Content-Type": "application/json"
        }
        
        messages = self._build_messages(request)
        
        payload = {
            "model": model,
            "messages": messages,
            "temperature": request.temperature if not model.startswith("o1") else 1.0,
            "max_tokens": request.max_tokens
        }
        
        # Add image for vision tasks
        if request.require_vision and request.image_input:
            messages[-1]["content"] = [
                {"type": "text", "text": request.prompt},
                {"type": "image_url", "image_url": {"url": f"data:image/png;base64,{request.image_input}"}}
            ]
            
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{self.config.base_url}/chat/completions",
                headers=headers,
                json=payload,
                timeout=60.0
            )
            response.raise_for_status()
            data = response.json()
            
        self.request_count += 1
        latency_ms = (asyncio.get_event_loop().time() - start_time) * 1000
        self.total_latency_ms += latency_ms
        
        content = data["choices"][0]["message"]["content"]
        tokens_used = data.get("usage", {}).get("total_tokens", 0)
        
        return GenerationResponse(
            content=content,
            content_type="text",
            provider="openai",
            model=model,
            latency_ms=latency_ms,
            tokens_used=tokens_used,
            cost_estimate=tokens_used * self.config.cost_per_1k_tokens / 1000
        )
    
    def _select_model(self, request: GenerationRequest) -> str:
        """Select model based on request."""
        if request.require_vision:
            return "gpt-4o"
        if request.complexity == TaskComplexity.COMPLEX:
            return "o1-preview"
        if request.complexity == TaskComplexity.CREATIVE:
            return "gpt-4o"
        return self.config.default_model
    
    def _build_messages(self, request: GenerationRequest) -> List[Dict]:
        """Build OpenAI message format."""
        messages = []
        
        system_prompt = self._get_system_prompt(request)
        messages.append({"role": "system", "content": system_prompt})
        
        if request.context.get("history"):
            messages.extend(request.context["history"])
            
        messages.append({"role": "user", "content": request.prompt})
        
        return messages
    
    def _get_system_prompt(self, request: GenerationRequest) -> str:
        """Get task-specific system prompt."""
        prompts = {
            "text": "You are a helpful AI assistant integrated into a Minecraft mod called Lucineer.",
            "script": "You are a JavaScript code generator for Minecraft automation. Generate clean, documented code.",
            "npc": "You are an NPC dialogue writer. Create immersive, character-driven responses.",
            "lesson": "You are an educational AI. Create progressive, engaging learning content.",
            "image_prompt": "You are an image prompt optimizer. Convert descriptions into detailed Stable Diffusion prompts."
        }
        return prompts.get(request.task_type, prompts["text"])
    
    async def health_check(self) -> bool:
        """Check OpenAI API health."""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.config.base_url}/models",
                    headers={"Authorization": f"Bearer {self.config.api_key}"},
                    timeout=10.0
                )
                return response.status_code == 200
        except:
            return False
