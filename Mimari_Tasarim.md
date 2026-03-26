# 🏗️ DAĞITIK SOSYAL MEDYA ANALİZ PLATFORMU: MİMARİ TASARIM DOKÜMANI

**Hazırlayan:** Hasan Kara  
**Görev:** Sistem Bileşenleri Entegrasyonu ve Teknik Mimari  
**Teslim Tarihi:** 28 Mart 2026  

---

## 1. GİRİŞ VE HEDEFLER
Bu doküman, projemizin saniyede binlerce veriyi işleyebilecek dağıtık mimarisini ve Google Cloud Platform (GCP) üzerindeki entegrasyon süreçlerini detaylandırır. Temel hedef; düşük gecikmeli (low-latency), hata toleranslı ve ölçeklenebilir bir analiz hattı kurmaktır.

---

## 2. GENEL SİSTEM MİMARİSİ (DİYAGRAM)

```mermaid
graph TD
    subgraph "1. Veri Kaynakları"
    API[Social Media APIs - X/IG/FB]
    end

    subgraph "2. Veri Ingestion (GCP)"
    K[(Apache Kafka)] -- "Mesaj Kuyruğu" --> S{Apache Spark}
    ZK[Zookeeper] -. "Koordinasyon" .-> K
    end

    subgraph "3. İşlem ve Analiz (Dataproc)"
    S -- "Sentiment Analysis" --> E[(Elasticsearch)]
    GCS[Cloud Storage] -. "Checkpointing" .-> S
    end

    subgraph "4. Sunum ve Visual"
    E --> CR[Cloud Run: API/Frontend]
    CR --> HP[Hoca Paneli / Dashboard]
    end

    API --> K

    style K fill:#f96,color:#fff,stroke:#333

  style S fill:#69f,color:#fff,stroke:#333
    style E fill:#7f7,color:#333,stroke:#333
    style CR fill:#f66,color:#fff,stroke:#333

3. SİSTEM KOMPONENTLERİNİN ENTEGRASYONU VE ÇALIŞMA SÜRECİ
3.1. Veri Toplama ve Kafka Entegrasyonu (Ingestion)
Entegrasyon: Sosyal medya API'lerinden gelen ham veriler, bir Python Producer script'i aracılığıyla çekilir.

Bağlantı: Veriler, Kafka'nın varsayılan 9092 portu üzerinden sisteme basılır.

Çalışma Mantığı: Gelen her veri, bir "Topic" içine JSON objesi olarak asenkron şekilde yazılır.

3.2. Spark ve Dataproc Entegrasyonu (Processing)
Entegrasyon: Spark, Spark-Kafka Connector kullanarak Kafka topic'lerine bağlanır.

Bağlantı: Veri okuma işlemi readStream komutuyla başlatılır.

Çalışma Mantığı: Spark, gelen verileri Memory (Bellek) üzerinde işler ve NLP analizi yapar.

3.3. Elasticsearch ve Cloud Run Entegrasyonu (Output)
Entegrasyon: Analiz edilen veriler, REST API üzerinden Elasticsearch'e gönderilir.

Sistem Çalışması: Cloud Run üzerindeki Frontend, Elasticsearch'e Query DSL sorguları atarak verileri görselleştirir.

4. HATA TOLERANSI VE SİSTEM GÜVENLİĞİ
Entegre Güvenlik: Tüm bileşenler GCP VPC içinde izole edilmiştir.

Resiliency: Spark'ın checkpointLocation özelliği sayesinde veri kaybı yaşanmaz.
