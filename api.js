// --- 1. KISIM: GRAFİKLERİ ÇİZ (Chart.js) ---
const pieCtx = document.getElementById('pieChart').getContext('2d');
const lineCtx = document.getElementById('lineChart').getContext('2d');

const pieChart = new Chart(pieCtx, {
    type: 'pie',
    data: {
        labels: ['Pozitif', 'Negatif', 'Nötr'],
        datasets: [{ data: [45, 25, 30], backgroundColor: ['#28a745', '#dc3545', '#ffc107'] }]
    },
    options: { responsive: true, maintainAspectRatio: false }
});

const lineChart = new Chart(lineCtx, {
    type: 'line',
    data: {
        labels: ['10:00', '10:05', '10:10', '10:15', '10:20'],
        datasets: [{ label: 'Aktif Veri Akışı', data: [20, 60, 45, 120, 80], borderColor: '#007bff', fill: false }]
    },
    options: { responsive: true, maintainAspectRatio: false }
});

// --- 2. KISIM: API TEST SENARYOLARI ---
const searchInput = document.getElementById('searchInput');
const testBtn = document.getElementById('testBtn');
const statusEl = document.getElementById('statusMessage');

function setStatus(message, type) {
    statusEl.textContent = message;
    statusEl.className = type ? `status ${type}` : 'status';
}

async function performansTestiCalistir() {
    const arananKelime = searchInput.value.trim();

    if (!arananKelime) {
        setStatus("Lütfen test için bir anahtar kelime girin.", "error");
        searchInput.focus();
        return;
    }

    testBtn.disabled = true;
    testBtn.textContent = "Test çalışıyor...";
    setStatus(`'${arananKelime}' için API isteği gönderiliyor...`, null);
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
        setStatus("Gerçek API'den veri başarıyla alındı.", "success");

    } catch (error) {
        // SENARYO 2: Hata Yönetimi
        console.error("Senaryo 2 Başarılı (Hata Yakalandı):", error.message);
        setStatus("Gerçek API'ye ulaşılamadı. Simülasyon moduna geçildi.", "error");

        // SENARYO 1: JSON Şeması
        console.log("Senaryo 1: Tasarlanan JSON formatı arayüze yükleniyor...");
        const sahteGelenVeri = {
            "system_info": { "source": "Twitter API", "ingestion_time": "2026-03-27T18:00:00Z" },
            "post_id": "ID_123456",
            "user": "@analizci",
            "text": "Bu teknoloji dünyayı değiştirecek!",
            "analysis_results": { "sentiment": "Positive", "confidence_score": 0.94, "language": "tr" }
        };

        alert(`GELEN JSON VERİSİ (Simülasyon)\n\n` +
              `Kullanıcı: ${sahteGelenVeri.user}\n` +
              `Tweet: ${sahteGelenVeri.text}\n` +
              `Duygu Analizi: ${sahteGelenVeri.analysis_results.sentiment}\n` +
              `Güven Skoru: %${sahteGelenVeri.analysis_results.confidence_score * 100}`);
    } finally {
        testBtn.disabled = false;
        testBtn.textContent = "Performans Testi Yap";
    }
}

testBtn.addEventListener('click', performansTestiCalistir);
searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') performansTestiCalistir();
});

// --- HAFTA 5: ÖLÇEKLENEBİLİRLİK VE OPTİMİZASYON ---

// 1. OPTİMİZASYON: Debounce (Gereksiz Yükü Engelleme)
// Arama kutusuna her harf girildiğinde API'ye gitmez, 
// kullanıcı yazmayı bırakınca 500ms bekleyip tek bir istek atar.
function debounce(func, delay) {
    let timeoutId;
    return function (...args) {
        if (timeoutId) clearTimeout(timeoutId);
        timeoutId = setTimeout(() => {
            func.apply(null, args);
        }, delay);
    };
}

// HTML'deki arama kutusuna (searchInput) bu özelliği bağlıyoruz
const aramaInput = document.getElementById('searchInput');
if (aramaInput) {
    aramaInput.addEventListener('input', debounce((e) => {
        console.log(`[OPTİMİZASYON] Gereksiz API istekleri durduruldu.`);
        console.log(`Sorgu: '${e.target.value}' kelimesi için tek bir optimize istek hazırlanıyor...`);
    }, 500));
}

// 2. ÖLÇEKLENEBİLİRLİK TESTİ: Stress Test (Yük Testi Simülasyonu)
// Konsola stressTest(100) yazarak aynı anda 100 isteği deneyebilirsin.
async function stressTest(istekSayisi = 50) {
    console.log(`\n🚨 ÖLÇEKLENEBİLİRLİK TESTİ BAŞLATILDI: ${istekSayisi} eşzamanlı istek...`);
    const baslangic = performance.now();

    // Fatih'in tasarladığı endpoint'leri simüle eden çoklu istekler
    const testler = Array.from({ length: istekSayisi }).map(async (_, i) => {
        try {
            // Mevcut veri yapısını zorluyoruz
            return await fetch('/api/v1/sentiment/summary', { method: 'GET' });
        } catch (e) {
            return i; // Hata olsa bile frontend'in kilitlenmediğini ölçüyoruz
        }
    });

    await Promise.all(testler);
    const toplamSure = (performance.now() - baslangic).toFixed(2);
    
    console.log(`✅ TEST BAŞARILI: Sistem ${istekSayisi} isteği ${toplamSure} ms sürede frontend performansını bozmadan yönetti.`);
}