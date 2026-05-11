"""Code quality analysis agent using LangChain."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser

from src.utils.github_fetcher import RepoSnapshot

SYSTEM_PROMPT = """Sen uzman bir yazılım mühendisisin. Senden verilen açık kaynak projesinin kodunu analiz etmeni istiyorum.

Analiz sonucunu YALNIZCA aşağıdaki JSON şemasına uygun olarak döndür (başka hiçbir şey yazma):

{{
  "score": <0-100 arası tam sayı>,
  "summary": "<3-5 cümlelik genel değerlendirme>",
  "strengths": ["<güçlü yön 1>", "..."],
  "issues": [
    {{
      "file": "<dosya_yolu>",
      "line_hint": "<satır veya fonksiyon adı, bilinmiyorsa null>",
      "severity": "high|medium|low",
      "description": "<sorun açıklaması>",
      "suggestion": "<somut iyileştirme önerisi>"
    }}
  ],
  "best_practices": {{
    "naming_conventions": <0-10>,
    "function_length": <0-10>,
    "code_duplication": <0-10>,
    "error_handling": <0-10>,
    "type_hints_or_types": <0-10>,
    "modularity": <0-10>
  }}
}}

Skorlama kriterleri:
- 90-100: Mükemmel, endüstri standartlarında
- 70-89: İyi, küçük iyileştirmeler yapılabilir
- 50-69: Orta, önemli sorunlar var
- 30-49: Zayıf, kapsamlı refactor gerekiyor
- 0-29: Çok kötü, temel ilkeler göz ardı edilmiş
"""

USER_PROMPT = """Proje: {full_name}
Açıklama: {description}
Birincil dil: {language}

--- KOD DOSYALARI ---
{code_snippets}
"""


def _prepare_snippets(snapshot: RepoSnapshot, max_chars: int = 20_000) -> str:
    parts: list[str] = []
    total = 0
    for f in snapshot.code_files:
        if total >= max_chars:
            break
        snippet = f.content[:3000]
        block = f"### {f.path}\n```\n{snippet}\n```\n"
        parts.append(block)
        total += len(block)
    return "\n".join(parts) if parts else "Kod dosyası bulunamadı."


@dataclass
class CodeQualityResult:
    score: int = 0
    summary: str = ""
    strengths: list[str] = field(default_factory=list)
    issues: list[dict[str, Any]] = field(default_factory=list)
    best_practices: dict[str, int] = field(default_factory=dict)
    raw: dict[str, Any] = field(default_factory=dict)


def run_code_quality_agent(snapshot: RepoSnapshot, llm) -> CodeQualityResult:
    prompt = ChatPromptTemplate.from_messages([
        ("system", SYSTEM_PROMPT),
        ("human", USER_PROMPT),
    ])

    chain = prompt | llm | JsonOutputParser()

    raw = chain.invoke({
        "full_name": snapshot.full_name,
        "description": snapshot.description or "Açıklama yok",
        "language": snapshot.language or "Bilinmiyor",
        "code_snippets": _prepare_snippets(snapshot),
    })

    return CodeQualityResult(
        score=int(raw.get("score", 0)),
        summary=raw.get("summary", ""),
        strengths=raw.get("strengths", []),
        issues=raw.get("issues", []),
        best_practices=raw.get("best_practices", {}),
        raw=raw,
    )
