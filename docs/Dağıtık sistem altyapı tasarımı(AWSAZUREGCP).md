# ☁️ Dağıtık Sistem Altyapı Tasarımı Raporu (AWS)

## 📋 Proje Bilgileri

- **Proje Adı:** Dağıtık Sosyal Medya Analiz Platformu
- **Ekip:** Bulut Kaşifleri
- **Oluşturan:** BERAY AKAR (250541019@firat.edu.tr)
- **Hafta:** 3
- **Görev:** Dağıtık Sistem Altyapı Tasarımı
- **Kullanılan Teknolojiler:**
  - Java
  - Spring Boot
  - Apache Kafka
  - Apache Spark
  - Elasticsearch
  - AWS Cloud Services

---

# 🎯 Proje Amacı

Bu proje kapsamında Twitter, Facebook ve diğer sosyal medya platformlarından gerçek zamanlı veri toplayan, bu verileri dağıtık sistemler üzerinde işleyen ve analiz eden ölçeklenebilir bir platform geliştirilmektedir.

Platformun temel hedefleri:

- Gerçek zamanlı veri toplama
- Veri ön işleme
- Trend analizi
- Duygu analizi (Sentiment Analysis)
- Web tabanlı görselleştirme
- Büyük veri işleme

---

# ☁️ Bulut Platformu Seçimi

Proje için **AWS (Amazon Web Services)** tercih edilmiştir.

## AWS Seçilme Nedenleri

| Özellik | Açıklama |
|---|---|
| Ölçeklenebilirlik | Auto Scaling desteği |
| Yönetilen Servisler | MSK, EMR, OpenSearch |
| Güvenlik | IAM, Security Groups, KMS |
| Performans | Küresel altyapı ve yüksek erişilebilirlik |
| Topluluk Desteği | Geniş dokümantasyon ve kullanıcı desteği |

---

# 🏗️ Sistem Mimarisi

Sistem aşağıdaki bileşenlerden oluşmaktadır:

```text
Kullanıcılar
    ↓
Application Load Balancer
    ↓
Spring Boot API Sunucuları (EC2)
    ↓
Amazon MSK (Apache Kafka)
    ↓
Apache Spark (EMR Cluster)
    ↓
Amazon OpenSearch / Elasticsearch
    ↓
Web Dashboard & Analytics
```

---

# 🛠️ Kullanılan AWS Servisleri

## 1. EC2 (Elastic Compute Cloud)

Spring Boot tabanlı mikroservislerin çalıştırılması için kullanılmaktadır.

### Yapılandırma
- Instance Type: t3.medium
- İşletim Sistemi: Ubuntu 22.04
- Auto Scaling: Aktif
- Minimum Instance: 2
- Maximum Instance: 6

---

## 2. Amazon MSK (Managed Streaming for Kafka)

Gerçek zamanlı veri akışını yönetmek için kullanılmaktadır.

### Yapılandırma
- Broker Sayısı: 3
- Replication Factor: 3
- Topic Partition: 6
- TLS Encryption: Aktif

### Kullanım Amacı
- Sosyal medya verilerini kuyruğa almak
- Veri akışını dağıtık şekilde yönetmek

---

## 3. Amazon EMR (Apache Spark)

Büyük veri işleme ve analiz işlemleri için kullanılmaktadır.

### Cluster Yapısı
- 1 Master Node
- 2 Worker Node

### Görevleri
- Veri temizleme
- Trend analizi
- Duygu analizi
- Gerçek zamanlı veri işleme

---

## 4. Amazon OpenSearch

Verilerin indekslenmesi ve analiz edilmesi için kullanılmaktadır.

### Özellikler
- Full-text search
- Gerçek zamanlı analiz
- Dashboard desteği

### Kullanım Alanı
- Trend analizi
- Hashtag arama
- Duygu analizi sonuçları

---

## 5. Amazon S3

Ham verilerin depolanması için kullanılmaktadır.

### Avantajları
- Düşük maliyet
- Yüksek dayanıklılık
- Yedekleme desteği

---

# 🔐 Güvenlik Yapılandırması

## IAM (Identity and Access Management)

Her ekip üyesine yalnızca gerekli yetkiler verilmiştir.

### Güvenlik Politikaları
- Least Privilege Principle
- MFA zorunluluğu
- Role-based access control

---

## Security Groups

Sadece gerekli portlar açılmıştır.

| Servis | Port |
|---|---|
| HTTPS | 443 |
| Kafka | 9092 |
| Elasticsearch | 9200 |
| SSH | 22 |

---

## Veri Güvenliği

### At-Rest Encryption
- S3 Encryption
- EBS Encryption
- KMS anahtarları

### In-Transit Encryption
- TLS/SSL
- HTTPS

---

# 📈 Ölçeklenebilirlik (Scalability)

Sistem yüksek kullanıcı yükünü karşılayabilecek şekilde tasarlanmıştır.

## Kullanılan Çözümler

- Auto Scaling Groups
- Load Balancer
- Kafka Partitioning
- Spark Distributed Processing

---

# 💰 Tahmini Maliyet Analizi

| Servis | Aylık Tahmini Maliyet |
|---|---|
| EC2 | 120 USD |
| Amazon MSK | 180 USD |
| EMR Cluster | 200 USD |
| OpenSearch | 100 USD |
| S3 Storage | 40 USD |

### Toplam Tahmini Maliyet
≈ 640 USD / Ay

---

# 📊 İzleme ve Loglama

## Kullanılan Araçlar

- AWS CloudWatch
- Prometheus
- Grafana
- ELK Stack

## Sağlanan Özellikler

- Gerçek zamanlı metrik takibi
- Hata logları
- CPU/RAM analizi
- Kafka monitoring

---

# ✅ Teslim Edilecek Modüller

- Sosyal medya API entegrasyonu
- Veri toplama sistemi
- Veri ön işleme sistemi
- Gerçek zamanlı analiz motoru
- Web dashboard sistemi
- Duygu analizi algoritmaları

---

# 📌 Sonuç

Tasarlanan AWS tabanlı dağıtık sistem altyapısı, yüksek performanslı veri işleme, güvenlik, ölçeklenebilirlik ve gerçek zamanlı analiz ihtiyaçlarını karşılayacak şekilde yapılandırılmıştır.

Bu mimari sayesinde:
- Büyük miktarda veri gerçek zamanlı işlenebilecek,
- Sistem yüksek trafikte ölçeklenebilecek,
- Veriler güvenli şekilde korunabilecek,
- Trend ve duygu analizleri hızlı şekilde gerçekleştirilebilecektir.

---

# 📝 Hazırlayan

**Bulut Kaşifleri Ekibi**  
Majid Alsavah