# bulutkasifleri

Dağıtık Sosyal Medya Analiz Platformu
Gereksinim Toplama ve Belgeleme Dokümanı

1. Proje Tanımı
Bu projenin amacı, sosyal medya platformlarından veri toplayarak bu verileri gerçek zamanlı olarak analiz eden bir sistem geliştirmektir. Sistem, sosyal medya paylaşımlarını inceleyerek trend konuları belirleyecek ve paylaşımların duygu analizini yapacaktır. Toplanan veriler dağıtık sistem mimarisi kullanılarak işlenecek ve kullanıcıların erişebileceği bir web arayüzü üzerinden sunulacaktır.

2. Projenin Amacı
Projenin amacı sosyal medya verilerini analiz ederek kullanıcıların güncel trendleri ve sosyal medya üzerindeki duygu durumlarını takip edebilmesini sağlamaktır. Bu sistem sayesinde kullanıcılar sosyal medya platformlarında hangi konuların popüler olduğunu ve paylaşımların genel duygu durumunu görebilecektir.

3. Fonksiyonel Gereksinimler
Sistemin gerçekleştirmesi gereken temel işlevler aşağıda belirtilmiştir:
•	Sistem sosyal medya platformlarından veri toplayabilmelidir.
•	Sistem toplanan verileri gerçek zamanlı olarak analiz edebilmelidir.
•	Sistem sosyal medya paylaşımlarının duygu analizini gerçekleştirebilmelidir.
•	Sistem trend olan konuları belirleyebilmelidir.
•	Kullanıcılar analiz sonuçlarını web arayüzü üzerinden görüntüleyebilmelidir.
•	Kullanıcılar verileri grafik veya tablo şeklinde inceleyebilmelidir.

4. Teknik Gereksinimler
Projenin geliştirilmesi sırasında kullanılacak teknolojiler şunlardır:
•	Programlama dili: Java
•	Backend framework: Spring Boot
•	Veri akışı yönetimi: Apache Kafka
•	Büyük veri analizi: Apache Spark
•	Veri depolama: Elasticsearch
•	Bulut altyapısı: AWS, Azure veya Google Cloud Platform

5. Veri Toplama Süreci
Sistem sosyal medya platformlarının API servisleri aracılığıyla veri toplayacaktır. Toplanan veriler Apache Kafka aracılığıyla veri akışı şeklinde sisteme aktarılacaktır. Daha sonra Apache Spark kullanılarak veriler analiz edilecek ve sonuçlar Elasticsearch veri tabanında saklanacaktır. Kullanıcılar bu verilere web tabanlı arayüz üzerinden erişebilecektir.

6. Sistem Mimarisi
Sistem dağıtık mimari kullanılarak geliştirilecektir. Sistemin temel bileşenleri aşağıdaki gibidir:
1. Veri Toplama Katmanı
Sosyal medya API’lerinden veri çekilir.
2. Veri Akış Katmanı
Apache Kafka kullanılarak veri akışı sağlanır.
3. Veri Analiz Katmanı
Apache Spark ile büyük veri analizi yapılır.
4. Veri Depolama Katmanı
Analiz edilen veriler Elasticsearch veri tabanında saklanır.
5. Sunum Katmanı
Kullanıcılar verileri web tabanlı arayüz üzerinden görüntüler.

9. Sonuç
Bu proje kapsamında sosyal medya platformlarından elde edilen verilerin gerçek zamanlı olarak analiz edilmesi ve kullanıcıya sunulması amaçlanmaktadır. Dağıtık sistem teknolojileri kullanılarak geliştirilecek olan bu platform, büyük veri analizine uygun ölçeklenebilir bir yapı sunacaktır.


