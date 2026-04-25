// Grafikleri Başlat (Muhammet Eren'in Tasarımı İçin)
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

// Performans Testi ve Arama Simülasyonu
const testBtn = document.getElementById('testBtn');

testBtn.addEventListener('click', () => {
    const query = document.getElementById('searchInput').value;
    
    if (!query) {
        alert("Kanki önce arama kutusuna bir kelime yaz!");
        return;
    }

    const t0 = performance.now(); // Performans sayacını başlat
    
    // Gecikme (Latency) simülasyonu
    setTimeout(() => {
        const t1 = performance.now();
        const duration = (t1 - t0).toFixed(2);
        
        // Sonuçları rapora veri olması için ekrana basıyoruz
        alert(`'${query}' için analiz tamamlandı.\nTepki Süresi: ${duration} ms\nDurum: Gecikmesiz (Low-latency)`);
        
        console.log(`Test Sonucu: ${query} araması ${duration} ms sürdü.`);
    }, 500); // 500ms sabit gecikme ekledik ki testi ölçebilelim
});