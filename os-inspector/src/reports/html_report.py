"""HTML report generator using Jinja2."""

from __future__ import annotations

import os
from datetime import datetime
from pathlib import Path

from jinja2 import Environment, BaseLoader

from src.utils.scorer import FinalReport

TEMPLATE = r"""<!DOCTYPE html>
<html lang="tr">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>OS Müfettişi — {{ report.repo_url }}</title>
  <style>
    :root {
      --bg: #0f1117; --surface: #1a1d27; --card: #222536;
      --accent: #7c6af7; --accent2: #4ecdc4;
      --text: #e2e4f0; --muted: #7b7fa8;
      --green: #4ade80; --yellow: #facc15; --orange: #fb923c;
      --red: #f87171; --critical: #ff4d6d;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { background: var(--bg); color: var(--text); font-family: 'Segoe UI', system-ui, sans-serif; line-height: 1.6; }
    a { color: var(--accent); text-decoration: none; }
    .container { max-width: 1100px; margin: 0 auto; padding: 2rem 1.5rem; }

    /* HEADER */
    header { background: linear-gradient(135deg, #1a1d27 0%, #2d1b69 100%);
             border-bottom: 1px solid #2e3150; padding: 2.5rem 0 2rem; }
    header .container { display: flex; align-items: center; gap: 1.5rem; flex-wrap: wrap; }
    .logo { font-size: 2.2rem; }
    .header-text h1 { font-size: 1.6rem; font-weight: 700; }
    .header-text p { color: var(--muted); font-size: 0.95rem; margin-top: .3rem; }
    .meta { margin-left: auto; text-align: right; font-size: 0.85rem; color: var(--muted); }

    /* SCORE HERO */
    .score-hero { display: grid; grid-template-columns: auto 1fr; gap: 2rem;
                  background: var(--surface); border-radius: 16px;
                  padding: 2rem; margin: 2rem 0; border: 1px solid #2e3150; align-items: center; }
    .big-score { text-align: center; }
    .big-score .number { font-size: 4.5rem; font-weight: 800; line-height: 1;
                         background: linear-gradient(135deg, var(--accent), var(--accent2));
                         -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .big-score .grade { font-size: 1.8rem; font-weight: 700; color: var(--text); }
    .big-score .label { color: var(--muted); font-size: 0.9rem; }
    .score-bars { display: grid; gap: 1rem; }
    .bar-row { display: grid; grid-template-columns: 160px 1fr 60px; gap: .8rem; align-items: center; }
    .bar-row label { font-size: 0.9rem; color: var(--muted); }
    .bar-track { background: #2a2d3e; border-radius: 999px; height: 10px; overflow: hidden; }
    .bar-fill { height: 100%; border-radius: 999px; transition: width .5s; }
    .bar-fill.quality { background: linear-gradient(90deg, #7c6af7, #4ecdc4); }
    .bar-fill.security { background: linear-gradient(90deg, #fb923c, #facc15); }
    .bar-fill.docs { background: linear-gradient(90deg, #4ade80, #4ecdc4); }
    .bar-val { font-weight: 700; font-size: 0.95rem; }

    /* CARDS */
    .section-title { font-size: 1.3rem; font-weight: 700; margin: 2.5rem 0 1rem;
                     display: flex; align-items: center; gap: .5rem; }
    .section-title::before { content: ''; display: block; width: 4px; height: 1.3rem;
                              background: var(--accent); border-radius: 2px; }
    .cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px,1fr)); gap: 1.2rem; }
    .card { background: var(--card); border-radius: 12px; padding: 1.4rem;
            border: 1px solid #2e3150; }
    .card h3 { font-size: 1rem; margin-bottom: .6rem; display: flex; align-items: center; gap: .4rem; }

    /* BADGES */
    .badge { display: inline-block; border-radius: 6px; padding: 2px 8px;
             font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }
    .badge.critical { background: #4d0018; color: var(--critical); }
    .badge.high   { background: #4d1500; color: var(--orange); }
    .badge.medium { background: #4d3900; color: var(--yellow); }
    .badge.low    { background: #1a3300; color: var(--green); }
    .badge.info   { background: #1a2040; color: #93c5fd; }
    .badge.minimal{ background: #1a3300; color: var(--green); }

    /* ISSUE LIST */
    .issue-list { list-style: none; display: grid; gap: .8rem; }
    .issue-item { background: var(--surface); border-radius: 10px; padding: 1rem 1.2rem;
                  border-left: 3px solid transparent; }
    .issue-item.critical { border-color: var(--critical); }
    .issue-item.high   { border-color: var(--orange); }
    .issue-item.medium { border-color: var(--yellow); }
    .issue-item.low    { border-color: var(--green); }
    .issue-item .path  { font-family: monospace; font-size: 0.8rem; color: var(--accent2); margin-bottom: .3rem; }
    .issue-item .desc  { font-size: 0.9rem; margin-bottom: .4rem; }
    .issue-item .suggest { font-size: 0.85rem; color: var(--muted); }
    .issue-item .suggest::before { content: '💡 '; }

    /* CHECKLIST */
    .checklist-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px,1fr)); gap: .7rem; }
    .check-item { background: var(--surface); border-radius: 8px; padding: .7rem 1rem;
                  display: flex; align-items: center; gap: .6rem; font-size: 0.9rem; }
    .check-item .icon { font-size: 1.1rem; }
    .check-item .qlabel { flex: 1; }
    .check-item .qscore { font-weight: 700; }

    /* BP TABLE */
    .bp-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px,1fr)); gap: .8rem; }
    .bp-card { background: var(--surface); border-radius: 10px; padding: 1rem;
               text-align: center; }
    .bp-card .bp-val { font-size: 2rem; font-weight: 800;
                       background: linear-gradient(135deg, var(--accent), var(--accent2));
                       -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .bp-card .bp-label { font-size: 0.8rem; color: var(--muted); margin-top: .2rem; }
    .bp-card .bp-track { background: #2a2d3e; border-radius: 999px; height: 6px; margin-top: .5rem; overflow: hidden; }
    .bp-card .bp-bar   { height: 100%; border-radius: 999px;
                          background: linear-gradient(90deg, var(--accent), var(--accent2)); }

    /* RECS */
    .rec-list { list-style: none; display: grid; gap: .7rem; }
    .rec-item { background: var(--surface); border-radius: 8px; padding: .9rem 1.1rem;
                display: grid; grid-template-columns: auto 1fr; gap: .7rem; align-items: start; }
    .rec-icon { font-size: 1.1rem; }
    .rec-area { font-size: 0.75rem; color: var(--accent); text-transform: uppercase;
                letter-spacing: .05em; margin-bottom: .2rem; }
    .rec-action { font-size: 0.9rem; }

    /* SUMMARY CARDS */
    .summary-card { background: var(--surface); border-radius: 10px; padding: 1.1rem 1.4rem;
                    border: 1px solid #2e3150; font-size: 0.92rem; line-height: 1.7; }

    footer { text-align: center; color: var(--muted); font-size: 0.8rem;
             padding: 2.5rem 0; margin-top: 3rem; border-top: 1px solid #2e3150; }
  </style>
</head>
<body>

<header>
  <div class="container">
    <div class="logo">🔍</div>
    <div class="header-text">
      <h1>Açık Kaynak Kod Müfettişi</h1>
      <p><a href="{{ report.repo_url }}" target="_blank">{{ report.repo_url }}</a></p>
    </div>
    <div class="meta">
      Oluşturulma: {{ generated_at }}<br>
      Model: {{ model_info }}
    </div>
  </div>
</header>

<div class="container">

  <!-- OVERALL SCORE -->
  <div class="score-hero">
    <div class="big-score">
      <div class="number">{{ report.overall_score }}</div>
      <div class="grade">{{ report.grade_letter }}</div>
      <div class="label">{{ report.grade_label }}</div>
    </div>
    <div class="score-bars">
      <div class="bar-row">
        <label>Kod Kalitesi</label>
        <div class="bar-track"><div class="bar-fill quality" style="width:{{ report.code_quality_score }}%"></div></div>
        <span class="bar-val">{{ report.code_quality_score }}/100</span>
      </div>
      <div class="bar-row">
        <label>Güvenlik</label>
        <div class="bar-track"><div class="bar-fill security" style="width:{{ report.security_score }}%"></div></div>
        <span class="bar-val">{{ report.security_score }}/100</span>
      </div>
      <div class="bar-row">
        <label>Dokümantasyon</label>
        <div class="bar-track"><div class="bar-fill docs" style="width:{{ report.docs_score }}%"></div></div>
        <span class="bar-val">{{ report.docs_score }}/100</span>
      </div>
    </div>
  </div>

  <!-- SUMMARIES -->
  <div class="section-title">Genel Değerlendirmeler</div>
  <div class="cards">
    <div class="card">
      <h3>💻 Kod Kalitesi</h3>
      <div class="summary-card">{{ report.code_quality.summary }}</div>
    </div>
    <div class="card">
      <h3>🔒 Güvenlik</h3>
      <div class="summary-card">
        <span class="badge {{ report.security.risk_level }}">{{ report.security.risk_level }}</span><br><br>
        {{ report.security.summary }}
      </div>
    </div>
    <div class="card">
      <h3>📄 Dokümantasyon</h3>
      <div class="summary-card">{{ report.docs.summary }}</div>
    </div>
  </div>

  <!-- TOP ISSUES -->
  {% if report.top_issues %}
  <div class="section-title">Öncelikli Sorunlar</div>
  <ul class="issue-list">
    {% for issue in report.top_issues %}
    <li class="issue-item {{ issue.severity }}">
      <div style="display:flex;align-items:center;gap:.5rem;margin-bottom:.4rem;">
        <span class="badge {{ issue.severity }}">{{ issue.severity }}</span>
        <span style="font-size:.75rem;color:var(--muted)">{{ issue.category }}</span>
      </div>
      {% if issue.file %}<div class="path">{{ issue.file }}</div>{% endif %}
      <div class="desc">{{ issue.description }}</div>
      {% if issue.suggestion %}<div class="suggest">{{ issue.suggestion }}</div>{% endif %}
    </li>
    {% endfor %}
  </ul>
  {% endif %}

  <!-- BEST PRACTICES -->
  {% if report.code_quality.best_practices %}
  <div class="section-title">Kod Kalitesi Detayları</div>
  <div class="bp-grid">
    {% for key, val in report.code_quality.best_practices.items() %}
    <div class="bp-card">
      <div class="bp-val">{{ val }}/10</div>
      <div class="bp-label">{{ key | replace('_', ' ') | title }}</div>
      <div class="bp-track"><div class="bp-bar" style="width:{{ val * 10 }}%"></div></div>
    </div>
    {% endfor %}
  </div>
  {% endif %}

  <!-- SECURITY DETAILS -->
  {% if report.security.vulnerabilities %}
  <div class="section-title">Güvenlik Açıkları</div>
  <ul class="issue-list">
    {% for v in report.security.vulnerabilities %}
    <li class="issue-item {{ v.severity }}">
      <div style="display:flex;align-items:center;gap:.5rem;flex-wrap:wrap;margin-bottom:.4rem;">
        <span class="badge {{ v.severity }}">{{ v.severity }}</span>
        {% if v.owasp_category %}
        <span style="font-size:.75rem;color:var(--muted)">{{ v.owasp_category }}</span>
        {% endif %}
        <span style="font-size:.75rem;font-family:monospace;color:var(--accent2)">{{ v.id }}</span>
      </div>
      {% if v.file %}<div class="path">{{ v.file }}{% if v.line_hint %} · {{ v.line_hint }}{% endif %}</div>{% endif %}
      <div class="desc">{{ v.description }}</div>
      {% if v.exploit_scenario %}<div class="suggest" style="color:#fb923c60">⚠️ {{ v.exploit_scenario }}</div>{% endif %}
      {% if v.remediation %}<div class="suggest">{{ v.remediation }}</div>{% endif %}
    </li>
    {% endfor %}
  </ul>
  {% endif %}

  <!-- DOCS CHECKLIST -->
  <div class="section-title">Açık Kaynak Standartları Kontrol Listesi</div>
  <div class="checklist-grid">
    {% for key, val in report.docs.checklist.items() %}
    <div class="check-item">
      <span class="icon">{{ '✅' if val.present else '❌' }}</span>
      <span class="qlabel">{{ key | replace('_', ' ') | title }}</span>
      {% if val.quality is defined %}
      <span class="qscore" style="color:{{ 'var(--green)' if val.quality >= 7 else ('var(--yellow)' if val.quality >= 4 else 'var(--red)') }}">
        {{ val.quality }}/10
      </span>
      {% endif %}
    </div>
    {% endfor %}
  </div>

  <!-- RECOMMENDATIONS -->
  {% if report.top_recommendations %}
  <div class="section-title">Öncelikli Öneriler</div>
  <ul class="rec-list">
    {% for r in report.top_recommendations %}
    <li class="rec-item">
      <span class="rec-icon">🚀</span>
      <div>
        <div class="rec-area">{{ r.area }}</div>
        <div class="rec-action">{{ r.action }}</div>
      </div>
    </li>
    {% endfor %}
  </ul>
  {% endif %}

  <!-- STRENGTHS -->
  {% if report.code_quality.strengths %}
  <div class="section-title">Güçlü Yönler</div>
  <ul class="rec-list">
    {% for s in report.code_quality.strengths %}
    <li class="rec-item">
      <span class="rec-icon">✨</span>
      <div><div class="rec-action">{{ s }}</div></div>
    </li>
    {% endfor %}
  </ul>
  {% endif %}

</div><!-- /container -->

<footer>
  OS Müfettişi · {{ generated_at }} · Powered by LangChain + LLM
</footer>

</body>
</html>
"""


def generate_html_report(report: FinalReport, output_path: str, model_info: str = "LLM") -> str:
    env = Environment(loader=BaseLoader())
    tmpl = env.from_string(TEMPLATE)

    html = tmpl.render(
        report=report,
        generated_at=datetime.now().strftime("%d.%m.%Y %H:%M"),
        model_info=model_info,
    )

    Path(output_path).write_text(html, encoding="utf-8")
    return output_path
