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
    // Düzen oturduktan sonra grafiği konteyner boyutuna oturt
    requestAnimationFrame(() => {
        const fit = (chart) => {
            if (!chart || !chart.canvas) return;
            const box = chart.canvas.parentNode;
            if (box.clientWidth > 0) chart.resize(box.clientWidth, box.clientHeight);
        };
        if (name === 'trends')    { fit(trendLineChart); fit(platformBarChart); }
        if (name === 'sentiment') { fit(sentimentPieChart); }
    });
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

// Canlı trend geçmişi (akış grafiği bundan beslenir)
const MAX_TREND_POINTS = 15;
const liveTrend = { labels: [], pos: [], neg: [], neu: [] };

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
                labels: [...liveTrend.labels],
                datasets: [
                    {
                        label: 'Pozitif',
                        data: [...liveTrend.pos],
                        borderColor: '#22c55e',
                        backgroundColor: 'rgba(34,197,94,0.1)',
                        fill: true, tension: 0.4, pointRadius: 3
                    },
                    {
                        label: 'Negatif',
                        data: [...liveTrend.neg],
                        borderColor: '#ef4444',
                        backgroundColor: 'rgba(239,68,68,0.1)',
                        fill: true, tension: 0.4, pointRadius: 3
                    },
                    {
                        label: 'Nötr',
                        data: [...liveTrend.neu],
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
                    data: platformCounts(),
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

// ═══════════════════════════════════════════════════════════
//  ÖZELLİK 1 — Canlı Duygu Analizi (algoritma tarayıcıda)
//  Backend testindeki "yeni algoritma"nın birebir JS karşılığı:
//  ağırlıklı sözlük + kapsamlı (scoped) olumsuzlama + kural tabanlı.
// ═══════════════════════════════════════════════════════════
function analyzeSentiment(content) {
    if (!content || !content.trim()) return { label: null, score: 0 };
    const lower = content.toLowerCase();
    const tokens = lower.replace(/[^\p{L}\p{N} ]/gu, ' ').trim().split(/\s+/).filter(Boolean);

    const posWeights = {};
    ['mükemmel','mukemmel','harika','süper','super','excellent','amazing','outstanding','perfect','wonderful','love'].forEach(w => posWeights[w] = 3);
    ['iyi','güzel','guzel','başarılı','basarili','good','great','happy','nice','cool'].forEach(w => posWeights[w] = 2);
    ['tamam','fena','ok','fine','okay','decent','alright'].forEach(w => posWeights[w] = 1);

    const negWeights = {};
    ['berbat','rezalet','korkunç','korkunc','terrible','horrible','awful','hate','worst'].forEach(w => negWeights[w] = 3);
    ['kötü','kotu','üzücü','uzucu','başarısız','basarisiz','bad','sad','angry','upset','poor','fail'].forEach(w => negWeights[w] = 2);
    ['vasat','sıradan','siradan','boring','dull','mediocre'].forEach(w => negWeights[w] = 1);

    const scores = tokens.map(t => posWeights[t] ? posWeights[t] : (negWeights[t] ? -negWeights[t] : 0));

    const postNeg = new Set(['değil','degil']);
    const preNeg  = new Set(['yok','olmaz','hayır','hayir','hiç','hic','not','no','never']);
    for (let i = 0; i < tokens.length; i++) {
        if (postNeg.has(tokens[i])) {
            for (let j = i - 1; j >= 0 && j >= i - 2; j--) { if (scores[j] !== 0) { scores[j] = -scores[j]; break; } }
        } else if (preNeg.has(tokens[i])) {
            for (let j = i + 1; j < tokens.length && j <= i + 3; j++) { if (scores[j] !== 0) { scores[j] = -scores[j]; break; } }
        }
    }
    let raw = scores.reduce((a, b) => a + b, 0);

    const upper = (content.match(/[A-ZÇĞİÖŞÜ]/g) || []).length;
    if (upper / (content.length + 1) > 0.3) raw = Math.round(raw * 1.2);

    if (content.includes('?') && Math.abs(raw) < 3) return { label: 'NEUTRAL', score: raw };
    if (raw > 0) return { label: 'POSITIVE', score: raw };
    if (raw < 0) return { label: 'NEGATIVE', score: raw };
    return { label: 'NEUTRAL', score: raw };
}

const LABEL_TR = {
    POSITIVE: ['Pozitif', 'badge-positive', '😊'],
    NEGATIVE: ['Negatif', 'badge-negative', '😞'],
    NEUTRAL:  ['Nötr',    'badge-neutral',  '😐']
};

const sentimentInput  = document.getElementById('sentimentInput');
const analyzeBtn      = document.getElementById('analyzeBtn');
const sentimentResult = document.getElementById('sentimentResult');

function renderSentimentResult(text) {
    const { label, score } = analyzeSentiment(text);
    if (!label) { sentimentResult.innerHTML = ''; return; }
    const [tr, cls, emoji] = LABEL_TR[label];
    sentimentResult.innerHTML =
        `<span class="badge ${cls}" style="font-size:0.95rem;padding:6px 16px;">${emoji} ${tr}</span>` +
        `<span class="result-score">skor: ${score}</span>`;
}

if (analyzeBtn && sentimentInput) {
    analyzeBtn.addEventListener('click', () => renderSentimentResult(sentimentInput.value));
    sentimentInput.addEventListener('input', () => renderSentimentResult(sentimentInput.value));
    document.querySelectorAll('.example-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            sentimentInput.value = chip.dataset.text;
            renderSentimentResult(sentimentInput.value);
        });
    });
}

// ═══════════════════════════════════════════════════════════
//  ÖZELLİK 2 — Tema Değiştirici (koyu / açık)
// ═══════════════════════════════════════════════════════════
const themeToggle = document.getElementById('themeToggle');
function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    if (themeToggle) themeToggle.textContent = theme === 'light' ? '☀️' : '🌙';
    localStorage.setItem('bk_theme', theme);
}
if (themeToggle) {
    applyTheme(localStorage.getItem('bk_theme') || 'dark');
    themeToggle.addEventListener('click', () => {
        const next = document.documentElement.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
        applyTheme(next);
    });
}

// ═══════════════════════════════════════════════════════════
//  ÖZELLİK 3 — Canlı Akış Simülasyonu
//  Birkaç saniyede bir yeni post üretir; duygu etiketini yukarıdaki
//  algoritma belirler; özet kartları ve pasta grafik canlı güncellenir.
// ═══════════════════════════════════════════════════════════
const STREAM_PLATFORMS = ['Twitter', 'Facebook', 'Instagram', 'Reddit'];
const STREAM_AUTHORS = ['ahmet_y', 'selin_k', 'baris_d', 'ece_nur', 'kaan_m', 'derya_s', 'tolga_a', 'pelin_u', 'umut_c', 'nazli_t'];
const STREAM_CONTENTS = [
    'Bu uygulama gerçekten harika, çok memnun kaldım!',
    'Yeni güncelleme berbat olmuş, hiç beğenmedim.',
    'Bugün hava bulutlu, toplantı saat 3 te.',
    'Müşteri hizmetleri çok kötü, rezalet bir deneyim.',
    'Ürün kalitesi mükemmel, kesinlikle tavsiye ederim.',
    'Kargo geç geldi ama paketleme güzeldi.',
    'Bu film fena değil, izlenebilir.',
    'Yapay zeka konferansı için kayıtlar açıldı.',
    'Telefonun pili çok kötü, sürekli şarj gerekiyor.',
    'Harika bir gün geçirdik, her şey süperdi!',
    'Sistem yine çöktü, bu kadar başarısız yazılım görmedim.',
    'İyi değil berbat, paramı geri istiyorum.',
    'Veri analizi sonuçları iyi çıktı, ekip başarılı.',
    'Bu kafenin tatlıları muhteşem!',
    'Uygulama iyi mi gerçekten?'
];
const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];

let streamTimer = null;

function refreshSummaryFromPosts() {
    const counts = { POSITIVE: 0, NEGATIVE: 0, NEUTRAL: 0 };
    allPosts.forEach(p => { counts[p.sentimentLabel] = (counts[p.sentimentLabel] || 0) + 1; });
    const total = allPosts.length || 1;
    totalPostsEl.textContent = allPosts.length.toLocaleString('tr-TR');
    positiveRateEl.textContent = `%${((counts.POSITIVE / total) * 100).toFixed(0)}`;
    negativeRateEl.textContent = `%${((counts.NEGATIVE / total) * 100).toFixed(0)}`;
    updateSentimentData([counts.POSITIVE, counts.NEGATIVE, counts.NEUTRAL]);
}

// Platform bazında post sayıları (bar grafik için)
function platformCounts() {
    const c = { Twitter: 0, Facebook: 0, Instagram: 0, Reddit: 0 };
    allPosts.forEach(p => { if (c[p.platform] !== undefined) c[p.platform]++; });
    return [c.Twitter, c.Facebook, c.Instagram, c.Reddit];
}

// Trend geçmişine yeni nokta ekle ve çizgi grafiği canlı güncelle
function recordTrendPoint() {
    const counts = { POSITIVE: 0, NEGATIVE: 0, NEUTRAL: 0 };
    allPosts.forEach(p => { counts[p.sentimentLabel] = (counts[p.sentimentLabel] || 0) + 1; });
    const t = new Date().toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    liveTrend.labels.push(t);
    liveTrend.pos.push(counts.POSITIVE);
    liveTrend.neg.push(counts.NEGATIVE);
    liveTrend.neu.push(counts.NEUTRAL);
    if (liveTrend.labels.length > MAX_TREND_POINTS) {
        liveTrend.labels.shift(); liveTrend.pos.shift(); liveTrend.neg.shift(); liveTrend.neu.shift();
    }
    if (trendLineChart) {
        trendLineChart.data.labels = [...liveTrend.labels];
        trendLineChart.data.datasets[0].data = [...liveTrend.pos];
        trendLineChart.data.datasets[1].data = [...liveTrend.neg];
        trendLineChart.data.datasets[2].data = [...liveTrend.neu];
        trendLineChart.update();
    }
}

// Platform (bar) grafiğini canlı güncelle
function updatePlatformChart() {
    if (!platformBarChart) return;
    platformBarChart.data.datasets[0].data = platformCounts();
    platformBarChart.update();
}

// Başlangıçta trend geçmişini birkaç noktayla doldur (grafik boş açılmasın)
function seedTrendHistory() {
    const counts = { POSITIVE: 0, NEGATIVE: 0, NEUTRAL: 0 };
    allPosts.forEach(p => { counts[p.sentimentLabel] = (counts[p.sentimentLabel] || 0) + 1; });
    const now = Date.now();
    for (let i = 4; i >= 1; i--) {
        const t = new Date(now - i * 4000).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        liveTrend.labels.push(t);
        liveTrend.pos.push(counts.POSITIVE);
        liveTrend.neg.push(counts.NEGATIVE);
        liveTrend.neu.push(counts.NEUTRAL);
    }
}

function pushStreamPost() {
    const content = pick(STREAM_CONTENTS);
    const { label } = analyzeSentiment(content);
    const post = {
        platform: pick(STREAM_PLATFORMS),
        authorUsername: pick(STREAM_AUTHORS),
        content,
        sentimentLabel: label || 'NEUTRAL',
        publishedAt: new Date().toISOString()
    };
    allPosts.unshift(post);
    if (allPosts.length > 60) allPosts.pop();

    // Postlar görünümündeysek ve arama aktif değilse tabloyu tazele
    const onPosts = document.getElementById('view-posts').classList.contains('active');
    if (onPosts && !searchInput.value.trim()) {
        renderPostsTable(allPosts);
        const firstRow = document.querySelector('#postsBody tr');
        if (firstRow) firstRow.classList.add('new-row');
    }
    refreshSummaryFromPosts();
    recordTrendPoint();      // canlı trend çizgisi
    updatePlatformChart();   // canlı platform grafiği
}

function startStream() {
    if (streamTimer) return;
    streamTimer = setInterval(pushStreamPost, 4000);
    liveIndicator.textContent = '● Canlı';
    liveIndicator.classList.remove('paused');
}
function stopStream() {
    clearInterval(streamTimer);
    streamTimer = null;
    liveIndicator.textContent = '⏸ Duraklatıldı';
    liveIndicator.classList.add('paused');
}
if (liveIndicator) {
    liveIndicator.addEventListener('click', () => { streamTimer ? stopStream() : startStream(); });
}

// ─── Sayfa Yüklendiğinde (sıralı) ────────────────────────
(async function init() {
    await fetchSentiments();
    await fetchTrends();
    await fetchRecentPosts();
    seedTrendHistory();   // trend grafiği boş açılmasın
    startStream();        // canlı akışı başlat (durdurmak için "● Canlı"ya tıkla)
})();