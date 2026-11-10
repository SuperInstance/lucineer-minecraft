"""
Asset Generator Configuration
=============================
Centralized configuration for all providers and settings.
"""

import os
import json
from typing import Dict, Any, Optional, List
from dataclasses import dataclass, field
from pathlib import Path
from enum import Enum


class ProviderPriority(Enum):
    """Provider priority levels."""
    HIGHEST = 10
    HIGH = 8
    NORMAL = 5
    LOW = 3
    FALLBACK = 1


@dataclass
class LLMProviderConfig:
    """Configuration for an LLM provider."""
    name: str
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    enabled: bool = True
    priority: int = 5
    models: List[str] = field(default_factory=list)
    default_model: str = ""
    max_requests_per_minute: int = 60
    cost_per_1k_tokens: float = 0.0
    
    @classmethod
    def from_env(cls, name: str, env_key: str, **kwargs) -> 'LLMProviderConfig':
        """Create config from environment variable."""
        return cls(
            name=name,
            api_key=os.getenv(env_key),
            **kwargs
        )


@dataclass
class ImageProviderConfig:
    """Configuration for an image generation provider."""
    name: str
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    enabled: bool = True
    priority: int = 5
    default_size: tuple = (64, 64)
    default_steps: int = 30
    cost_per_image: float = 0.0


@dataclass
class CacheConfig:
    """Configuration for caching."""
    enabled: bool = True
    max_size: int = 1000
    ttl_seconds: int = 3600
    semantic_similarity_threshold: float = 0.95


@dataclass
class RoutingConfig:
    """Configuration for request routing."""
    prefer_local: bool = True
    fallback_enabled: bool = True
    complexity_analysis: bool = True
    max_retries: int = 3
    timeout_seconds: int = 120


@dataclass
class VibeConfig:
    """Configuration for vibe-coding."""
    context_window_blocks: int = 50
    history_length: int = 20
    auto_intent_detection: bool = True
    progressive_learning: bool = True
    lesson_generation: bool = True


@dataclass
class AssetGeneratorConfig:
    """Main configuration class."""
    
    # LLM Providers
    llm_providers: Dict[str, LLMProviderConfig] = field(default_factory=dict)
    
    # Image Providers  
    image_providers: Dict[str, ImageProviderConfig] = field(default_factory=dict)
    
    # Subsystems
    cache: CacheConfig = field(default_factory=CacheConfig)
    routing: RoutingConfig = field(default_factory=RoutingConfig)
    vibe: VibeConfig = field(default_factory=VibeConfig)
    
    # Storage
    data_dir: Path = field(default_factory=lambda: Path("./data/assets"))
    output_dir: Path = field(default_factory=lambda: Path("./output"))
    
    @classmethod
    def from_env(cls) -> 'AssetGeneratorConfig':
        """Load configuration from environment variables."""
        config = cls()
        
        # LLM Providers
        config.llm_providers = {
            "deepseek": LLMProviderConfig.from_env(
                "deepseek",
                "DEEPSEEK_API_KEY",
                base_url="https://api.deepseek.com/v1",
                models=["deepseek-chat", "deepseek-reasoner"],
                default_model="deepseek-chat",
                priority=8,
                cost_per_1k_tokens=0.001
            ),
            "groq": LLMProviderConfig.from_env(
                "groq",
                "GROQ_API_KEY",
                base_url="https://api.groq.com/openai/v1",
                models=["llama-3.3-70b-versatile", "llama-3.1-8b-instant"],
                default_model="llama-3.3-70b-versatile",
                priority=9,
                cost_per_1k_tokens=0.0
            ),
            "openai": LLMProviderConfig.from_env(
                "openai",
                "OPENAI_API_KEY",
                base_url="https://api.openai.com/v1",
                models=["gpt-4o", "gpt-4o-mini", "o1-preview"],
                default_model="gpt-4o-mini",
                priority=7,
                cost_per_1k_tokens=0.01
            ),
            "local": LLMProviderConfig(
                name="local",
                base_url=os.getenv("LOCAL_LLM_URL", "http://localhost:11434"),
                enabled=True,
                priority=10,
                models=["llama3.2", "mistral", "phi3"],
                default_model="llama3.2",
                cost_per_1k_tokens=0.0
            )
        }
        
        # Image Providers
        config.image_providers = {
            "deepinfra": ImageProviderConfig.from_env(
                "deepinfra",
                "DEEPINFRA_API_KEY",
                base_url="https://api.deepinfra.com/v1/inference",
                default_size=(64, 64),
                default_steps=20,
                cost_per_image=0.002
            ),
            "local_sd": ImageProviderConfig(
                name="local_sd",
                base_url=os.getenv("LOCAL_SD_URL", "http://localhost:7860"),
                enabled=True,
                priority=10,
                default_size=(64, 64),
                default_steps=30,
                cost_per_image=0.0
            )
        }
        
        return config
    
    @classmethod
    def from_file(cls, path: str) -> 'AssetGeneratorConfig':
        """Load configuration from JSON file."""
        with open(path, 'r') as f:
            data = json.load(f)
            
        config = cls()
        
        # Load LLM providers
        for name, prov_data in data.get("llm_providers", {}).items():
            config.llm_providers[name] = LLMProviderConfig(
                name=name,
                api_key=os.getenv(prov_data.get("api_key_env", ""), prov_data.get("api_key")),
                base_url=prov_data.get("base_url"),
                enabled=prov_data.get("enabled", True),
                priority=prov_data.get("priority", 5),
                models=prov_data.get("models", []),
                default_model=prov_data.get("default_model", "")
            )
            
        # Load image providers
        for name, prov_data in data.get("image_providers", {}).items():
            config.image_providers[name] = ImageProviderConfig(
                name=name,
                api_key=os.getenv(prov_data.get("api_key_env", ""), prov_data.get("api_key")),
                base_url=prov_data.get("base_url"),
                enabled=prov_data.get("enabled", True),
                priority=prov_data.get("priority", 5)
            )
            
        return config
    
    def to_file(self, path: str):
        """Save configuration to JSON file."""
        data = {
            "llm_providers": {
                name: {
                    "api_key": "***" if prov.api_key else None,
                    "base_url": prov.base_url,
                    "enabled": prov.enabled,
                    "priority": prov.priority,
                    "models": prov.models,
                    "default_model": prov.default_model
                }
                for name, prov in self.llm_providers.items()
            },
            "image_providers": {
                name: {
                    "api_key": "***" if prov.api_key else None,
                    "base_url": prov.base_url,
                    "enabled": prov.enabled,
                    "priority": prov.priority
                }
                for name, prov in self.image_providers.items()
            },
            "cache": {
                "enabled": self.cache.enabled,
                "max_size": self.cache.max_size,
                "ttl_seconds": self.cache.ttl_seconds
            },
            "routing": {
                "prefer_local": self.routing.prefer_local,
                "fallback_enabled": self.routing.fallback_enabled
            },
            "vibe": {
                "context_window_blocks": self.vibe.context_window_blocks,
                "progressive_learning": self.vibe.progressive_learning
            }
        }
        
        with open(path, 'w') as f:
            json.dump(data, f, indent=2)


# Default configuration instance
_config: Optional[AssetGeneratorConfig] = None


def get_config() -> AssetGeneratorConfig:
    """Get the global configuration instance."""
    global _config
    if _config is None:
        _config = AssetGeneratorConfig.from_env()
    return _config


def set_config(config: AssetGeneratorConfig):
    """Set the global configuration instance."""
    global _config
    _config = config
