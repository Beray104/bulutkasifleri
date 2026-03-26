# 🏗️ DAĞITIK SOSYAL MEDYA ANALİZ PLATFORMU: TEKNİK MİMARİ VE ENTEGRASYON RAPORU

**Hazırlayan:** Hasan Kara  
**Görev:** Sistem Bileşenleri Entegrasyonu ve Teknik Mimari  
**Teslim Tarihi:** 28 Mart 2026  

---

## 1. STRATEJİK MİMARİ VE HEDEFLER
Bu döküman, sosyal medya platformlarından (Twitter, Instagram, Reddit vb.) elde edilen büyük veri akışını gerçek zamanlı analiz eden dağıtık platformun teknik altyapısını tanımlar. Temel hedef, düşük gecikmeli, hata toleranslı ve ölçeklenebilir bir analiz hattı kurmaktır.

---

## 2. SİSTEM BİLEŞENLERİ VE ENTEGRASYON KATMANLARI

Sistemimiz, verinin kaynağından son kullanıcıya ulaşana kadar geçtiği 5 ana katmandan oluşmaktadır:

### 2.1. Veri Kaynak ve Toplama Katmanı (Ingestion)
* **Veri Kaynakları:** X (Twitter), Instagram, Pinterest ve Reddit gibi platformların resmi API servisleri kullanılır.
* [cite_start]**Entegrasyon:** Her platform için ayrı veri çekme modüllerine sahip Spring Boot tabanlı servisler kullanılır. [cite: 118, 119]
* [cite_start]**Normalizasyon:** Farklı platformlardan gelen ham veriler ortak bir JSON yapısına (platform, içerik, hashtag, zaman damgası) dönüştürülerek standartlaştırılır. [cite: 121, 134]

### 2.2. Veri Akış ve Mesajlaşma Katmanı (Streaming)
* [cite_start]**Teknoloji:** **Apache Kafka** [cite: 138]
* [cite_start]**Entegrasyon:** Veri toplama servisleri (Producer), normalize edilmiş veriyi ilgili Kafka topic'lerine gönderir. [cite: 140, 145]
* [cite_start]**Görev:** Yüksek hacimli veri akışını yönetmek ve sistem bileşenleri arasındaki bağımlılığı (decoupling) azaltarak veri kaybını önlemektir. [cite: 141, 143]

### 2.3. Veri İşleme ve Analiz Katmanı (Processing)
* [cite_start]**Teknoloji:** **Apache Spark (Streaming)** [cite: 153]
* **Entegrasyon:** Spark, Kafka topic'lerine "Subscriber" olarak bağlanarak veriyi saniyeler içinde işler.
* [cite_start]**Analiz İşlevleri:** Gürültü giderme, kelime frekans analizi ve duygu analizi (pozitif, negatif, nötr sınıflandırma) gerçekleştirilir. [cite: 155, 158, 159]

### 2.4. Veri Saklama Katmanı (Storage)
Performans odaklı **hibrit bir depolama** stratejisi izlenir:
* [cite_start]**Operasyonel Veritabanı (PostgreSQL/MySQL):** Kullanıcı ve gönderi gibi yapısal verilerin ilişkisel bütünlüğünü ve kısa süreli saklanmasını sağlar. [cite: 171, 172]
* [cite_start]**Analitik Veritabanı (Elasticsearch):** İşlenmiş analiz sonuçlarını hızlı arama ve dashboard filtreleme işlemleri için uzun süreli saklar. [cite: 181, 182]

### 2.5. Sunum Katmanı (Presentation)
* [cite_start]**Arayüz:** Dashboard üzerinden trend hashtag'ler, duygu analizi sonuçları ve platform karşılaştırmalı istatistikler görselleştirilir. [cite: 191, 195]
* [cite_start]**Kullanıcı Rolleri:** Veri analistleri sonuçları değerlendirirken, sistem yöneticileri veri akışını ve kapasiteyi (ölçeklendirme) kontrol eder. [cite: 66, 68]

---

## 3. TEKNİK PROTOKOLLER VE PORT YÖNETİMİ

| Birim | Protokol | Port | Görev |
| :--- | :--- | :--- | :--- |
| **Kafka Broker** | TCP | 9092 | Dağıtık veri akışı ve kuyruklama |
| **Elasticsearch** | REST API | 9200 | Hızlı veri indeksleme ve arama |
| **Veritabanı** | SQL | 5432/3306 | Yapısal verilerin kalıcı saklanması |

---

## 4. ÖLÇEKLENEBİLİRLİK VE GÜVENLİK
* **Hata Toleransı:** Spark'ın `checkpointLocation` özelliği sayesinde sistem çökmelerinde veri kaybı yaşanmaz.
* **Güvenlik:** Tüm bileşenler Google Cloud VPC (Sanal Özel Ağ) içinde izole edilerek dış erişime kapatılmıştır.
* [cite_start]**Veri Tutma:** Depolama maliyetlerini optimize etmek için ham veriler 7-30 gün arası tutulurken, analiz verileri daha uzun süreli saklanır. [cite: 252, 256]

---

## 📐 5. GENEL SİSTEM MİMARİSİ (ŞEMA)

```mermaid
graph TD
    subgraph "1. Veri Kaynakları"
    API[Sosyal Medya API'leri]
    end

    subgraph "2. Veri Giriş Katmanı (GCP)"
    K[(Apache Kafka)] -- "Port: 9092" --> S{Apache Spark}
    ZK[Yönetici Birim] -. "Eşleşme" .-> K
    end

    subgraph "3. Analiz Katmanı (Dataproc)"
    S -- "Duygu Analizi" --> E[(Veri Deposu)]
    GCS[Yedekleme Alanı] -. "Kaldığı Yerden Devam" .-> S
    end

    subgraph "4. Sunum Katmanı"
    E -- "Port: 9200" --> WA[Web Arayüzü / Panel]
    end

    API --> K

    style K fill:#f96,color:#fff,stroke:#333
    style S fill:#69f,color:#fff,stroke:#333
    style E fill:#7f7,color:#333,stroke:#333
    style WA fill:#f66,color:#fff,stroke:#333
