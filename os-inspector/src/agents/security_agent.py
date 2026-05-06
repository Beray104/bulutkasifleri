"""Security vulnerability analysis agent."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser

from src.utils.github_fetcher import RepoSnapshot

SYSTEM_PROMPT = """Sen bir uygulama güvenliği uzmanısın (AppSec). Verilen açık kaynak projesinin kaynak kodunu OWASP Top 10 ve yaygın güvenlik açıkları açısından incele.

YALNIZCA aşağıdaki JSON şemasını döndür:

{{
  "score": <0-100 arası tam sayı; 100 = çok güvenli>,
  "risk_level": "critical|high|medium|low|minimal",
  "summary": "<3-5 cümlelik güvenlik değerlendirmesi>",
  "vulnerabilities": [
    {{
      "id": "VULN-001",
      "file": "<dosya_yolu>",
      "line_hint": "<satır veya fonksiyon, bilinmiyorsa null>",
      "owasp_category": "<OWASP kategori adı veya CWE kodu>",
      "severity": "critical|high|medium|low|info",
      "description": "<açık açıklaması>",
      "exploit_scenario": "<kötüye kullanım senaryosu>",
      "remediation": "<düzeltme önerisi>"
    }}
  ],
  "positive_findings": ["<iyi güvenlik uygulaması 1>", "..."],
  "dependency_notes": "<bağımlılıklarla ilgili genel gözlem veya null>"
}}

Arama önceliği:
1. SQL/NoSQL/Command/LDAP Injection
2. XSS, CSRF, SSRF
3. Kriptografik zayıflıklar (MD5, SHA1, sabit salt, zayıf IV)
4. Sızdırılan sırlar / kimlik bilgileri (hardcoded token, key, şifre)
5. Güvensiz dosya işlemleri / path traversal
6. Güvensiz deserialization
7. Broken access control
8. Güvenli olmayan bağımlılıklar
"""

USER_PROMPT = """Proje: {full_name}
Dil: {language}

--- KOD ---
{code_snippets}
"""


def _prepare_snippets(snapshot: RepoSnapshot, max_chars: int = 25_000) -> str:
    parts: list[str] = []
    total = 0
    for f in snapshot.code_files:
        if total >= max_chars:
            break
        snippet = f.content[:4000]
        block = f"### {f.path}\n```\n{snippet}\n```\n"
        parts.append(block)
        total += len(block)
    return "\n".join(parts) if parts else "Kod dosyası bulunamadı."


@dataclass
class SecurityResult:
    score: int = 0
    risk_level: str = "unknown"
    summary: str = ""
    vulnerabilities: list[dict[str, Any]] = field(default_factory=list)
    positive_findings: list[str] = field(default_factory=list)
    dependency_notes: str | None = None
    raw: dict[str, Any] = field(default_factory=dict)


def run_security_agent(snapshot: RepoSnapshot, llm) -> SecurityResult:
    prompt = ChatPromptTemplate.from_messages([
        ("system", SYSTEM_PROMPT),
        ("human", USER_PROMPT),
    ])

    chain = prompt | llm | JsonOutputParser()

    raw = chain.invoke({
        "full_name": snapshot.full_name,
        "language": snapshot.language or "Bilinmiyor",
        "code_snippets": _prepare_snippets(snapshot),
    })

    return SecurityResult(
        score=int(raw.get("score", 0)),
        risk_level=raw.get("risk_level", "unknown"),
        summary=raw.get("summary", ""),
        vulnerabilities=raw.get("vulnerabilities", []),
        positive_findings=raw.get("positive_findings", []),
        dependency_notes=raw.get("dependency_notes"),
        raw=raw,
    )
