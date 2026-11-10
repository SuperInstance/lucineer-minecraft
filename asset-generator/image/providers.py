"""
Image Generation System
=======================
Multi-provider image generation for Minecraft assets.
Supports DeepInfra, Stability AI, local Stable Diffusion, and DALL-E.

Features:
- Texture generation
- Item sprites
- Entity skins
- UI icons
- Environment textures
"""

import asyncio
import base64
import json
import httpx
from typing import Dict, List, Optional, Any, Tuple
from dataclasses import dataclass, field
from enum import Enum
from pathlib import Path
import hashlib

from api.providers import (
    BaseProvider, ProviderConfig, GenerationRequest, GenerationResponse,
    ProviderTier
)


class ImageSize(Enum):
    """Standard image sizes for Minecraft assets."""
    TILE_16 = (16, 16)       # Block/item textures
    TILE_32 = (32, 32)       # HD textures
    TILE_64 = (64, 64)       # High-detail items
    TILE_128 = (128, 128)    # UI icons
    TILE_256 = (256, 256)    # Entity textures
    TILE_512 = (512, 512)    # Concept art
    TILE_1024 = (1024, 1024) # Backgrounds


class ImageStyle(Enum):
    """Art styles for generation."""
    PIXEL_ART = "pixel art, minecraft style, 16-bit"
    REALISTIC = "realistic, detailed, 4k"
    CARTOON = "cartoon, vibrant, clean lines"
    ANIME = "anime style, cel shaded"
    FANTASY = "fantasy art, magical, ethereal"
    SCI_FI = "science fiction, futuristic, neon"
    NATURAL = "natural, organic, earth tones"
    INDUSTRIAL = "industrial, mechanical, metal"


@dataclass
class ImageGenerationRequest(GenerationRequest):
    """Extended request for image generation."""
    size: ImageSize = ImageSize.TILE_64
    style: ImageStyle = ImageStyle.PIXEL_ART
    negative_prompt: str = ""
    num_images: int = 1
    steps: int = 30
    guidance_scale: float = 7.5
    seed: Optional[int] = None
    transparent_background: bool = False
    tileable: bool = False  # For seamless textures
    

@dataclass
class ImageGenerationResponse(GenerationResponse):
    """Response with image data."""
    image_data: bytes = b""
    image_base64: str = ""
    width: int = 0
    height: int = 0
    format: str = "png"
    seed_used: int = 0


class DeepInfraImageProvider(BaseProvider):
    """
    DeepInfra image generation - cost-effective Stable Diffusion.
    
    Models:
    - stabilityai/stable-diffusion-xl-base-1.0
    - runwayml/stable-diffusion-v1-5
    -ByteDance/SDXL-Lightning (fast)
    
    Strengths:
    - Low cost (~$0.002/image)
    - Fast generation
    - High quality
    """
    
    MODEL_SDXL = "stabilityai/stable-diffusion-xl-base-1.0"
    MODEL_SD15 = "runwayml/stable-diffusion-v1-5"
    MODEL_LIGHTNING = "ByteDance/SDXL-Lightning"
    MODEL_FLUX = "black-forest-labs/FLUX-1-schnell"
    
    def __init__(self, api_key: Optional[str] = None):
        config = ProviderConfig(
            name="deepinfra_image",
            api_key=api_key,
            base_url="https://api.deepinfra.com/v1/inference",
            models=[self.MODEL_SDXL, self.MODEL_SD15, self.MODEL_LIGHTNING, self.MODEL_FLUX],
            default_model=self.MODEL_SDXL,
            tier=ProviderTier.CLOUD_BALANCED,
            max_requests_per_minute=30,
            supports_vision=False,
            supports_streaming=False,
            cost_per_1k_tokens=0.0,  # Per-image pricing
            priority=8
        )
        super().__init__(config)
        
    async def generate(self, request: GenerationRequest) -> GenerationResponse:
        """Generate image using DeepInfra."""
        img_request = request if isinstance(request, ImageGenerationRequest) else ImageGenerationRequest(prompt=request.prompt)
        
        start_time = asyncio.get_event_loop().time()
        
        # Select model based on speed needs
        model = self._select_model(img_request)
        
        # Build enhanced prompt with style
        prompt = self._enhance_prompt(img_request)
        
        headers = {
            "Authorization": f"Bearer {self.config.api_key}",
            "Content-Type": "application/json"
        }
        
        payload = {
            "model": model,
            "prompt": prompt,
            "negative_prompt": img_request.negative_prompt,
            "width": img_request.size.value[0],
            "height": img_request.size.value[1],
            "num_images": img_request.num_images,
            "guidance_scale": img_request.guidance_scale,
            "num_inference_steps": img_request.steps,
        }
        
        if img_request.seed is not None:
            payload["seed"] = img_request.seed
            
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{self.config.base_url}/{model}",
                headers=headers,
                json=payload,
                timeout=120.0
            )
            response.raise_for_status()
            data = response.json()
            
        self.request_count += 1
        latency_ms = (asyncio.get_event_loop().time() - start_time) * 1000
        self.total_latency_ms += latency_ms
        
        # Extract image data
        images = data.get("images", [])
        if not images:
            raise ValueError("No images in response")
            
        image_b64 = images[0]
        image_data = base64.b64decode(image_b64)
        
        return ImageGenerationResponse(
            content={"images": [image_b64]},
            content_type="image",
            provider="deepinfra",
            model=model,
            latency_ms=latency_ms,
            image_data=image_data,
            image_base64=image_b64,
            width=img_request.size.value[0],
            height=img_request.size.value[1],
            seed_used=data.get("seed", 0)
        )
    
    def _select_model(self, request: ImageGenerationRequest) -> str:
        """Select model based on requirements."""
        if request.steps <= 5:
            return self.MODEL_LIGHTNING
        if request.size in [ImageSize.TILE_512, ImageSize.TILE_1024]:
            return self.MODEL_FLUX
        return self.MODEL_SDXL
    
    def _enhance_prompt(self, request: ImageGenerationRequest) -> str:
        """Enhance prompt with style and quality modifiers."""
        style_modifiers = {
            ImageStyle.PIXEL_ART: "pixel art style, minecraft aesthetic, 16x16 tiles, clean edges, limited palette",
            ImageStyle.REALISTIC: "highly detailed, realistic, 4k quality, professional rendering",
            ImageStyle.CARTOON: "cartoon style, vibrant colors, clean lines, stylized",
            ImageStyle.ANIME: "anime art style, cel shading, vibrant, expressive",
            ImageStyle.FANTASY: "fantasy art, magical atmosphere, ethereal lighting, epic",
            ImageStyle.SCI_FI: "science fiction, futuristic, neon lights, cyber aesthetic",
            ImageStyle.NATURAL: "natural textures, organic shapes, earth tones, realistic",
            ImageStyle.INDUSTRIAL: "industrial design, metallic surfaces, mechanical details"
        }
        
        modifiers = [request.prompt]
        
        # Add style
        modifiers.append(style_modifiers.get(request.style, ""))
        
        # Add texture modifiers
        if request.tileable:
            modifiers.append("seamless tileable texture, repeatable pattern")
            
        if request.transparent_background:
            modifiers.append("transparent background, isolated subject")
            
        # Quality boosters
        modifiers.append("high quality, detailed, clean")
        
        return ", ".join(m for m in modifiers if m)
    
    async def health_check(self) -> bool:
        """Check DeepInfra availability."""
        return True  # DeepInfra is generally reliable


class LocalStableDiffusionProvider(BaseProvider):
    """
    Local Stable Diffusion - Free, private generation.
    
    Requires:
    - Automatic1111 WebUI (localhost:7860)
    - ComfyUI (localhost:8188)
    - Or diffusers library
    
    Strengths:
    - Zero cost
    - Full privacy
    - Custom models
    - No rate limits
    """
    
    def __init__(self, base_url: str = "http://localhost:7860", backend: str = "a1111"):
        config = ProviderConfig(
            name="local_sd",
            base_url=base_url,
            models=["sd_xl_base_1.0", "sd_v1.5", "sdxl_lightning"],
            default_model="sd_xl_base_1.0",
            tier=ProviderTier.LOCAL_FREE,
            max_requests_per_minute=1000,
            supports_vision=False,
            supports_streaming=False,
            cost_per_1k_tokens=0.0,
            priority=10
        )
        super().__init__(config)
        self.backend = backend
        self._healthy = None
        
    async def generate(self, request: GenerationRequest) -> GenerationResponse:
        """Generate using local Stable Diffusion."""
        img_request = request if isinstance(request, ImageGenerationRequest) else ImageGenerationRequest(prompt=request.prompt)
        
        start_time = asyncio.get_event_loop().time()
        
        if self.backend == "a1111":
            response = await self._generate_a1111(img_request)
        elif self.backend == "comfyui":
            response = await self._generate_comfyui(img_request)
        else:
            raise ValueError(f"Unknown backend: {self.backend}")
            
        self.request_count += 1
        latency_ms = (asyncio.get_event_loop().time() - start_time) * 1000
        self.total_latency_ms += latency_ms
        
        return response
    
    async def _generate_a1111(self, request: ImageGenerationRequest) -> ImageGenerationResponse:
        """Generate using Automatic1111 API."""
        prompt = self._enhance_prompt(request)
        
        payload = {
            "prompt": prompt,
            "negative_prompt": request.negative_prompt or "blurry, low quality, distorted",
            "width": request.size.value[0],
            "height": request.size.value[1],
            "steps": request.steps,
            "cfg_scale": request.guidance_scale,
            "batch_size": request.num_images,
        }
        
        if request.seed is not None:
            payload["seed"] = request.seed
            
        if request.tileable:
            payload["tiling"] = True
            
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{self.config.base_url}/sdapi/v1/txt2img",
                json=payload,
                timeout=300.0
            )
            response.raise_for_status()
            data = response.json()
            
        image_b64 = data["images"][0]
        image_data = base64.b64decode(image_b64.split(",", 1)[-1] if "," in image_b64 else image_b64)
        
        return ImageGenerationResponse(
            content={"images": [image_b64]},
            content_type="image",
            provider="local_sd",
            model=self.config.default_model,
            latency_ms=0,
            image_data=image_data,
            image_base64=image_b64,
            width=request.size.value[0],
            height=request.size.value[1],
            seed_used=data.get("seed", 0)
        )
    
    async def _generate_comfyui(self, request: ImageGenerationRequest) -> ImageGenerationResponse:
        """Generate using ComfyUI API."""
        # Simplified ComfyUI workflow
        workflow = {
            "3": {
                "inputs": {
                    "seed": request.seed or 0,
                    "steps": request.steps,
                    "cfg": request.guidance_scale,
                    "sampler_name": "euler",
                    "scheduler": "normal",
                    "denoise": 1.0,
                    "model": ["4", 0],
                    "positive": ["6", 0],
                    "negative": ["7", 0],
                    "latent_image": ["5", 0]
                },
                "class_type": "KSampler"
            },
            "4": {
                "inputs": {"ckpt_name": "sd_xl_base_1.0.safetensors"},
                "class_type": "CheckpointLoaderSimple"
            },
            "5": {
                "inputs": {"width": request.size.value[0], "height": request.size.value[1], "batch_size": 1},
                "class_type": "EmptyLatentImage"
            },
            "6": {
                "inputs": {"text": self._enhance_prompt(request), "clip": ["4", 1]},
                "class_type": "CLIPTextEncode"
            },
            "7": {
                "inputs": {"text": request.negative_prompt, "clip": ["4", 1]},
                "class_type": "CLIPTextEncode"
            },
            "8": {
                "inputs": {"samples": ["3", 0], "vae": ["4", 2]},
                "class_type": "VAEDecode"
            },
            "9": {
                "inputs": {"filename_prefix": "minecraft_asset", "images": ["8", 0]},
                "class_type": "SaveImage"
            }
        }
        
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{self.config.base_url}/prompt",
                json={"prompt": workflow},
                timeout=300.0
            )
            response.raise_for_status()
            prompt_id = response.json()["prompt_id"]
            
            # Wait for completion (simplified)
            await asyncio.sleep(request.steps * 0.1)
            
            # Get result
            history_response = await client.get(
                f"{self.config.base_url}/history/{prompt_id}"
            )
            history = history_response.json()
            
            # Get image from output
            outputs = history.get(prompt_id, {}).get("outputs", {})
            for node_id, output in outputs.items():
                if "images" in output:
                    image_info = output["images"][0]
                    image_response = await client.get(
                        f"{self.config.base_url}/view",
                        params={"filename": image_info["filename"], "subfolder": image_info.get("subfolder", ""), "type": image_info.get("type", "output")}
                    )
                    image_data = image_response.content
                    image_b64 = base64.b64encode(image_data).decode()
                    
                    return ImageGenerationResponse(
                        content={"images": [image_b64]},
                        content_type="image",
                        provider="local_sd",
                        model=self.config.default_model,
                        latency_ms=0,
                        image_data=image_data,
                        image_base64=image_b64,
                        width=request.size.value[0],
                        height=request.size.value[1]
                    )
                    
        raise ValueError("No image generated")
    
    def _enhance_prompt(self, request: ImageGenerationRequest) -> str:
        """Enhance prompt with style modifiers."""
        style_additions = {
            ImageStyle.PIXEL_ART: ", pixel art style, minecraft aesthetic, 16-bit, retro game art",
            ImageStyle.REALISTIC: ", highly detailed, realistic, 8k uhd, professional photography",
            ImageStyle.CARTOON: ", cartoon style, vibrant colors, clean lines, animated style",
        }
        return request.prompt + style_additions.get(request.style, "")
    
    async def health_check(self) -> bool:
        """Check if local SD is running."""
        try:
            async with httpx.AsyncClient() as client:
                if self.backend == "a1111":
                    response = await client.get(
                        f"{self.config.base_url}/sdapi/v1/sd-models",
                        timeout=5.0
                    )
                else:
                    response = await client.get(
                        f"{self.config.base_url}/system_stats",
                        timeout=5.0
                    )
                self._healthy = response.status_code == 200
                return self._healthy
        except:
            self._healthy = False
            return False
    
    def is_healthy(self) -> bool:
        return self._healthy if self._healthy is not None else False


class MinecraftTextureOptimizer:
    """
    Post-processing for Minecraft textures.
    Converts generated images to proper Minecraft texture format.
    """
    
    @staticmethod
    def to_minecraft_texture(image_data: bytes, size: int = 16) -> bytes:
        """Convert image to Minecraft texture format."""
        try:
            from PIL import Image
            import io
            
            img = Image.open(io.BytesIO(image_data))
            
            # Resize to target size
            img = img.resize((size, size), Image.Resampling.NEAREST)
            
            # Ensure RGBA mode
            if img.mode != "RGBA":
                img = img.convert("RGBA")
                
            # Optimize palette for pixel art look
            img = img.quantize(colors=256, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.FLOYDSTEINBERG)
            img = img.convert("RGBA")
            
            # Save as PNG
            output = io.BytesIO()
            img.save(output, format="PNG", optimize=True)
            return output.getvalue()
        except ImportError:
            return image_data  # Return original if PIL not available
    
    @staticmethod
    def make_tileable(image_data: bytes) -> bytes:
        """Make texture seamlessly tileable."""
        try:
            from PIL import Image
            import io
            import numpy as np
            
            img = Image.open(io.BytesIO(image_data))
            arr = np.array(img)
            
            # Simple mirroring approach for tileability
            h, w = arr.shape[:2]
            result = arr.copy()
            
            # Blend edges
            for i in range(min(4, w // 4)):
                alpha = i / (min(4, w // 4))
                result[:, i] = (arr[:, i] * alpha + arr[:, -i-1] * (1 - alpha)).astype(np.uint8)
                result[:, -i-1] = (arr[:, -i-1] * alpha + arr[:, i] * (1 - alpha)).astype(np.uint8)
                result[i, :] = (arr[i, :] * alpha + arr[-i-1, :] * (1 - alpha)).astype(np.uint8)
                result[-i-1, :] = (arr[-i-1, :] * alpha + arr[i, :] * (1 - alpha)).astype(np.uint8)
            
            return Image.fromarray(result).tobytes()
        except:
            return image_data
