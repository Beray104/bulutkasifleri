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

// ─── Durum Mesajı ──────────────────────────────────────────
function setStatus(message, type) {
    statusEl.textContent = message;
    statusEl.className = type ? `status-msg ${type}` : 'status-msg';
}

// ─── Chart.js Yapılandırma ──────────────────────────────
Chart.defaults.color = '#94a3b8';
Chart.defaults.font.family = "'Inter', sans-serif";

// 1. Duygu Analizi Pie Chart
const sentimentPieChart = new Chart(
    document.getElementById('sentimentPieChart').getContext('2d'),
    {
        type: 'doughnut',
        data: {
            labels: ['Pozitif', 'Negatif', 'Nötr'],
            datasets: [{
                data: [45, 25, 30],
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

// 2. Trend Akışı Line Chart
const trendLineChart = new Chart(
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

// 3. Platform Dağılımı Bar Chart
const platformBarChart = new Chart(
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

        sentimentPieChart.data.datasets[0].data = [map.POSITIVE, map.NEGATIVE, map.NEUTRAL];
        sentimentPieChart.update();

        totalPostsEl.textContent = total.toLocaleString('tr-TR');
        positiveRateEl.textContent = total > 0 ? `%${((map.POSITIVE / total) * 100).toFixed(1)}` : '—';
        negativeRateEl.textContent = total > 0 ? `%${((map.NEGATIVE / total) * 100).toFixed(1)}` : '—';

        setStatus('Duygu analizi verileri başarıyla yüklendi.', 'success');
    } catch (e) {
        console.warn('Sentiment API erişilemedi, simülasyon verisi kullanılıyor:', e.message);
        totalPostsEl.textContent = '1.200';
        positiveRateEl.textContent = '%45';
        negativeRateEl.textContent = '%25';
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

// ─── Simülasyon Verisi ───────────────────────────────────
const SAMPLE_POSTS = [
    { platform: 'Twitter', authorUsername: '@analizci', content: 'Bu teknoloji harika bir gelişme! Yapay zeka dünyayı değiştirecek.', sentimentLabel: 'POSITIVE', publishedAt: '2026-05-14T10:30:00Z' },
    { platform: 'Facebook', authorUsername: 'teknoloji_fan', content: 'Yeni güncelleme berbat olmuş, eski hali çok daha iyiydi.', sentimentLabel: 'NEGATIVE', publishedAt: '2026-05-14T09:15:00Z' },
    { platform: 'Instagram', authorUsername: 'dijital_guru', content: 'Bugün bulut teknolojileri üzerine bir webinar düzenledik.', sentimentLabel: 'NEUTRAL', publishedAt: '2026-05-14T08:00:00Z' },
    { platform: 'Twitter', authorUsername: '@veri_bilimci', content: 'Spark Streaming ile gerçek zamanlı analiz muhteşem çalışıyor!', sentimentLabel: 'POSITIVE', publishedAt: '2026-05-14T07:45:00Z' },
    { platform: 'Reddit', authorUsername: 'dev_user42', content: 'Kafka cluster kurulumu düşündüğümden zor oldu ama sonuç mükemmel.', sentimentLabel: 'POSITIVE', publishedAt: '2026-05-14T06:30:00Z' },
];

// ─── Arama İşlevi ──────────────────────────────────────────
function handleSearch() {
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
    setStatus(`'${query}' için ${filtered.length} sonuç bulundu.`, 'success');
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