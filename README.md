# 📊 Dağıtık Sosyal Medya Analiz Platformu
## Gereksinim Toplama ve Belgeleme Dokümanı

---

## 1. Proje Tanımı
Bu projenin amacı, sosyal medya platformlarından veri toplayarak bu verileri **gerçek zamanlı** olarak analiz eden bir sistem geliştirmektir. Sistem, sosyal medya paylaşımlarını inceleyerek trend konuları belirleyecek ve paylaşımların duygu analizini yapacaktır. Toplanan veriler **dağıtık sistem mimarisi** kullanılarak işlenecek ve bir web arayüzü üzerinden sunulacaktır.

---

## 2. Projenin Amacı
Projenin amacı sosyal medya verilerini analiz ederek kullanıcıların güncel trendleri ve sosyal medya üzerindeki duygu durumlarını takip edebilmesini sağlamaktır. Bu sistem sayesinde kullanıcılar sosyal medya platformlarında hangi konuların popüler olduğunu ve paylaşımların genel duygu durumunu görebilecektir.

---

## 3. Fonksiyonel Gereksinimler
* **Veri Toplama:** Sistem sosyal medya platformlarından veri toplayabilmelidir.
* **Gerçek Zamanlı Analiz:** Sistem toplanan verileri gerçek zamanlı olarak analiz edebilmelidir.
* **Duygu Analizi:** Sistem sosyal medya paylaşımlarının duygu analizini gerçekleştirebilmelidir.
* **Trend Belirleme:** Sistem trend olan konuları belirleyebilmelidir.
* **Web Arayüzü:** Kullanıcılar analiz sonuçlarını web arayüzü üzerinden görüntüleyebilmelidir.
* **Görselleştirme:** Kullanıcılar verileri grafik veya tablo şeklinde inceleyebilmelidir.

---

## 4. Teknik Gereksinimler
| Bileşen | Seçilen Teknoloji |
| :--- | :--- |
| **Programlama Dili** | Java |
| **Backend Framework** | Spring Boot |
| **Veri Akışı Yönetimi** | Apache Kafka |
| **Büyük Veri Analizi** | Apache Spark |
| **Veri Depolama** | Elasticsearch |
| **Bulut Altyapısı** | AWS, Azure veya Google Cloud Platform |

---

## 5. Veri Toplama Süreci
Sistem sosyal medya platformlarının API servisleri aracılığıyla veri toplayacaktır. Toplanan veriler **Apache Kafka** aracılığıyla veri akışı şeklinde sisteme aktarılacaktır. Daha sonra **Apache Spark** kullanılarak veriler analiz edilecek ve sonuçlar **Elasticsearch** veri tabanında saklanacaktır. Kullanıcılar bu verilere web tabanlı arayüz üzerinden erişebilecektir.

---

## 6. Sistem Mimarisi
Sistem dağıtık mimari kullanılarak geliştirilecektir. Sistemin temel bileşenleri aşağıdaki gibidir:
1. **Veri Toplama Katmanı:** Sosyal medya API’lerinden veri çekilir.
2. **Veri Akış Katmanı:** Apache Kafka kullanılarak veri akışı sağlanır.
3. **Veri Analiz Katmanı:** Apache Spark ile büyük veri analizi yapılır.
4. **Veri Depolama Katmanı:** Analiz edilen veriler Elasticsearch veri tabanında saklanır.
5. **Sunum Katmanı:** Kullanıcılar verileri web tabanlı arayüz üzerinden görüntüler.

---

## 7. Sistem Mimarisi Diyagramı

    [Sosyal Medya API'leri]
               │
               ▼
    [Veri Toplama Servisi]
               │
               ▼
        (Apache Kafka)
         (Veri Akışı)
               │
               ▼
        (Apache Spark)
        (Veri Analizi)
               │
               ▼
       (Elasticsearch)
       (Veri Depolama)
               │
               ▼
        [Web Arayüzü]
         (Kullanıcı)

---

## 8. Use Case (Kullanım Senaryosu)
**Aktör:** Kullanıcı

**Senaryolar:**
1. Kullanıcı sisteme giriş yapar.
2. Kullanıcı sosyal medya analiz panelini açar.
3. Sistem sosyal medya verilerini toplar.
4. Sistem verileri analiz eder.
5. Sistem trend konuları belirler.
6. Kullanıcı analiz sonuçlarını grafik veya tablo şeklinde görüntüler.

---

## 9. Görev Dağılımı ve Sorumluluklar
| Sorumlu | Görev Tanımı |
| :--- | :--- |
| **Amine Ceren Yiğit** | Analiz ve Kapsam Yönetimi |
| **Hasan Kara** | Teknoloji Araştırması ve Seçimi |
| **Muhammet Eren Mente** | Teknik Altyapı ve Geliştirme Ortamı |
| **Fatih Mehmet Albayrak** | Versiyon Kontrol ve İş Akış Stratejisi |
| **Beray Akar** | Gereksinim Toplama ve Belgeleme |

---

## 10. Sonuç
Bu proje kapsamında sosyal medya platformlarından elde edilen verilerin gerçek zamanlı olarak analiz edilmesi ve kullanıcıya sunulması amaçlanmaktadır. Dağıtık sistem teknolojileri kullanılarak geliştirilecek olan bu platform, büyük veri analizine uygun ölçeklenebilir bir yapı sunacaktır.
