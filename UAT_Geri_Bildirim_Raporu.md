# Kullanici Kabul Testleri (UAT) ve Geri Bildirim Toplama Raporu

**Proje:** Dagitik Sosyal Medya Analiz Platformu  
**Sorumlu:** Muhammet Eren Mente  
**Gorev:** Kullanici Kabul Testleri (UAT) ve geri bildirim toplama  
**Teslim Tarihi:** 9 Mayis 2026 Cumartesi  
**Rapor Tarihi:** 5 Mayis 2026

## 1. Amac

Bu calismanin amaci, gelistirilen sosyal medya analiz platformunun hedef kullanici beklentilerini karsilayip karsilamadigini dogrulamak, temel kullanim akisini test etmek ve kullanici geri bildirimlerine gore gerekli duzeltme aksiyonlarini belirlemektir.

UAT kapsaminda web arayuzu, API erisimleri, analiz sonuclarinin gosterimi, hata durumlari ve temel kullanilabilirlik kontrolleri degerlendirilmistir.

## 2. Test Kapsami

| Alan | Kapsam |
| :--- | :--- |
| Web Arayuzu | Dashboard, trend gorunumu, sentiment gorunumu, veri kartlari ve form davranislari |
| API Entegrasyonu | `/api/v1/trends` ve `/api/v1/sentiments` endpoint'lerinden veri okunmasi |
| Veri Akisi | Kafka, Elasticsearch ve analiz verilerinin arayuze yansima akisi |
| Kullanilabilirlik | Navigasyon, okunabilirlik, hata mesajlari ve mobil gorunum |
| Geri Bildirim | Kullanici yorumlarinin toplanmasi, siniflandirilmasi ve aksiyona donusturulmesi |

## 3. Test Ortami

| Bilesen | Deger |
| :--- | :--- |
| Uygulama | Spring Boot tabanli sosyal medya analiz platformu |
| Frontend | HTML, CSS ve JavaScript tabanli web arayuzu |
| Mesaj Kuyrugu | Kafka `social-media-topic` |
| Arama ve Analiz Deposu | Elasticsearch / OpenSearch uyumlu `social_media_analytics` indeksi |
| Yerel Test Altyapisi | Docker Compose ile calisan destek servisleri |

## 4. Kabul Kriterleri

| Kriter | Beklenen Sonuc |
| :--- | :--- |
| Trend verileri goruntulenebilmeli | Kullanici trend listesini veya grafiklerini okuyabilmeli |
| Duygu analizi goruntulenebilmeli | Pozitif, negatif ve notr dagilimlari anlasilir sekilde sunulmali |
| Bos veri durumlari yonetilmeli | Kullaniciya bozuk ekran yerine acik bir durum mesaji gosterilmeli |
| API hatalari yonetilmeli | Baglanti hatasinda kullanici bilgilendirilmeli |
| Arayuz mobilde kullanilabilir olmali | Temel aksiyonlar kucuk ekranda tasma yapmadan calismali |
| Geri bildirimler kaydedilebilir olmali | Her geri bildirim durum ve aksiyon bilgisiyle takip edilmeli |

## 5. UAT Test Senaryolari

| Test No | Senaryo | Beklenen Sonuc | Durum |
| :--- | :--- | :--- | :--- |
| UAT-01 | Kullanici ana sayfayi acar | Dashboard hatasiz yuklenir ve temel kartlar gorunur | Basarili |
| UAT-02 | Kullanici trend ekranini inceler | En cok gecen hashtag ve anahtar kelimeler listelenir | Basarili |
| UAT-03 | Kullanici sentiment ekranini inceler | Pozitif, negatif ve notr skorlar okunabilir sekilde gosterilir | Basarili |
| UAT-04 | API'den trend verisi cekilir | `/api/v1/trends` endpoint'i frontend tarafindan tuketilebilir | Basarili |
| UAT-05 | API'den sentiment verisi cekilir | `/api/v1/sentiments` endpoint'i frontend tarafindan tuketilebilir | Basarili |
| UAT-06 | Veri bulunmayan durumda ekran acilir | Kullaniciya bos veri mesaji gosterilir | Basarili |
| UAT-07 | Backend veya Elasticsearch gecici olarak erisilemez olur | Kullaniciya teknik olmayan, anlasilir hata mesaji gosterilir | Basarili |
| UAT-08 | Kullanici mobil ekranda dashboard'u acar | Kartlar ve tablolar ekrana sigar, ana aksiyonlar kullanilabilir kalir | Basarili |
| UAT-09 | Kullanici farkli veri kategorilerini karsilastirir | Trend ve sentiment bilgileri ayni veri baglami icinde tutarli gorunur | Basarili |
| UAT-10 | Kullanici geri bildirim verir | Geri bildirim aksiyon tablosuna alinip durum bilgisiyle takip edilir | Basarili |

## 6. Toplanan Geri Bildirimler

| No | Geri Bildirim | Oncelik | Aksiyon | Durum |
| :--- | :--- | :--- | :--- | :--- |
| FB-01 | Sentiment sonuc etiketleri daha anlasilir olmali | Yuksek | Pozitif, negatif ve notr etiketleri netlestirildi | Tamamlandi |
| FB-02 | API hata durumunda ekran bos kaliyor gibi algilaniyor | Yuksek | Kullanici dostu hata mesaji senaryosu kabul kriterlerine eklendi | Tamamlandi |
| FB-03 | Mobil ekranda kartlar daha rahat okunmali | Orta | Mobil gorunum UAT senaryosuna dahil edildi | Tamamlandi |
| FB-04 | Trend verilerinde hashtag bilgisinin ayrica gorunmesi isteniyor | Orta | Trend ciktisinda hashtag odakli gosterim kabul edildi | Tamamlandi |
| FB-05 | Raporlama veya disa aktarma ozelligi talep edildi | Dusuk | Sonraki surum icin gelistirme havuzuna alindi | Planlandi |

## 7. Duzeltme ve Iyilestirme Aksiyonlari

| Aksiyon | Aciklama | Sonuc |
| :--- | :--- | :--- |
| Kullanici hata mesaji kontrolu | API veya veri kaynagi erisilemediginde anlasilir mesaj gosterilmesi dogrulandi | Tamamlandi |
| Trend gorunumu kontrolu | Hashtag ve anahtar kelime odakli analiz sonucunun okunabilirligi test edildi | Tamamlandi |
| Sentiment gorunumu kontrolu | Pozitif, negatif ve notr ayriminin kullanici tarafindan anlasilir oldugu dogrulandi | Tamamlandi |
| Mobil kullanim kontrolu | Dashboard'un kucuk ekranlarda temel islevleri korudugu test edildi | Tamamlandi |
| Geri bildirim takibi | Geri bildirimler oncelik ve durum bilgisiyle siniflandirildi | Tamamlandi |

## 8. Test Ozeti

| Metrik | Sonuc |
| :--- | :--- |
| Toplam UAT senaryosu | 10 |
| Basarili senaryo | 10 |
| Basarisiz senaryo | 0 |
| Toplanan geri bildirim | 5 |
| Tamamlanan geri bildirim aksiyonu | 4 |
| Sonraki surume aktarilan aksiyon | 1 |

## 9. Sonuc

Kullanici kabul testleri sonucunda platformun temel analiz, trend goruntuleme, sentiment goruntuleme ve hata durumu yonetimi beklentilerini karsiladigi degerlendirilmistir. Toplanan geri bildirimler onceliklendirilmis, kritik ve orta seviye aksiyonlar tamamlanmis, raporlama/disa aktarma talebi ise sonraki surum icin planlanmistir.

Bu sonuc ile UAT gorevi tamamlanmis kabul edilmistir.
