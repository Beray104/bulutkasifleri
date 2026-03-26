# 🏗️ DAĞITIK SOSYAL MEDYA ANALİZ PLATFORMU: MİMARİ TASARIM DOKÜMANI

**Hazırlayan:** Hasan Kara  
**Görev:** Sistem Bileşenleri Entegrasyonu ve Teknik Mimari  
**Teslim Tarihi:** 28 Mart 2026  

---

## 1. GİRİŞ VE HEDEFLER
Bu doküman, projemizin saniyede binlerce veriyi işleyebilecek dağıtık mimarisini ve Google Cloud Platform (GCP) üzerindeki entegrasyon süreçlerini detaylandırır. Temel hedef; düşük gecikmeli, hata toleranslı ve ölçeklenebilir bir analiz hattı kurmaktır.

---

## 2. SİSTEM KOMPONENTLERİNİN ENTEGRASYONU VE ÇALIŞMA SÜRECİ

Sistemimizdeki teknolojiler birbirine şu teknik protokoller ve yöntemlerle entegre edilmiştir:

### 2.1. Veri Toplama ve Kafka Entegrasyonu (Ingestion)
* **Entegrasyon:** Sosyal medya API'lerinden gelen ham veriler, bir **Python Producer** script'i aracılığıyla çekilir.
* **Bağlantı:** Veriler, Kafka'nın varsayılan **9092 portu** üzerinden sisteme basılır.
* **Çalışma Mantığı:** Gelen her veri, bir "Topic" içine JSON objesi olarak asenkron şekilde yazılır. Bu sayede veri hızı artsa bile sistem tıkanmaz.

### 2.2. Spark ve Dataproc Entegrasyonu (Processing)
* **Entegrasyon:** GCP Dataproc üzerinde kurulu olan Spark, Kafka topic'lerine "Subscriber" (abone) olarak bağlanır.
* **Bağlantı:** Veri okuma işlemi kesintisiz takip edilir (offset yönetimi ile).
* **Çalışma Mantığı:** Spark, gelen verileri **Memory (Bellek)** üzerinde işler. NLP kütüphaneleri her satırı analiz eder ve sonuçları geçici bir veri tablosuna dönüştürür.

### 2.3. Elasticsearch ve Cloud Run Entegrasyonu (Output)
* **Entegrasyon:** Analiz edilen veriler, Spark'ın **Elasticsearch-Hadoop** kütüphanesi kullanılarak Elasticsearch indekslerine "REST API" üzerinden gönderilir.
* **Bağlantı:** Elasticsearch'ün **9200 portu** üzerinden JSON formatında indeksleme yapılır.
* **Sistem Çalışması:** Cloud Run üzerinde koşan Frontend (React/Vue), Elasticsearch'e sorgular atarak analiz edilen verileri saniyeler içinde görselleştirir.

---

## 3. HATA TOLERANSI VE SİSTEM GÜVENLİĞİ
* **Entegre Güvenlik:** Tüm bileşenler **GCP VPC** içinde izole edilmiştir.
* **Resiliency:** Spark'ın `checkpointLocation` özelliği sayesinde sistem çökerse veri kaybı yaşanmadan devam eder.

---

## 📐 4. GENEL SİSTEM MİMARİSİ (ŞEMA)

```mermaid
graph TD
    subgraph "1. Veri Kaynakları"
    API[Sosyal Medya Verileri]
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
    style S fill:#69f,color:#fff,stroke:#333
    style E fill:#7f7,color:#333,stroke:#333
    style WA fill:#f66,color:#fff,stroke:#333
