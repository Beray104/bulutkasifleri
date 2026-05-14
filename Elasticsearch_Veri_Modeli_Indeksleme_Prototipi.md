# Elasticsearch Veri Modeli ve İndeksleme Prototipi

**Proje:** Dağıtık Sosyal Medya Analiz Platformu  
**Hazırlayan:** Fatih Mehmet Albayrak  
**Tarih:** 14 Mayıs 2026

---

## 1. Amaç

Bu doküman, sosyal medya verilerinin Elasticsearch'te nasıl saklanacağını, hangi alanların indeksleneceğini, veri tiplerini, analizör (analyzer) yapılandırmasını ve arama performansını optimize etmek için kullanılan indeksleme yöntemlerini tanımlar.

---

## 2. İndeks Yapısı

Sistem iki ana Elasticsearch indeksi kullanır:

| İndeks Adı | Açıklama | Kullanım |
|---|---|---|
| `social_media_posts` | Sosyal medya postlarını saklar | Spring Data ES + Spark çıktısı |
| `social_media_trends` | Trend konuları ve hashtag'leri saklar | Spark analiz çıktısı |

---

## 3. Veri Modeli — `social_media_posts`

### 3.1. Alan Tanımları ve Veri Tipleri

| Alan | Veri Tipi | İndeksleme | Açıklama |
|---|---|---|---|
| `id` | Auto (ES) | Otomatik | Elasticsearch belge kimliği |
| `externalId` | `keyword` | Tam eşleşme | Platformdaki orijinal post ID'si |
| `platform` | `keyword` | Tam eşleşme | twitter, instagram, facebook vb. |
| `authorUsername` | `keyword` | Tam eşleşme | Paylaşımı yapan kullanıcı adı |
| `content` | `text` (multi-field) | Tam metin arama | Post içeriği (Türkçe + İngilizce analiz) |
| `content.english` | `text` | Tam metin arama | İngilizce analizör ile indekslenen kopya |
| `content.keyword` | `keyword` | Tam eşleşme | Aggregation sorguları için |
| `hashtags` | `keyword` (array) | Tam eşleşme | Hashtag listesi |
| `mentions` | `keyword` (array) | Tam eşleşme | Bahsedilen kullanıcılar |
| `language` | `keyword` | Tam eşleşme | İçerik dili (tr, en vb.) |
| `sentimentScore` | `double` | Sayısal arama | Duygu analizi skoru [-1.0, +1.0] |
| `sentimentLabel` | `keyword` | Tam eşleşme | POSITIVE, NEGATIVE, NEUTRAL |
| `likes` | `integer` | Sayısal arama | Beğeni sayısı |
| `shares` | `integer` | Sayısal arama | Paylaşım sayısı |
| `comments` | `integer` | Sayısal arama | Yorum sayısı |
| `views` | `long` | Sayısal arama | Görüntülenme sayısı |
| `engagementRate` | `float` | Sayısal arama | Etkileşim oranı |
| `mediaType` | `keyword` | Tam eşleşme | text, image, video |
| `isRepost` | `boolean` | Filtreleme | Repost/retweet mi? |
| `publishedAt` | `date` | Tarih aralığı | Orijinal paylaşım zamanı |
| `indexedAt` | `date` | Tarih aralığı | ES'e indekslenme zamanı |

### 3.2. Neden Bu Veri Tipleri Seçildi?

| Veri Tipi | Kullanım Yeri | Gerekçe |
|---|---|---|
| `keyword` | platform, hashtag, kullanıcı adı | Tam eşleşme, gruplama ve filtreleme için optimize |
| `text` | content | Tam metin arama ve dil analizi için tokenize edilir |
| `integer/long` | likes, views | Sayısal sıralama ve range sorguları |
| `double/float` | sentimentScore, engagementRate | Ondalıklı hesaplamalar ve ortalama aggregation'lar |
| `boolean` | isRepost | Basit filtre sorguları |
| `date` | publishedAt, indexedAt | Zaman bazlı sorgular ve histogram aggregation |

---

## 4. İndeksleme Stratejisi

### 4.1. Analizörler (Analyzers)

Sistem iki custom analizör kullanır:

#### Türkçe Analizör (`turkish_analyzer`)
```
Tokenizer: standard
Filters  : lowercase → turkish_stop → turkish_stemmer
```
- **turkish_stop:** Türkçe durma kelimeleri filtrelenir (ve, bir, bu, ile vb.)
- **turkish_stemmer:** Türkçe kök bulma (çalışıyordu → çalış)

#### İngilizce Analizör (`english_analyzer`)
```
Tokenizer: standard
Filters  : lowercase → english_stop → english_stemmer
```
- **english_stop:** İngilizce durma kelimeleri filtrelenir (the, is, at vb.)
- **english_stemmer:** İngilizce kök bulma (running → run)

### 4.2. Multi-Field Strateji

`content` alanı üç farklı şekilde indekslenir:

```
content                → turkish_analyzer ile (ana arama alanı)
content.english        → english_analyzer ile (İngilizce içerik araması)
content.keyword        → keyword olarak (aggregation ve exact match)
```

Bu yapı sayesinde tek bir sorgu ile hem Türkçe hem İngilizce içeriklerde arama yapılabilir:

```json
{
  "query": {
    "multi_match": {
      "query": "yapay zeka",
      "fields": ["content", "content.english"],
      "type": "best_fields"
    }
  }
}
```

---

## 5. Performans Optimizasyonu

### 5.1. Shard ve Replica Stratejisi

| Parametre | Değer | Gerekçe |
|---|---|---|
| `number_of_shards` | 5 | Yüksek hacimli post verisi için paralel sorgu |
| `number_of_replicas` | 1 | Tek node kesintisine karşı dayanıklılık |
| `refresh_interval` | 5s | Gerçek zamanlılığa yakın, performansı korur |

### 5.2. Index Lifecycle Management (ILM)

Eski verilerin otomatik yönetimi için önerilen politika:

```
Hot   (0-7 gün)   → Aktif okuma/yazma, 5 shard
Warm  (7-30 gün)  → Salt okunur, 1 shard'a shrink, force merge
Cold  (30-90 gün) → Freeze, minimum kaynak tüketimi
Delete (90+ gün)  → Otomatik silme
```

### 5.3. Caching

```json
{
  "index.requests.cache.enable": true
}
```
- **Request cache:** Aynı sorgu tekrar gelirse cache'den döner
- **Field data cache:** Aggregation sorguları için
- **Node query cache:** Filter sorguları için

---

## 6. Örnek Sorgular

### 6.1. Son 24 Saatin Trend Hashtag'leri
```json
GET /social_media_posts/_search
{
  "query": { "range": { "publishedAt": { "gte": "now-24h" } } },
  "aggs": {
    "trending_hashtags": { "terms": { "field": "hashtags", "size": 10 } }
  },
  "size": 0
}
```

### 6.2. Platform Bazlı Duygu Analizi Dağılımı
```json
GET /social_media_posts/_search
{
  "aggs": {
    "by_platform": {
      "terms": { "field": "platform" },
      "aggs": {
        "sentiment_dist": { "terms": { "field": "sentimentLabel" } },
        "avg_score": { "avg": { "field": "sentimentScore" } }
      }
    }
  },
  "size": 0
}
```

### 6.3. Türkçe İçerik Arama
```json
GET /social_media_posts/_search
{
  "query": {
    "multi_match": {
      "query": "yapay zeka",
      "fields": ["content", "content.english"],
      "type": "best_fields"
    }
  },
  "highlight": { "fields": { "content": {} } }
}
```

---

## 7. Prototip Dosyaları

| Dosya | Açıklama |
|---|---|
| `src/main/resources/elasticsearch/social-media-posts-settings.json` | Analizör yapılandırması (Spring Data ES tarafından kullanılır) |
| `scripts/db/elasticsearch-social-media-posts-mapping.json` | Manuel indeks oluşturma için tam mapping |
| `SocialMediaPostDocument.java` | Java doküman modeli (@Document, @MultiField, @Setting) |
| `TrendDocument.java` | Trend verisi doküman modeli |

---

## 8. Sonuç

Bu veri modeli ve indeksleme stratejisi:
- Türkçe ve İngilizce içerikleri doğru analizörlerle indeksler
- Multi-field yapı ile hem tam metin arama hem aggregation destekler
- Shard/replica stratejisi ile yüksek hacimli veriyi ölçeklenebilir şekilde yönetir
- ILM politikası ile depolama maliyetini optimize eder
