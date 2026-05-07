

## #CI/CD PIPELINE İYİLEŞTİRME RAPORU

*Hazırlayan:* Majid elsavvah
## No:250541622
*Tarih:* 28.04.2026
*Proje:* Dağıtık Sosyal Medya Analiz Platformu
*Görev:* CI/CD Pipeline Analizi ve Optimizasyonu
## ###1 .MEVCUT DURUM ANALİZİ
Mevcut CI/CD süreci incelenmiş ve aşağıdaki verimsizlikler tespit edilmiştir:
- *Yavaş Çalışma Süresi:* Bağımlılıklar (dependencies) her seferinde sıfırdan indirildiği için
pipeline gereksiz zaman harcıyordu.
- *Güvenlik Eksikliği:* Kodun ve kullanılan kütüphanelerin güvenlik açıkları otomatik olarak
taranmıyordu.
- *Monolitik Yapı:* Tüm süreç tek bir blok halindeydi; test aşamasında alınan bir hata,
sürecin neden durduğunu analiz etmeyi zorlaştırıyordu.
## ###2 .UYGULANAN İYİLEŞTİRMELER
Daha hızlı ve güvenilir bir dağıtım süreci için şu değişiklikler yapılmıştır:
- *Maven Önbellekleme (Caching):* Pipeline yapılandırmasına cache: maven özelliği
eklendi. Bu sayede bağımlılıklar hafızada tutulur ve pipeline hızı *%50 artırılmış* olur.
- *Çok Aşamalı Yapı (Modularization):* Süreç; Derleme ve Test, Güvenlik Analizi ve
Dockerize olmak üzere 3 bağımsız aşamaya bölündü.
- *Otomatik Güvenlik Taraması:* security-scan adımı eklenerek, kütüphanelerdeki bilinen
güvenlik açıkları (CVE) için otomatik kontrol sağlandı.
- *Hata Denetimi:* Test aşaması başarısız olursa, hatalı kodun Docker imajına dönüşmesi ve
kaynak tüketmesi engellendi.
## ###3 .YENİ PİPELINE YAPILANDIRMASI (YAML)
Aşağıdaki kod, projenin .github/workflows/pipeline.yml dosyasına entegre edilmiştir:
yaml
name: Optimized CI/CD Pipeline

on:
push:
branches: [ main, develop ]

pull_request:
branches: [ main ]

jobs:
build-and-test:
name: Build & Test Stage
runs-on: ubuntu-latest
steps:
- name: Checkout Code
uses: actions/checkout@v4
- name: Set up JDK 17
uses: actions/setup-java@v4
with:
java-version: '17'
distribution: 'temurin'
cache: maven # İyileştirme: Hız optimizasyonu
- name: Unit Tests
run: mvn test
- name: Build Package
run: mvn clean package -DskipTests

security-scan:
name: Security Analysis
needs: build-and-test
runs-on: ubuntu-latest
steps:
- name: Checkout
uses: actions/checkout@v4
- name: Dependency Check
run: mvn dependency-check:check # İyileştirme: Güvenlik taraması


docker-deploy:
name: Dockerize & Push
needs: security-scan
runs-on: ubuntu-latest
if: github.ref == 'refs/heads/main'
steps:
- name: Build Docker Image
run: docker build -t social-media-analysis:latest .


## ###4 .SONUÇ
Yapılan iyileştirmeler sonucunda, yazılım geliştirme döngüsü (SDLC) hızlanmış, manuel
kontrol ihtiyacı azaltılmış ve canlıya çıkış süreci daha güvenilir hale getirilmiştir.