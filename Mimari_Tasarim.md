\# 🏗️ DAĞITIK SOSYAL MEDYA ANALİZ PLATFORMU: MİMARİ TASARIM DOKÜMANI



\*\*Hazırlayan:\*\* Hasan Kara  

\*\*Görev:\*\* Sistem Bileşenleri Entegrasyonu ve Teknik Mimari  

\*\*Teslim Tarihi:\*\* 28 Mart 2026   



\---



\## 1. GİRİŞ VE HEDEFLER

Bu doküman, projemizin saniyede binlerce veriyi işleyebilecek dağıtık mimarisini ve Google Cloud Platform (GCP) üzerindeki entegrasyon süreçlerini detaylandırır. Temel hedef; düşük gecikmeli (low-latency), hata toleranslı ve ölçeklenebilir bir analiz hattı kurmaktır.



\---



\## 2. GENEL SİSTEM MİMARİSİ (DİYAGRAM)



```mermaid

graph TD

\&#x20;   subgraph "1. Veri Kaynakları"

\&#x20;   API\\\[Social Media APIs - X/IG/FB]

\&#x20;   end



\&#x20;   subgraph "2. Veri Ingestion (GCP)"

\&#x20;   K\\\[(Apache Kafka)] -- "Mesaj Kuyruğu" --> S{Apache Spark}

\&#x20;   ZK\\\[Zookeeper] -. "Koordinasyon" .-> K

\&#x20;   end



\&#x20;   subgraph "3. İşlem ve Analiz (Dataproc)"

\&#x20;   S -- "Sentiment Analysis" --> E\\\[(Elasticsearch)]

\&#x20;   GCS\\\[Cloud Storage] -. "Checkpointing" .-> S

\&#x20;   end



\&#x20;   subgraph "4. Sunum ve Visual"

E --> CR\\\[Cloud Run: API/Frontend]

\&#x20;   CR --> HP\\\[Hoca Paneli / Dashboard]

\&#x20;   end



\&#x20;   API --> K



\&#x20;   style K fill:#f96,color:#fff,stroke:#333

\&#x20;   style S fill:#69f,color:#fff,stroke:#333

\&#x20;   style E fill:#7f7,color:#333,stroke:#333

\&#x20;   style CR fill:#f66,color:#fff,stroke:#333




\## 3. SİSTEM KOMPONENTLERİNİN ENTEGRASYONU VE ÇALIŞMA SÜRECİ



Sistemimizdeki teknolojiler birbirine şu teknik protokoller ve yöntemlerle entegre edilmiştir:



\### 3.1. Veri Toplama ve Kafka Entegrasyonu (Ingestion)

\* \*\*Entegrasyon:\*\* Sosyal medya API'lerinden (X, Instagram vb.) gelen ham veriler, bir \*\*Python Producer\*\* script'i aracılığıyla çekilir.

\* \*\*Bağlantı:\*\* Veriler, Kafka'nın varsayılan \*\*9092 portu\*\* üzerinden `bootstrap-servers` parametresi kullanılarak sisteme basılır.

\* \*\*Çalışma Mantığı:\*\* Gelen her tweet veya post, bir "Topic" (kanal) içine JSON objesi olarak asenkron şekilde yazılır. Bu sayede veri hızı artsa bile sistem tıkanmaz.



\### 3.2. Spark ve Dataproc Entegrasyonu (Processing)

\* \*\*Entegrasyon:\*\* GCP Dataproc üzerinde kurulu olan Spark, \*\*Spark-Kafka Connector\*\* kütüphanesini kullanarak Kafka topic'lerine "Subscriber" (abone) olarak bağlanır.

\* \*\*Bağlantı:\*\* Veri okuma işlemi `readStream` komutuyla başlatılır ve Kafka'dan gelen veri akışı (offset yönetimi ile) kesintisiz takip edilir.

\* \*\*Çalışma Mantığı:\*\* Spark, gelen verileri \*\*Memory (Bellek)\*\* üzerinde işler. NLP kütüphaneleri her satırı analiz eder ve sonuçları geçici bir veri tablosuna (DataFrame) dönüştürür.



\### 3.3. Elasticsearch ve Cloud Run Entegrasyonu (Output)

\* \*\*Entegrasyon:\*\* Analiz edilen veriler, Spark'ın \*\*Elasticsearch-Hadoop\*\* kütüphanesi kullanılarak Elasticsearch indekslerine "REST API" üzerinden `PUT` isteğiyle gönderilir.

\* \*\*Bağlantı:\*\* Elasticsearch'ün \*\*9200 portu\*\* üzerinden JSON formatında indeksleme yapılır.

\* \*\*Sistem Çalışması:\*\* Cloud Run üzerinde koşan Frontend (React/Vue), Elasticsearch'e \*\*Query DSL\*\* sorguları atarak analiz edilen verileri saniyeler içinde hoca panelinde görselleştirir.



\---



\## 4. HATA TOLERANSI VE SİSTEM GÜVENLİĞİ

\* \*\*Entegre Güvenlik:\*\* Tüm bileşenler \*\*GCP VPC (Sanal Özel Ağ)\*\* içinde birbirine bağlıdır; dışarıdan sadece hoca paneli (Dashboard) erişime açıktır.

\* \*\*Resiliency (Dayanıklılık):\*\* Spark'ın `checkpointLocation` özelliği sayesinde, sistem çökerse GCP Cloud Storage'daki kayıtlara bakarak veri kaybı olmadan kaldığı yerden devam eder.





