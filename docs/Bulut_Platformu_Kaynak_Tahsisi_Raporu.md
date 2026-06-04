

☁️ Bulut Platformu Seçimi ve Kaynak Tahsisi Raporu

## Proje Adı: Dağıtık Sosyal Medya Analiz Platformu
## Ekip: Bulut Kaşifleri
Görev: Bulut Platformu Seçimi ve Kaynak Tahsisi (Hafta 1)
## Tarih: 03.05.2026
- Bulut Platformu Değerlendirmesi ve Karşılaştırma
Projenin ihtiyaç duyduğu Apache Kafka, Apache Spark ve Elasticsearch gibi dağıtık sistem
bileşenlerini en verimli şekilde çalıştırmak için üç ana bulut sağlayıcısı (AWS, Azure, GCP) analiz
edilmiştir.
Özellik AWS (Seçilen) Microsoft Azure Google Cloud (GCP)
Büyük Veri Servisleri Amazon MSK (Kafka)
& EMR (Spark)
## Event Hubs &
HDInsight
Pub/Sub & Dataproc
Performans Çok yüksek, küresel
ağ altyapısı.
## Kurumsal
entegrasyonda güçlü.
Veri analitiği ve ML'de
lider.
Maliyet Kullanım başına
ödeme (Yüksek
ölçekte uygun).
Kurumsal lisanslarla
ekonomik.
Genel olarak daha
düşük başlangıç
maliyeti.
Popülerlik En geniş
dokümantasyon ve
topluluk desteği.
Hibrit bulut
çözümlerinde öncü.
Konteyner (K8s)
yönetiminde öncü.

Karar: Projenin dağıtık yapısı ve topluluk desteğinin genişliği nedeniyle AWS (Amazon Web
Services) platformu tercih edilmiştir.
- Kaynak Tahsisi Planı (Resource Allocation)
Projenin ölçeklenebilir ve güvenilir olması için aşağıdaki kaynaklar AWS üzerinde planlanmış ve
tahsis edilmiştir:
A. Hesaplama (Compute)
 API & Ingestion Service: 2 adet t3.medium EC2 örneği (Spring Boot uygulamaları için).
 Veri İşleme (Spark): 1 Master ve 2 Worker node içeren m5.xlarge EMR cluster.
 Mesajlaşma (Kafka): 3 Broker’lı Amazon MSK kurulumu.
B. Depolama (Storage)
 Ham Veri (Raw Data): Amazon S3 bucketları.
 Arama ve Analiz: Amazon OpenSearch (Elasticsearch Service).
C. Ağ (Networking)

 VPC kurulumu ile servislerin izole ağda çalıştırılması.
 Application Load Balancer (ALB) ile trafik yönetimi.
- Güvenlik Önlemleri ve Erişim Kontrolleri
IAM (Identity and Access Management)
 Her ekip üyesine sadece ihtiyacı olan yetkiler tanımlanmıştır.
 Yönetici hesapları için Çok Faktörlü Kimlik Doğrulama (MFA) zorunlu kılınmıştır.
Güvenlik Grupları (Security Groups)
 Kafka ve Spark servisleri sadece VPC içerisinden erişime açılmıştır.
 Dış erişim yalnızca HTTPS (Port 443) üzerinden sağlanmıştır.
## Veri Güvenliği
 At-Rest: S3 ve EBS verileri KMS anahtarları ile şifrelenmiştir.
 In-Transit: Tüm servisler arası iletişim TLS/SSL ile korunmaktadır.
## 4. Sonuç
Hafta 1 kapsamında yapılan bu seçim ve yapılandırma, projenin sonraki aşamalarında (veri toplama,
analiz ve görselleştirme) yüksek performanslı ve güvenli bir zemin oluşturmaktadır. AWS üzerindeki
kaynaklar Auto-Scaling modunda yapılandırılarak maliyet-performans dengesi optimize edilmiştir.

## ☁️ Hazırlayan
## Bulut Kaşifleri Ekibi
## Beray Akar & Ekip Üyeleri