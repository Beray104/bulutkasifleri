// ─────────────────────────────────────────────────────────────────
// Giriş Sayfası Mantığı
//
// NOT: Bu, statik (backend'siz) demo için İSTEMCİ TARAFI bir giriştir.
// Gerçek güvenlik değildir — gerçek kimlik doğrulama sunucu tarafında
// (Spring Security) yapılmalıdır. Burada amaç giriş akışını göstermektir.
// ─────────────────────────────────────────────────────────────────

const VALID_USER = 'admin';
const VALID_PASS = 'admin123';

// Zaten giriş yapılmışsa doğrudan panele git
if (sessionStorage.getItem('bk_auth') === '1') {
    window.location.replace('index.html');
}

const form   = document.getElementById('loginForm');
const errEl  = document.getElementById('loginError');

form.addEventListener('submit', (e) => {
    e.preventDefault();
    const user = document.getElementById('username').value.trim();
    const pass = document.getElementById('password').value;

    if (user === VALID_USER && pass === VALID_PASS) {
        sessionStorage.setItem('bk_auth', '1');
        sessionStorage.setItem('bk_user', user);
        window.location.replace('index.html');
    } else {
        errEl.textContent = 'Kullanıcı adı veya şifre hatalı.';
        errEl.classList.add('show');
    }
});
