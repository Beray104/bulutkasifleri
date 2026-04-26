// --- 1. KISIM: GRAFİKLERİ ÇİZ (Chart.js) ---
const pieCtx = document.getElementById('pieChart').getContext('2d');
const lineCtx = document.getElementById('lineChart').getContext('2d');

const pieChart = new Chart(pieCtx, {
    type: 'pie',
    data: { 
        labels: ['Pozitif', 'Negatif', 'Nötr'], 
        datasets: [{ data: [45, 25, 30], backgroundColor: ['#28a745', '#dc3545', '#ffc107'] }] 
    }
});

const lineChart = new Chart(lineCtx, {
    type: 'line',
    data: { 
        labels: ['10:00', '10:05', '10:10', '10:15', '10:20'], 
        datasets: [{ label: 'Aktif Veri Akışı', data: [20, 60, 45, 120, 80], borderColor: '#007bff', fill: false }] 
    }
});

// --- 2. KISIM: API TEST SENARYOLARI ---
document.getElementById('testBtn').addEventListener('click', async () => {
    const arananKelime = document.getElementById('searchInput').value;
    
    if (!arananKelime) {
        alert("Kanka test yapmak için kutuya bir kelime yaz!");
        return;
    }

    console.log(`TEST BAŞLADI: '${arananKelime}' kelimesi için API'ye istek atılıyor...`);

    try {
        // SENARYO 3: Veri gönderme testi
        console.log("Senaryo 3: /api/v1/collector/task adresine POST isteği deneniyor...");
        const response = await fetch('/api/v1/collector/task', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ keyword: arananKelime })
        });

        if (!response.ok) throw new Error("Arka plan sunucusu (Backend) şu an kapalı.");
        const data = await response.json();
        console.log("Gerçek Sunucudan Gelen Veri:", data);

    } catch (error) {
        // SENARYO 2: Hata Yönetimi
        console.error("Senaryo 2 Başarılı (Hata Yakalandı):", error.message);
        alert(`SİSTEM UYARISI: Gerçek API sunucusuna şu an ulaşılamıyor.\n\nSimülasyon Moduna Geçiliyor...`);

        // SENARYO 1: JSON Şeması
        console.log("Senaryo 1: Fatih'in tasarladığı JSON formatı arayüze yükleniyor...");
        const sahteGelenVeri = {
            "system_info": { "source": "Twitter API", "ingestion_time": "2026-03-27T18:00:00Z" },
            "post_id": "ID_123456",
            "user": "@analizci",
            "text": "Bu teknoloji dünyayı değiştirecek!",
            "analysis_results": { "sentiment": "Positive", "confidence_score": 0.94, "language": "tr" }
        };

        alert(`GELEN JSON VERİSİ (Senaryo 1)\n\n` +
              `Kullanıcı: ${sahteGelenVeri.user}\n` +
              `Tweet: ${sahteGelenVeri.text}\n` +
              `Duygu Analizi: ${sahteGelenVeri.analysis_results.sentiment}\n` +
              `Güven Skoru: %${sahteGelenVeri.analysis_results.confidence_score * 100}`);
    }
});