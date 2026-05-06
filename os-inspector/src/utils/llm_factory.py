"""LLM provider factory — supports Anthropic Claude and OpenAI GPT."""

from __future__ import annotations

import os
from typing import Any


def get_llm(
    provider: str | None = None,
    model: str | None = None,
    temperature: float = 0.1,
    **kwargs: Any,
):
    """Return a LangChain chat model based on env vars or explicit arguments."""

    provider = (provider or os.getenv("LLM_PROVIDER", "anthropic")).lower()
    model = model or os.getenv("LLM_MODEL")

    if provider == "anthropic":
        from langchain_anthropic import ChatAnthropic

        model = model or "claude-sonnet-4-6"
        api_key = os.getenv("ANTHROPIC_API_KEY")
        if not api_key:
            raise EnvironmentError("ANTHROPIC_API_KEY ortam değişkeni ayarlanmamış.")
        return ChatAnthropic(
            model=model,
            api_key=api_key,
            temperature=temperature,
            max_tokens=4096,
            **kwargs,
        )

    if provider == "openai":
        from langchain_openai import ChatOpenAI

        model = model or "gpt-4o"
        api_key = os.getenv("OPENAI_API_KEY")
        if not api_key:
            raise EnvironmentError("OPENAI_API_KEY ortam değişkeni ayarlanmamış.")
        return ChatOpenAI(
            model=model,
            api_key=api_key,
            temperature=temperature,
            **kwargs,
        )

    raise ValueError(f"Desteklenmeyen LLM sağlayıcısı: {provider!r}. 'anthropic' veya 'openai' kullanın.")
