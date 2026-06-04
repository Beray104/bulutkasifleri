# Teknik Altyapi ve Gelistirme Ortami Kurulumu Raporu

**Proje:** Dagitik Sosyal Medya Analiz Platformu  
**Sorumlu:** Muhammet Eren Mente  
**Gorev:** Teknik altyapi ve gelistirme ortami kurulumu  
**Hafta:** 1  
**Teslim Tarihi:** 10 Mayis 2026 Pazar  
**Rapor Tarihi:** 5 Mayis 2026

## 1. Amac

Bu dokuman, proje icin gerekli temel teknik altyapinin kurulmasi, gelistirme ortamının standart hale getirilmesi ve takim uyelerinin ayni komutlarla projeyi calistirabilmesi icin hazirlanmistir.

Kapsam; Java, Spring Boot, Apache Kafka, Apache Spark, Elasticsearch, SQL veritabani, Docker, IDE ve Git konfigurasyonlarini icerir.

## 2. Kullanilan Teknolojiler

| Bilesen | Teknoloji / Surum | Projedeki Kullanim |
| :--- | :--- | :--- |
| Programlama Dili | Java 25 | Backend uygulamasi ve Spark job kodlari |
| Backend Framework | Spring Boot 3.5.11 | REST API, service katmani ve entegrasyonlar |
| Build Araci | Maven Wrapper | Standart build ve test komutlari |
| Mesaj Kuyrugu | Apache Kafka / Confluent 7.5.0 | `social-media-topic` veri akisi |
| Stream Analizi | Apache Spark 3.5.5 | Structured Streaming ve EMR Spark job |
| Arama Motoru | Elasticsearch 8.15.3 | `social_media_analytics` indeksi |
| SQL Veritabani | MS SQL Server 2022 | Kullanici ve API credential verileri |
| Ek SQL Ortami | PostgreSQL 16 | Alternatif/gelistirme veritabani destegi |
| Konteyner | Docker Compose | Yerel altyapi servislerini tek komutla baslatma |
| Versiyon Kontrol | Git / GitHub | Branch, commit, push ve takim is akisi |

## 3. On Kosullar

Takim uyelerinin asagidaki araclari kurmasi gerekir:

| Arac | Kontrol Komutu |
| :--- | :--- |
| Java JDK | `java -version` |
| Git | `git --version` |
| Docker Desktop | `docker version` |
| Maven Wrapper | `.\mvnw.cmd -version` |
| IDE | IntelliJ IDEA veya Visual Studio Code |

Windows kullanicilari icin komutlar PowerShell uzerinden calistirilmelidir.

## 4. Projeyi Klonlama ve Branch Hazirligi

```powershell
git clone https://github.com/Beray104/bulutkasifleri.git
cd SosyalMedyaAnalizPlatformu
git checkout dev/erenmente
git pull origin dev/erenmente
```

Gelistirme yapmadan once calisma dalinin guncel oldugu dogrulanmalidir:

```powershell
git status
```

## 5. Yerel Servislerin Baslatilmasi

Docker Compose ile MS SQL Server, PostgreSQL, Kafka, Zookeeper ve Elasticsearch servisleri baslatilir:

```powershell
docker compose up -d
```

Servislerin durumunu kontrol etmek icin:

```powershell
docker ps
```

Beklenen temel portlar:

| Servis | Port |
| :--- | :--- |
| MS SQL Server | `1433` |
| PostgreSQL | `5432` |
| Kafka | `9092` |
| Elasticsearch | `9200` |

Elasticsearch saglik kontrolu:

```powershell
curl http://localhost:9200
```

## 6. Veritabani ve Indeks Kurulumu

MS SQL Server veritabani ve Elasticsearch mapping dosyalari `scripts/db` klasoru altinda tutulur.

| Dosya | Gorev |
| :--- | :--- |
| `scripts/db/init-mssql.sql` | `SosyalMedyaAnaliz` veritabani ve temel tablolar |
| `scripts/db/elasticsearch-social-media-analytics-mapping.json` | Elasticsearch indeks mapping ayarlari |
| `scripts/db/init-databases.ps1` | Veritabani baslatma adimlarini otomatiklestiren PowerShell betigi |

Baslatma betigi:

```powershell
.\scripts\db\init-databases.ps1
```

## 7. Spring Boot Uygulamasini Calistirma

Bagimlilikleri derlemek ve testleri calistirmak icin:

```powershell
.\mvnw.cmd test
```

Uygulamayi baslatmak icin:

```powershell
.\mvnw.cmd spring-boot:run
```

Build almak icin:

```powershell
.\mvnw.cmd clean package -DskipTests
```

## 8. Kafka Konfigurasyonu

Kafka yerel ortamda `localhost:9092` uzerinden calisacak sekilde ayarlanmistir. Uygulama sosyal medya mesajlarini `social-media-topic` uzerinden uretip tuketecek sekilde tasarlanmistir.

Topic kontrolu icin Kafka container icinde su komut kullanilabilir:

```powershell
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

## 9. Spark Konfigurasyonu

Spark Structured Streaming is akisi iki sekilde konumlandirilmistir:

| Mod | Sinif | Aciklama |
| :--- | :--- | :--- |
| Local Mode | `SparkStructuredStreamingJob` | Spring Boot icinde yerel test ve gelistirme |
| EMR Mode | `EmrSocialMediaAnalyticsJob` | AWS EMR uzerinde `spark-submit` ile calisan bagimsiz job |

EMR paketi almak icin:

```powershell
.\mvnw.cmd clean package -Pemr -DskipTests
```

## 10. IDE Ayarlari

Takim uyeleri asagidaki IDE ayarlarini kullanmalidir:

| Ayar | Deger |
| :--- | :--- |
| Project SDK | Java 25 |
| Build Tool | Maven |
| Encoding | UTF-8 |
| Line Ending | CRLF veya IDE varsayilani |
| Run Configuration | `SocialMediaAnalysisApplication` |

Kod gelistirme sirasinda Maven Wrapper tercih edilmelidir. Boylece takim uyeleri farkli Maven surumlerinden etkilenmez.

## 11. Git Is Akisi

Temel calisma akisi:

```powershell
git checkout dev/erenmente
git pull origin dev/erenmente
git status
git add <dosya>
git commit -m "Aciklayici commit mesaji"
git push origin dev/erenmente
```

Commit mesajlari yapilan degisikligi acik ve kisa anlatmalidir.

## 12. Dogrulama Kontrol Listesi

| Kontrol | Beklenen Sonuc | Durum |
| :--- | :--- | :--- |
| Java kurulu mu? | `java -version` cikti verir | Tamamlandi |
| Docker servisleri calisiyor mu? | `docker ps` servisleri listeler | Tamamlandi |
| Maven testleri geciyor mu? | `BUILD SUCCESS` gorulur | Tamamlandi |
| Elasticsearch erisilebilir mi? | `http://localhost:9200` yanit verir | Tamamlandi |
| Kafka ayakta mi? | Kafka container calisir | Tamamlandi |
| Spring Boot basliyor mu? | Uygulama hata almadan acilir | Tamamlandi |
| Git branch dogru mu? | `dev/erenmente` uzerinde calisilir | Tamamlandi |

## 13. Sonuc

Proje icin gerekli teknik altyapi ve gelistirme ortami kurulum adimlari standart hale getirilmistir. Java, Spring Boot, Kafka, Spark, Elasticsearch, SQL veritabanlari, Docker ve Git is akisi icin takim tarafindan uygulanabilir bir kurulum rehberi hazirlanmistir.

Bu dokuman ile Hafta 1 teknik altyapi ve gelistirme ortami kurulumu gorevi tamamlanmis kabul edilmistir.
