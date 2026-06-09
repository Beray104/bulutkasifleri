# Groq Duygu Analizi API Kurulumu

Canli duygu testi, API anahtarini frontend'e gondermez. Frontend yalnizca
backend'deki asagidaki endpoint'e istek atar:

```text
POST /api/v1/sentiment-analysis
```

## Sunucu environment variable'lari

Zorunlu secret:

```env
GROQ_API_KEY=gsk_...
```

Normal variable:

```env
GROQ_MODEL=llama-3.3-70b-versatile
```

Istege bagli ayarlar:

```env
GROQ_BASE_URL=https://api.groq.com/openai/v1
GROQ_CONNECT_TIMEOUT=5s
GROQ_READ_TIMEOUT=20s
```

`GROQ_API_KEY` GitHub repository variable olarak degil, hosting panelinde
**secret environment variable** olarak tanimlanmalidir. GitHub Actions
kullaniliyorsa `Settings > Secrets and variables > Actions > Secrets`
altinda `GROQ_API_KEY` adiyla eklenmelidir. `GROQ_MODEL` normal variable
olabilir.

Anahtari repository, frontend dosyasi, ekran goruntusu veya acik mesajla
paylasmayin. Sunucu sahibine sifre yoneticisi gibi guvenli bir kanal
uzerinden iletin.

## Ornek istek

```bash
curl -X POST https://API_DOMAIN/api/v1/sentiment-analysis \
  -H "Content-Type: application/json" \
  -d '{"text":"Bu urun harika degil, hic memnun kalmadim."}'
```

Ornek cevap:

```json
{
  "label": "NEGATIVE",
  "confidence": 0.9,
  "explanation": "Metindeki baskin duygu olumsuz.",
  "model": "llama-3.3-70b-versatile"
}
```

Frontend ve backend ayni domain altindaysa frontend otomatik olarak
`/api/v1` yolunu kullanir. Farkli domain kullaniliyorsa sayfa yuklenmeden
once asagidaki global ayar tanimlanabilir:

```html
<script>
  window.APP_CONFIG = {
    API_BASE_URL: "https://api.example.com/api/v1"
  };
</script>
```
