"""Documentation and open-source standards analysis agent."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser

from src.utils.github_fetcher import RepoSnapshot

SYSTEM_PROMPT = """Sen açık kaynak proje yönetimi konusunda uzman bir danışmansın. Verilen projeyi açık kaynak topluluk standartları açısından değerlendir.

YALNIZCA aşağıdaki JSON şemasını döndür:

{{
  "score": <0-100 arası tam sayı>,
  "summary": "<3-5 cümlelik genel değerlendirme>",
  "checklist": {{
    "readme": {{"present": true/false, "quality": 0-10, "notes": "..."}},
    "license": {{"present": true/false, "type": "<lisans adı veya null>", "notes": "..."}},
    "contributing_guide": {{"present": true/false, "quality": 0-10, "notes": "..."}},
    "changelog": {{"present": true/false, "quality": 0-10, "notes": "..."}},
    "code_of_conduct": {{"present": true/false, "notes": "..."}},
    "security_policy": {{"present": true/false, "notes": "..."}},
    "ci_cd": {{"present": true/false, "notes": "..."}},
    "tests": {{"present": true/false, "notes": "..."}},
    "issue_templates": {{"present": true/false, "notes": "..."}}
  }},
  "readme_analysis": {{
    "has_description": true/false,
    "has_installation": true/false,
    "has_usage_examples": true/false,
    "has_badges": true/false,
    "has_screenshots_or_demos": true/false,
    "has_contributing_section": true/false,
    "clarity_score": 0-10
  }},
  "recommendations": [
    {{
      "priority": "high|medium|low",
      "area": "<alan adı>",
      "action": "<yapılması gereken somut eylem>"
    }}
  ]
}}
"""

USER_PROMPT = """Proje: {full_name}
Yıldız: {stars} | Fork: {forks} | Açık Issue: {open_issues}
Lisans: {license_name}
Wiki: {has_wiki}

Mevcut dosyalar ve meta bilgiler:
- has_contributing: {has_contributing}
- has_changelog: {has_changelog}
- has_code_of_conduct: {has_code_of_conduct}
- has_security_policy: {has_security_policy}
- has_ci: {has_ci}
- has_tests: {has_tests}
- topics: {topics}

--- README & DOC İÇERİKLERİ ---
{doc_contents}
"""


def _prepare_docs(snapshot: RepoSnapshot, max_chars: int = 12_000) -> str:
    parts: list[str] = []
    total = 0
    for f in snapshot.doc_files:
        if not f.content or total >= max_chars:
            continue
        snippet = f.content[:5000]
        block = f"### {f.path}\n{snippet}\n"
        parts.append(block)
        total += len(block)
    return "\n".join(parts) if parts else "Dokümantasyon dosyası içeriği alınamadı."


@dataclass
class DocsResult:
    score: int = 0
    summary: str = ""
    checklist: dict[str, Any] = field(default_factory=dict)
    readme_analysis: dict[str, Any] = field(default_factory=dict)
    recommendations: list[dict[str, Any]] = field(default_factory=list)
    raw: dict[str, Any] = field(default_factory=dict)


def run_docs_agent(snapshot: RepoSnapshot, llm) -> DocsResult:
    prompt = ChatPromptTemplate.from_messages([
        ("system", SYSTEM_PROMPT),
        ("human", USER_PROMPT),
    ])

    chain = prompt | llm | JsonOutputParser()

    raw = chain.invoke({
        "full_name": snapshot.full_name,
        "stars": snapshot.stars,
        "forks": snapshot.forks,
        "open_issues": snapshot.open_issues,
        "license_name": snapshot.license_name or "Lisans yok",
        "has_wiki": snapshot.has_wiki,
        "has_contributing": snapshot.has_contributing,
        "has_changelog": snapshot.has_changelog,
        "has_code_of_conduct": snapshot.has_code_of_conduct,
        "has_security_policy": snapshot.has_security_policy,
        "has_ci": snapshot.has_ci,
        "has_tests": snapshot.has_tests,
        "topics": ", ".join(snapshot.topics) if snapshot.topics else "yok",
        "doc_contents": _prepare_docs(snapshot),
    })

    return DocsResult(
        score=int(raw.get("score", 0)),
        summary=raw.get("summary", ""),
        checklist=raw.get("checklist", {}),
        readme_analysis=raw.get("readme_analysis", {}),
        recommendations=raw.get("recommendations", []),
        raw=raw,
    )
