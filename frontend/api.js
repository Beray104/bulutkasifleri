// ─────────────────────────────────────────────────────────────────
// Sosyal Medya Analiz Platformu — Dashboard API & Grafik Mantığı
// ─────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080/api/v1';

// ─── DOM Referansları ──────────────────────────────────────────
const searchInput     = document.getElementById('searchInput');
const searchBtn       = document.getElementById('searchBtn');
const statusEl        = document.getElementById('statusMessage');
const menuToggle      = document.getElementById('menuToggle');
const sidebar         = document.getElementById('sidebar');

// Modal DOM Referansları
const postModal       = document.getElementById('postModal');
const closeModalBtn   = document.getElementById('closeModalBtn');
const modalPostContent= document.getElementById('modalPostContent');
const modalPostMeta   = document.getElementById('modalPostMeta');

// Özet kartları
const totalPostsEl    = document.getElementById('totalPosts');
const positiveRateEl  = document.getElementById('positiveRate');
const negativeRateEl  = document.getElementById('negativeRate');
const trendCountEl    = document.getElementById('trendCount');

// ─── Sidebar Toggle (Mobil) ──────────────────────────────
if (menuToggle) {
    menuToggle.addEventListener('click', () => {
        sidebar.classList.toggle('open');
    });
}

// ─── Görünüm (Sayfa) Değiştirme ──────────────────────────
const navItems = document.querySelectorAll('.nav-item');

function showView(name) {
    // Görünümleri göster/gizle
    document.querySelectorAll('.view').forEach(v => {
        v.classList.toggle('active', v.id === `view-${name}`);
    });
    // Menüde aktif öğeyi işaretle
    navItems.forEach(n => n.classList.toggle('active', n.dataset.view === name));
    // Mobilde menüyü kapat
    sidebar.classList.remove('open');
    // Görünüm artık görünür; grafikleri ilk gösterimde (boyutlu konteynerde) kur
    if (name === 'trends')    ensureTrendCharts();
    if (name === 'sentiment') ensureSentimentChart();
}

navItems.forEach(item => {
    item.addEventListener('click', e => {
        e.preventDefault();
        showView(item.dataset.view);
    });
});

// ─── Çıkış ───────────────────────────────────────────────
const logoutBtn = document.getElementById('logoutBtn');
if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
        sessionStorage.removeItem('bk_auth');
        sessionStorage.removeItem('bk_user');
        window.location.replace('login.html');
    });
}

// ─── Durum Mesajı ──────────────────────────────────────────
function setStatus(message, type) {
    statusEl.textContent = message;
    statusEl.className = type ? `status-msg ${type}` : 'status-msg';
}

// ─── Chart.js Yapılandırma ──────────────────────────────
Chart.defaults.color = '#94a3b8';
Chart.defaults.font.family = "'Inter', sans-serif";

// NOT: Grafikler, ilgili görünüm İLK kez açıldığında oluşturulur.
// Gizli (display:none, 0 boyutlu) bir konteynerde oluşturulan Chart.js
// grafikleri 0x0 boyutta kalıp resize ile düzelmediği için tembel kurulum.
let sentimentPieChart = null;
let trendLineChart = null;
let platformBarChart = null;
let pendingSentimentData = null;   // grafik kurulmadan gelen veri burada bekler

// 1. Duygu Analizi Pie Chart — Duygu Analizi görünümünde
function ensureSentimentChart() {
    if (sentimentPieChart) return;
    sentimentPieChart = new Chart(
        document.getElementById('sentimentPieChart').getContext('2d'),
        {
            type: 'doughnut',
            data: {
                labels: ['Pozitif', 'Negatif', 'Nötr'],
                datasets: [{
                    data: pendingSentimentData || [45, 25, 30],
                    backgroundColor: ['#22c55e', '#ef4444', '#f59e0b'],
                    borderColor: '#1e293b',
                    borderWidth: 3,
                    hoverOffset: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true } }
                },
                cutout: '60%'
            }
        }
    );
}

// 2 + 3. Trend Akışı (line) ve Platform Dağılımı (bar) — Trendler görünümünde
function ensureTrendCharts() {
    if (trendLineChart && platformBarChart) return;
    trendLineChart = new Chart(
        document.getElementById('trendLineChart').getContext('2d'),
        {
            type: 'line',
            data: {
                labels: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '23:59'],
                datasets: [
                    {
                        label: 'Pozitif',
                        data: [20, 35, 60, 80, 95, 70, 55],
                        borderColor: '#22c55e',
                        backgroundColor: 'rgba(34,197,94,0.1)',
                        fill: true, tension: 0.4, pointRadius: 3
                    },
                    {
                        label: 'Negatif',
                        data: [15, 20, 30, 25, 40, 35, 28],
                        borderColor: '#ef4444',
                        backgroundColor: 'rgba(239,68,68,0.1)',
                        fill: true, tension: 0.4, pointRadius: 3
                    },
                    {
                        label: 'Nötr',
                        data: [30, 25, 40, 50, 45, 38, 42],
                        borderColor: '#f59e0b',
                        backgroundColor: 'rgba(245,158,11,0.1)',
                        fill: true, tension: 0.4, pointRadius: 3
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'top', labels: { usePointStyle: true, padding: 12 } } },
                scales: {
                    x: { grid: { color: 'rgba(51,65,85,0.5)' } },
                    y: { grid: { color: 'rgba(51,65,85,0.5)' }, beginAtZero: true }
                }
            }
        }
    );

    platformBarChart = new Chart(
        document.getElementById('platformBarChart').getContext('2d'),
        {
            type: 'bar',
            data: {
                labels: ['Twitter', 'Facebook', 'Instagram', 'Reddit'],
                datasets: [{
                    label: 'Post Sayısı',
                    data: [420, 280, 350, 150],
                    backgroundColor: ['#6366f1', '#3b82f6', '#a855f7', '#f97316'],
                    borderRadius: 6,
                    barThickness: 36
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { grid: { display: false } },
                    y: { grid: { color: 'rgba(51,65,85,0.5)' }, beginAtZero: true }
                }
            }
        }
    );
}

// Duygu dağılımını güncelle — grafik kuruluysa anında, değilse beklet
function updateSentimentData(dataArr) {
    pendingSentimentData = dataArr;
    if (sentimentPieChart) {
        sentimentPieChart.data.datasets[0].data = dataArr;
        sentimentPieChart.update();
    }
}

// ─── API Çağrıları ──────────────────────────────────────────

// Duygu analizi dağılımını backend'den çek
async function fetchSentiments() {
    try {
        const res = await fetch(`${API_BASE}/sentiments`, { cache: 'no-store' });
        if (!res.ok) throw new Error('Backend kapalı');
        const data = await res.json();

        const map = { POSITIVE: 0, NEGATIVE: 0, NEUTRAL: 0 };
        data.forEach(item => { map[item.label] = item.count; });
        const total = map.POSITIVE + map.NEGATIVE + map.NEUTRAL;

        updateSentimentData([map.POSITIVE, map.NEGATIVE, map.NEUTRAL]);

        totalPostsEl.textContent = total.toLocaleString('tr-TR');
        positiveRateEl.textContent = total > 0 ? `%${((map.POSITIVE / total) * 100).toFixed(1)}` : '—';
        negativeRateEl.textContent = total > 0 ? `%${((map.NEGATIVE / total) * 100).toFixed(1)}` : '—';

        setStatus('Duygu analizi verileri başarıyla yüklendi.', 'success');
    } catch (e) {
        console.warn('Sentiment API erişilemedi, simülasyon verisi kullanılıyor:', e.message);
        // Örnek gönderilerden tutarlı dağılım hesapla
        const counts = { POSITIVE: 0, NEGATIVE: 0, NEUTRAL: 0 };
        SAMPLE_POSTS.forEach(p => { counts[p.sentimentLabel] = (counts[p.sentimentLabel] || 0) + 1; });
        const total = SAMPLE_POSTS.length;

        updateSentimentData([counts.POSITIVE, counts.NEGATIVE, counts.NEUTRAL]);

        totalPostsEl.textContent = total.toLocaleString('tr-TR');
        positiveRateEl.textContent = `%${((counts.POSITIVE / total) * 100).toFixed(0)}`;
        negativeRateEl.textContent = `%${((counts.NEGATIVE / total) * 100).toFixed(0)}`;
    }
}

// Trend verilerini backend'den çek
async function fetchTrends() {
    try {
        const res = await fetch(`${API_BASE}/trends`, { cache: 'no-store' });
        if (!res.ok) throw new Error('Backend kapalı');
        const data = await res.json();
        trendCountEl.textContent = data.length;
    } catch (e) {
        console.warn('Trends API erişilemedi, simülasyon verisi kullanılıyor:', e.message);
        trendCountEl.textContent = '5';
    }
}

let allPosts = [];

// Son postları backend'den çek ve tabloya yaz
async function fetchRecentPosts() {
    try {
        const res = await fetch(`${API_BASE}/social-media-posts`, { cache: 'no-store' });
        if (!res.ok) throw new Error('Backend kapalı');
        allPosts = await res.json();
        renderPostsTable(allPosts);
    } catch (e) {
        console.warn('Posts API erişilemedi, simülasyon verisi kullanılıyor:', e.message);
        allPosts = SAMPLE_POSTS;
        renderPostsTable(allPosts);
    }
}

// ─── Tablo Render ──────────────────────────────────────────
function renderPostsTable(posts) {
    const tbody = document.getElementById('postsBody');
    tbody.innerHTML = '';

    if (!posts || posts.length === 0) {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td colspan="5" class="no-results">Sonuç bulunamadı.</td>`;
        tbody.appendChild(tr);
        return;
    }

    posts.slice(0, 20).forEach(post => {
        const label = (post.sentimentLabel || 'NEUTRAL').toUpperCase();
        const badgeClass = label === 'POSITIVE' ? 'badge-positive'
                         : label === 'NEGATIVE' ? 'badge-negative'
                         : 'badge-neutral';
        const labelTR = label === 'POSITIVE' ? 'Pozitif'
                      : label === 'NEGATIVE' ? 'Negatif' : 'Nötr';
        const date = post.publishedAt ? new Date(post.publishedAt).toLocaleString('tr-TR') : '—';
        
        const contentStr = (post.content || '');
        const truncated = contentStr.substring(0, 80) + (contentStr.length > 80 ? '…' : '');

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${post.platform || '—'}</td>
            <td>${post.authorUsername || '—'}</td>
            <td class="clickable-text" title="Tamamını okumak için tıklayın">${truncated}</td>
            <td><span class="badge ${badgeClass}">${labelTR}</span></td>
            <td>${date}</td>
        `;
        
        // Tıklayınca modalı aç
        tr.children[2].addEventListener('click', () => showPostModal(post));
        
        tbody.appendChild(tr);
    });
}

// ─── Modal İşlevleri ───────────────────────────────────────
function showPostModal(post) {
    modalPostContent.textContent = post.content || 'İçerik yok';
    modalPostMeta.innerHTML = `
        <span><b>Platform:</b> ${post.platform || '—'}</span> | 
        <span><b>Yazar:</b> ${post.authorUsername || '—'}</span> | 
        <span><b>Tarih:</b> ${post.publishedAt ? new Date(post.publishedAt).toLocaleString('tr-TR') : '—'}</span>
    `;
    postModal.style.display = 'block';
}

closeModalBtn.addEventListener('click', () => {
    postModal.style.display = 'none';
});

window.addEventListener('click', (e) => {
    if (e.target === postModal) {
        postModal.style.display = 'none';
    }
});

// ─── Simülasyon Verisi (backend kapalıyken kullanılır) ───────────
const SAMPLE_POSTS = [
    { platform: 'Twitter',   authorUsername: 'ayse_yilmaz',   content: 'Bu teknoloji harika bir gelişme! Yapay zeka dünyayı değiştirecek.', sentimentLabel: 'POSITIVE', publishedAt: '2026-06-07T10:30:00Z' },
    { platform: 'Facebook',  authorUsername: 'mehmet_kaya',   content: 'Yeni güncelleme berbat olmuş, eski hali çok daha iyiydi.', sentimentLabel: 'NEGATIVE', publishedAt: '2026-06-07T09:15:00Z' },
    { platform: 'Instagram', authorUsername: 'zeynep_demir',  content: 'Bugün bulut teknolojileri üzerine bir webinar düzenledik.', sentimentLabel: 'NEUTRAL',  publishedAt: '2026-06-07T08:00:00Z' },
    { platform: 'Twitter',   authorUsername: 'can_ozturk',    content: 'Spark Streaming ile gerçek zamanlı analiz muhteşem çalışıyor!', sentimentLabel: 'POSITIVE', publishedAt: '2026-06-07T07:45:00Z' },
    { platform: 'Reddit',    authorUsername: 'dev_user42',    content: 'Kafka cluster kurulumu düşündüğümden zor oldu ama sonuç mükemmel.', sentimentLabel: 'POSITIVE', publishedAt: '2026-06-07T06:30:00Z' },
    { platform: 'Twitter',   authorUsername: 'elif_sahin',    content: 'Müşteri hizmetleri çok kötü, saatlerce bekledim hiç çözüm yok.', sentimentLabel: 'NEGATIVE', publishedAt: '2026-06-06T22:10:00Z' },
    { platform: 'Instagram', authorUsername: 'burak_aydin',   content: 'Yeni telefon kamerası gerçekten güzel, fotoğraflar net çıkıyor.', sentimentLabel: 'POSITIVE', publishedAt: '2026-06-06T20:05:00Z' },
    { platform: 'Facebook',  authorUsername: 'fatma_celik',   content: 'Toplantı saat 15:00 te yapılacak, herkes katılsın lütfen.', sentimentLabel: 'NEUTRAL',  publishedAt: '2026-06-06T18:40:00Z' },
    { platform: 'Reddit',    authorUsername: 'serkan_yildiz', content: 'Bu oyun rezalet, paramın karşılığını alamadım kesinlikle önermem.', sentimentLabel: 'NEGATIVE', publishedAt: '2026-06-06T16:25:00Z' },
    { platform: 'Twitter',   authorUsername: 'deniz_arslan',  content: 'Hava bugün bulutlu, yağmur bekleniyormuş öğleden sonra.', sentimentLabel: 'NEUTRAL',  publishedAt: '2026-06-06T14:00:00Z' },
    { platform: 'Instagram', authorUsername: 'merve_dogan',   content: 'Bu kafenin tatlıları muhteşem, kesinlikle tekrar geleceğim!', sentimentLabel: 'POSITIVE', publishedAt: '2026-06-06T12:30:00Z' },
    { platform: 'Twitter',   authorUsername: 'emre_koc',      content: 'Uygulama sürekli çöküyor, bu kadar hatalı yazılım görmedim.', sentimentLabel: 'NEGATIVE', publishedAt: '2026-06-06T11:15:00Z' },
    { platform: 'Reddit',    authorUsername: 'gizem_polat',   content: 'Veri analizi sonuçları beklediğimden iyi çıktı, ekip başarılı.', sentimentLabel: 'POSITIVE', publishedAt: '2026-06-05T21:50:00Z' },
    { platform: 'Facebook',  authorUsername: 'okan_tas',      content: 'Kargo bugün gelecekti ama hâlâ ortada yok, takip numarası çalışmıyor.', sentimentLabel: 'NEGATIVE', publishedAt: '2026-06-05T19:20:00Z' },
    { platform: 'Twitter',   authorUsername: 'sıla_aksoy',    content: 'Yapay zeka konferansı için kayıtlar başladı, program açıklandı.', sentimentLabel: 'NEUTRAL',  publishedAt: '2026-06-05T15:10:00Z' },
    { platform: 'Instagram', authorUsername: 'kerem_balci',   content: 'Yeni sezon harika başladı, ekip muhteşem oynuyor bu yıl şampiyonluk bizim!', sentimentLabel: 'POSITIVE', publishedAt: '2026-06-05T13:00:00Z' },
];

// ─── Arama İşlevi ──────────────────────────────────────────
function handleSearch() {
    // Arama her zaman "Son Postlar" görünümünde sonuç gösterir
    showView('posts');

    const query = searchInput.value.trim().toLowerCase();
    if (!query) {
        renderPostsTable(allPosts);
        setStatus('Tüm gönderiler listeleniyor.', 'success');
        return;
    }

    const filtered = allPosts.filter(post =>
        (post.content && post.content.toLowerCase().includes(query)) ||
        (post.authorUsername && post.authorUsername.toLowerCase().includes(query)) ||
        (post.platform && post.platform.toLowerCase().includes(query))
    );

    renderPostsTable(filtered);
    if (filtered.length > 0) {
        setStatus(`'${query}' için ${filtered.length} sonuç bulundu.`, 'success');
    } else {
        setStatus(`'${query}' için sonuç bulunamadı.`, 'error');
    }
}

let searchTimeout = null;

searchBtn.addEventListener('click', handleSearch);
searchInput.addEventListener('input', e => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        if (searchInput.value.trim()) {
            handleSearch();
        }
    }, 300);
});
searchInput.addEventListener('keydown', e => { 
    if (e.key === 'Enter') {
        clearTimeout(searchTimeout);
        handleSearch();
    }
});

// ─── Sayfa Yüklendiğinde ─────────────────────────────────
fetchSentiments();
fetchTrends();
fetchRecentPosts();