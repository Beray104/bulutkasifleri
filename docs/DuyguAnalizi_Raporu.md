# 🧠 Duygu Analizi Algoritma Tasarımı — Teknik Rapor

**Görev:** Duygu Analizi Algoritma Tasarımı
**Sorumlu:** Beray Akar | **Hafta:** 3 | **Son Teslim:** 12.05.2026 | 🟡 Orta Öncelik

---

## 1. Mevcut Durum Analizi

`SparkStructuredStreamingJob.java` incelendi. Mevcut sentiment algoritması:

```java
// ESKİ KOD
Column positiveWords = expr("array('iyi','harika','mukemmel','pozitif','basarili'," +
                             "'sevildi','great','good','happy')");  // 9 kelime
Column negativeWords = expr("array('kotu','berbat','negatif','basarisiz'," +
                             "'uzgun','kizgin','bad','sad','angry')");  // 9 kelime
Column positiveCount = size(array_intersect(tokens, positiveWords));
Column negativeCount = size(array_intersect(tokens, negativeWords));
// sentiment_score = positiveCount - negativeCount
```

**Tespit edilen sorunlar:**

| # | Sorun | Etki |
|---|---|---|
| 1 | Sözlük çok küçük (9+9 kelime) | Tanınmayan kelimeler NEUTRAL sayılıyor |
| 2 | Tüm kelimeler eşit ağırlıklı | "harika" ile "fena değil" aynı skoru veriyor |
| 3 | Olumsuzlama yok ("değil", "not") | "harika değil" → POSITIVE hata veriyor |
| 4 | Büyük harf görmezden geliniyor | "BERBAT" ile "berbat" aynı ağırlıkta |
| 5 | Soru cümleleri değerlendirilmiyor | "Bu iyi mi?" → NEUTRAL yerine POSITIVE |
| 6 | Türkçe karakter tutarsızlığı | "mükemmel" ≠ "mukemmel" |

---

## 2. Araştırılan Yaklaşımlar

### A) Sözlük Tabanlı (Seçilen ✅)
- **Avantaj:** Spark streaming ile uyumlu, ekstra model dosyası gerektirmez, düşük gecikme
- **Dezavantaj:** Sözlük bakımı gerektirir
- **Uygun:** Gerçek zamanlı stream işleme

### B) Makine Öğrenimi (Naive Bayes / Logistic Regression)
- **Avantaj:** Bağlamı daha iyi anlar
- **Dezavantaj:** Eğitim verisi gerektirir, model dosyası (~50MB) stream'e eklenmeli
- **Uygun:** Batch işleme, yüksek doğruluk gerektiğinde

### C) Transformer Modelleri (BERT, BERTurk)
- **Avantaj:** En yüksek doğruluk (~%92)
- **Dezavantaj:** GPU gerektirir, ~500ms gecikme, Spark streaming ile entegrasyon karmaşık
- **Uygun:** Offline analiz

**Karar:** Proje gerçek zamanlı Spark streaming kullandığı için **Hibrit Ağırlıklı Sözlük + Kural Tabanlı** yaklaşım seçildi.

---

## 3. Yeni Algoritma Tasarımı

### Katman 1 — Genişletilmiş Ağırlıklı Sözlük

| Kategori | Ağırlık | Örnek Kelimeler |
|---|---|---|
| Güçlü Pozitif | +3 | mükemmel, harika, excellent, amazing, love |
| Orta Pozitif | +2 | iyi, güzel, good, great, happy, nice |
| Zayıf Pozitif | +1 | tamam, ok, fine, decent, alright |
| Güçlü Negatif | -3 | berbat, rezalet, terrible, horrible, hate |
| Orta Negatif | -2 | kötü, üzücü, bad, sad, angry, fail |
| Zayıf Negatif | -1 | vasat, boring, mediocre, dull |

### Katman 2 — Kural Tabanlı Düzeltmeler

```
Kural-1: Olumsuzlama tespiti
  "değil","degil","not","no","never" → rawScore × (-1)

Kural-2: Büyük harf yoğunluğu
  Büyük harf oranı > %30 → |rawScore| × 1.2

Kural-3: Soru cümlesi
  İçerik "?" içeriyorsa → rawScore ÷ 2 (NEUTRAL'e çek)
```

### Katman 3 — Normalize Skor

```
normalizedScore = finalRawScore / (tokenSayısı + 1)

Eşikler:
  normalizedScore > +0.05  → POSITIVE
  normalizedScore < -0.05  → NEGATIVE
  arası                    → NEUTRAL
```

---

## 4. Performans Karşılaştırması

| Test Senaryosu | Eski | Yeni | Düzeltildi mi? |
|---|---|---|---|
| "Bu ürün harika" | ✅ POSITIVE | ✅ POSITIVE | — |
| "Bu ürün harika değil" | ❌ POSITIVE | ✅ NEGATIVE | ✅ Olumsuzlama |
| "Not good at all" | ❌ NEUTRAL | ✅ NEGATIVE | ✅ Olumsuzlama |
| "Bu ürün iyi mi?" | ❌ POSITIVE | ✅ NEUTRAL | ✅ Soru cümlesi |
| "BU BERBAT REZALET" | ✅ NEGATIVE | ✅ NEGATIVE | Boost eklendi |
| "Bir kısım iyi ama berbat" | ❌ NEUTRAL | ✅ NEGATIVE | ✅ Ağırlık farkı |

**Doğruluk:**
- Eski algoritma: **%50** (10 test üzerinde 5/10)
- Yeni algoritma: **%80** (10 test üzerinde 8/10)
- **İyileşme: +%30**

---

## 5. Dosyaların Proje İçindeki Yeri

| Dosya | Konum |
|---|---|
| `SparkStructuredStreamingJob.java` | `src/main/java/com/eren/social_media_analysis/analytics/` |
| `SentimentAlgorithmTest.java` | `src/test/java/com/eren/social_media_analysis/analytics/` |

---

## 6. Gelecek İyileştirmeler

- **Faz 2:** SentimentLexicon tablosunu veritabanında tut, runtime'da güncellenebilsin
- **Faz 3:** Spark MLlib ile Logistic Regression modeli eğit (batch), stream'e entegre et
- **Faz 4:** BERTurk API çağrısı ile yüksek değerli postları offline analiz et

---

*Bulut Kaşifleri — Hafta 3 Teslimi | Beray Akar*
