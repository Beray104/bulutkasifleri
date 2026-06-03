

## Görev Tamamlama Raporu
 Görev Tamamlama Raporu: Loglama ve İzleme Altyapısı İyileştirmeleri (Hafta 4)

Görev: Loglama ve İzleme Altyapısını İyileştir

## Öncelik:  Yüksek

## Proje: Dağıtık Sosyal Medya Analiz Platformu

## Ekip: Bulut Kaşifleri

 Uygulama Özeti (Özet)

Hafta 3'te yapılan incelemeler sonucunda, sistemin dağıtık mimarisinde (Spring Boot ->
Kafka -> Spark) veri takibi ve hata ayıklama süreçlerinde eksiklikler tespit edilmiştir. Bu
hafta, sistemin güvenilirliğini ve izlenebilirliğini artırmak amacıyla aşağıdaki iyileştirmeler
uygulanmıştır:

- Dağıtık İzleme (Distributed Tracing): Micrometer Tracing entegrasyonu ile her isteğe
benzersiz bir Trace ID atandı.
- Yapılandırılmış Loglama (Structured Logging): Loglar, Elasticsearch ve Logstash
tarafından kolayca indekslenebilmesi için JSON formatına dönüştürüldü.
- Merkezi İzleme ve Metrikler: Spring Boot Actuator kullanılarak sistem sağlığı ve Kafka
performans metrikleri erişilebilir hale getirildi.

 Teknik Değişiklikler ve Kodlar

## 1. Bağımlılıkların Güncellenmesi (pom.xml)


İzleme ve JSON loglama yetenekleri için projeye aşağıdaki kütüphaneler eklenmiştir:

<!-- Sistem sağlığı ve metrik takibi için -->
## <dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-actuator</artifactId>
## </dependency>

<!-- Dağıtık izleme (TraceID & SpanID) için -->
## <dependency>
<groupId>io.micrometer</groupId>
<artifactId>micrometer-tracing-bridge-brave</artifactId>
## </dependency>

<!-- Logların JSON formatında çıktı vermesi için -->
## <dependency>
<groupId>net.logstash.logback</groupId>
<artifactId>logstash-logback-encoder</artifactId>
## <version>7.4</version>
## </dependency>

## 2. Uygulama Yapılandırması (application.yml)

management:
endpoints:

web:
exposure:
include: health, metrics, prometheus, info
endpoint:
health:
show-details: always
tracing:
sampling:
probability: 1.0
metrics:
tags:
application: ${spring.application.name}

## 3. Logback Yapılandırması (src/main/resources/logback-spring.xml)

<?xml version="1.0" encoding="UTF-8"?>
## <configuration>
<appender name="CONSOLE_JSON"
class="ch.qos.logback.core.ConsoleAppender">
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
<customFields>{"team":"Bulut Kasifleri",
"project":"SocialMediaAnalysis"}</customFields>
<includeMdcKeyName>traceId</includeMdcKeyName>
<includeMdcKeyName>spanId</includeMdcKeyName>
## </encoder>
## </appender>


<root level="INFO">
<appender-ref ref="CONSOLE_JSON"/>
## </root>
## </configuration>

- Kod Düzeyinde Loglama Örneği (Java)

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataIngestionService {

public void processData(String dataId, String content) {
log.info("Veri işleme başladı. Veri ID: {}", dataId);

try {
log.info("Veri başarıyla Kafka'ya aktarıldı. Veri ID: {}", dataId);
} catch (Exception e) {
log.error("Veri işlenirken kritik hata! Veri ID: {}, Hata: {}", dataId, e.getMessage(),
e);
## }
## }
## }

## ✅ Elde Edilen Sonuçlar


- Hızlı Hata Tespiti: Dağıtık sistemdeki bir hata, Trace ID sayesinde tüm mikroservisler
boyunca takip edilebilmektedir.
- Görselleştirme Hazırlığı: JSON formatındaki loglar sayesinde Elasticsearch ve Kibana
üzerinde anlık dashboardlar oluşturulabilir hale gelmiştir.
- Sistem Sağlığı: /actuator/health üzerinden sistemin (ve Kafka bağlantısının) ayakta
olup olmadığı anlık olarak izlenebilmektedir.

Hafta 4 iyileştirmeleri tamamlanmış olup, kodlar ana dala (main branch) birleştirilmeye
hazırdır.