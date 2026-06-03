// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// Sosyal Medya Analiz Platformu â€” Dashboard API & Grafik MantÄ±ÄŸÄ±
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

const API_BASE = 'http://localhost:8080/api/v1';

// â”€â”€ DOM ReferanslarÄ± â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
const searchInput     = document.getElementById('searchInput');
const searchBtn       = document.getElementById('searchBtn');
const statusEl        = document.getElementById('statusMessage');
const menuToggle      = document.getElementById('menuToggle');
const sidebar         = document.getElementById('sidebar');

// Ã–zet kartlarÄ±
const totalPostsEl    = document.getElementById('totalPosts');
const positiveRateEl  = document.getElementById('positiveRate');
const negativeRateEl  = document.getElementById('negativeRate');
const trendCountEl    = document.getElementById('trendCount');

// â”€â”€ Sidebar Toggle (Mobil) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
if (menuToggle) {
    menuToggle.addEventListener('click', () => {
        sidebar.classList.toggle('open');
    });
}

// â”€â”€ Durum MesajÄ± â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
function setStatus(message, type) {
    statusEl.textContent = message;
    statusEl.className = type ? `status-msg ${type}` : 'status-msg';
}

// â”€â”€ Chart.js YapÄ±landÄ±rma â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
Chart.defaults.color = '#94a3b8';
Chart.defaults.font.family = "'Inter', sans-serif";

// 1. Duygu Analizi Pie Chart
const sentimentPieChart = new Chart(
    document.getElementById('sentimentPieChart').getContext('2d'),
    {
        type: 'doughnut',
        data: {
            labels: ['Pozitif', 'Negatif', 'NÃ¶tr'],
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

// 2. Trend AkÄ±ÅŸÄ± Line Chart
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
                    label: 'NÃ¶tr',
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

// 3. Platform DaÄŸÄ±lÄ±mÄ± Bar Chart
const platformBarChart = new Chart(
    document.getElementById('platformBarChart').getContext('2d'),
    {
        type: 'bar',
        data: {
            labels: ['Twitter', 'Facebook', 'Instagram', 'Reddit'],
            datasets: [{
                label: 'Post SayÄ±sÄ±',
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

// â”€â”€ API Ã‡aÄŸrÄ±larÄ± â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

// Duygu analizi daÄŸÄ±lÄ±mÄ±nÄ± backend'den Ã§ek
async function fetchSentiments() {
    try {
        const res = await fetch(`${API_BASE}/sentiments`);
        if (!res.ok) throw new Error('Backend kapalÄ±');
        const data = await res.json();

        const map = { POSITIVE: 0, NEGATIVE: 0, NEUTRAL: 0 };
        data.forEach(item => { map[item.label] = item.count; });
        const total = map.POSITIVE + map.NEGATIVE + map.NEUTRAL;

        sentimentPieChart.data.datasets[0].data = [map.POSITIVE, map.NEGATIVE, map.NEUTRAL];
        sentimentPieChart.update();

        totalPostsEl.textContent = total.toLocaleString('tr-TR');
        positiveRateEl.textContent = total > 0 ? `%${((map.POSITIVE / total) * 100).toFixed(1)}` : 'â€”';
        negativeRateEl.textContent = total > 0 ? `%${((map.NEGATIVE / total) * 100).toFixed(1)}` : 'â€”';

        setStatus('Duygu analizi verileri baÅŸarÄ±yla yÃ¼klendi.', 'success');
    } catch (e) {
        console.warn('Sentiment API eriÅŸilemedi, simÃ¼lasyon verisi kullanÄ±lÄ±yor:', e.message);
        totalPostsEl.textContent = '1.200';
        positiveRateEl.textContent = '%45';
        negativeRateEl.textContent = '%25';
    }
}

// Trend verilerini backend'den Ã§ek
async function fetchTrends() {
    try {
        const res = await fetch(`${API_BASE}/trends`);
        if (!res.ok) throw new Error('Backend kapalÄ±');
        const data = await res.json();
        trendCountEl.textContent = data.length;
    } catch (e) {
        console.warn('Trends API eriÅŸilemedi, simÃ¼lasyon verisi kullanÄ±lÄ±yor:', e.message);
        trendCountEl.textContent = '5';
    }
}

// Son postlarÄ± backend'den Ã§ek ve tabloya yaz
async function fetchRecentPosts() {
    try {
        const res = await fetch(`${API_BASE}/social-media-posts`);
        if (!res.ok) throw new Error('Backend kapalÄ±');
        const posts = await res.json();
        renderPostsTable(posts);
    } catch (e) {
        console.warn('Posts API eriÅŸilemedi, simÃ¼lasyon verisi kullanÄ±lÄ±yor:', e.message);
        renderPostsTable(SAMPLE_POSTS);
    }
}

// â”€â”€ Tablo Render â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
function renderPostsTable(posts) {
    const tbody = document.getElementById('postsBody');
    tbody.innerHTML = '';
    posts.slice(0, 20).forEach(post => {
        const label = (post.sentimentLabel || 'NEUTRAL').toUpperCase();
        const badgeClass = label === 'POSITIVE' ? 'badge-positive'
                         : label === 'NEGATIVE' ? 'badge-negative'
                         : 'badge-neutral';
        const labelTR = label === 'POSITIVE' ? 'Pozitif'
                      : label === 'NEGATIVE' ? 'Negatif' : 'NÃ¶tr';
        const date = post.publishedAt ? new Date(post.publishedAt).toLocaleString('tr-TR') : 'â€”';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${post.platform || 'â€”'}</td>
            <td>${post.authorUsername || 'â€”'}</td>
            <td>${(post.content || '').substring(0, 80)}${(post.content || '').length > 80 ? 'â€¦' : ''}</td>
            <td><span class="badge ${badgeClass}">${labelTR}</span></td>
            <td>${date}</td>
        `;
        tbody.appendChild(tr);
    });
}

// â”€â”€ SimÃ¼lasyon Verisi â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
const SAMPLE_POSTS = [
    { platform: 'Twitter', authorUsername: '@analizci', content: 'Bu teknoloji harika bir geliÅŸme! Yapay zeka dÃ¼nyayÄ± deÄŸiÅŸtirecek.', sentimentLabel: 'POSITIVE', publishedAt: '2026-05-14T10:30:00Z' },
    { platform: 'Facebook', authorUsername: 'teknoloji_fan', content: 'Yeni gÃ¼ncelleme berbat olmuÅŸ, eski hali Ã§ok daha iyiydi.', sentimentLabel: 'NEGATIVE', publishedAt: '2026-05-14T09:15:00Z' },
    { platform: 'Instagram', authorUsername: 'dijital_guru', content: 'BugÃ¼n bulut teknolojileri Ã¼zerine bir webinar dÃ¼zenledik.', sentimentLabel: 'NEUTRAL', publishedAt: '2026-05-14T08:00:00Z' },
    { platform: 'Twitter', authorUsername: '@veri_bilimci', content: 'Spark Streaming ile gerÃ§ek zamanlÄ± analiz muhteÅŸem Ã§alÄ±ÅŸÄ±yor!', sentimentLabel: 'POSITIVE', publishedAt: '2026-05-14T07:45:00Z' },
    { platform: 'Reddit', authorUsername: 'dev_user42', content: 'Kafka cluster kurulumu dÃ¼ÅŸÃ¼ndÃ¼ÄŸÃ¼mden zor oldu ama sonuÃ§ mÃ¼kemmel.', sentimentLabel: 'POSITIVE', publishedAt: '2026-05-14T06:30:00Z' },
];

// â”€â”€ Arama Ä°ÅŸlevi â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
async function handleSearch() {
    const query = searchInput.value.trim();
    if (!query) { setStatus('LÃ¼tfen bir anahtar kelime girin.', 'error'); return; }
    setStatus(`'${query}' iÃ§in aranÄ±yor...`, '');
    try {
        const res = await fetch(`${API_BASE}/social-media-posts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ platform: 'search', content: query, externalId: 'search-' + Date.now() })
        });
        if (!res.ok) throw new Error('Backend kapalÄ±');
        setStatus(`'${query}' sorgusu baÅŸarÄ±yla gÃ¶nderildi.`, 'success');
    } catch (e) {
        setStatus(`Backend eriÅŸilemedi. SimÃ¼lasyon modunda Ã§alÄ±ÅŸÄ±lÄ±yor.`, 'error');
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

// â”€â”€ Sayfa YÃ¼klendiÄŸinde â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
fetchSentiments();
fetchTrends();
fetchRecentPosts();