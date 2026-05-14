"""Aggregate scoring engine — combines all agent results into a final score."""

from __future__ import annotations

from dataclasses import dataclass

from src.agents.code_quality_agent import CodeQualityResult
from src.agents.security_agent import SecurityResult
from src.agents.docs_agent import DocsResult

# Weight distribution (must sum to 1.0)
WEIGHTS = {
    "code_quality": 0.40,
    "security": 0.35,
    "documentation": 0.25,
}

GRADE_MAP = [
    (90, "A+", "Mükemmel"),
    (80, "A",  "Çok İyi"),
    (70, "B+", "İyi"),
    (60, "B",  "Orta-İyi"),
    (50, "C",  "Orta"),
    (40, "D",  "Zayıf"),
    (0,  "F",  "Yetersiz"),
]


def _grade(score: float) -> tuple[str, str]:
    for threshold, letter, label in GRADE_MAP:
        if score >= threshold:
            return letter, label
    return "F", "Yetersiz"


@dataclass
class FinalReport:
    repo_url: str
    overall_score: float
    grade_letter: str
    grade_label: str

    code_quality_score: int
    security_score: int
    docs_score: int

    code_quality: CodeQualityResult
    security: SecurityResult
    docs: DocsResult

    top_issues: list[dict]
    top_recommendations: list[dict]


def build_final_report(
    repo_url: str,
    cq: CodeQualityResult,
    sec: SecurityResult,
    docs: DocsResult,
) -> FinalReport:
    overall = (
        cq.score * WEIGHTS["code_quality"]
        + sec.score * WEIGHTS["security"]
        + docs.score * WEIGHTS["documentation"]
    )
    overall = round(overall, 1)
    grade_letter, grade_label = _grade(overall)

    # Collect top high/critical issues across agents
    top_issues: list[dict] = []

    # Security vulnerabilities (critical + high first)
    severity_order = {"critical": 0, "high": 1, "medium": 2, "low": 3, "info": 4}
    sorted_vulns = sorted(
        sec.vulnerabilities,
        key=lambda v: severity_order.get(v.get("severity", "info"), 5),
    )
    for v in sorted_vulns[:3]:
        top_issues.append({
            "category": "Güvenlik",
            "severity": v.get("severity", "?"),
            "file": v.get("file", ""),
            "description": v.get("description", ""),
            "suggestion": v.get("remediation", ""),
        })

    # Code quality issues (high first)
    cq_sorted = sorted(
        cq.issues,
        key=lambda i: severity_order.get(i.get("severity", "low"), 5),
    )
    for i in cq_sorted[:3]:
        top_issues.append({
            "category": "Kod Kalitesi",
            "severity": i.get("severity", "?"),
            "file": i.get("file", ""),
            "description": i.get("description", ""),
            "suggestion": i.get("suggestion", ""),
        })

    # High-priority doc recommendations
    top_recommendations = [
        r for r in docs.recommendations if r.get("priority") == "high"
    ][:5]

    return FinalReport(
        repo_url=repo_url,
        overall_score=overall,
        grade_letter=grade_letter,
        grade_label=grade_label,
        code_quality_score=cq.score,
        security_score=sec.score,
        docs_score=docs.score,
        code_quality=cq,
        security=sec,
        docs=docs,
        top_issues=top_issues,
        top_recommendations=top_recommendations,
    )
