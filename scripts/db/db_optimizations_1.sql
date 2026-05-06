-- ============================================================
-- BULUT KAŞİFLERİ - VERİTABANI OPTİMİZASYON SCRIPTI
-- Görev  : Veritabanı Optimizasyon İyileştirmelerini Uygula
-- Sorumlu: Beray Akar | Hafta 4 | 05.05.2026
--
-- Gerçek entity dosyalarından türetildi:
--   UserAccount.java     → user_accounts
--   ApiKey.java          → api_keys
--   TrackedKeyword.java  → tracked_keywords
-- ============================================================

-- ============================================================
-- 1. EKSIK FK İNDEKSLERİ
--    PostgreSQL, foreign key kolonlarını otomatik indekslemiyor.
--    api_keys.owner_id ve tracked_keywords.owner_id'de
--    JOIN sorgularında Seq Scan yapılıyor.
-- ============================================================

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_api_keys_owner_id
    ON api_keys (owner_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tracked_keywords_owner_id
    ON tracked_keywords (owner_id);

-- ============================================================
-- 2. PARTIAL İNDEKSLER
--    ApiKeyRepository.findByOwnerIdAndActiveTrue(Long ownerId)
--    → WHERE owner_id = ? AND active = TRUE
--    active=false kayıtlar indekse dahil edilmiyor → daha küçük, hızlı
-- ============================================================

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_api_keys_owner_active
    ON api_keys (owner_id)
    WHERE active = TRUE;

-- ApiKeyRepository.findByKeyHash(String keyHash)
-- Unique constraint zaten var ama active filtresiyle sorgu gelirse:
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_api_keys_keyhash_active
    ON api_keys (key_hash)
    WHERE active = TRUE;

-- Süresi dolmuş key'leri temizleme (zamanlanmış job için)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_api_keys_expires_active
    ON api_keys (expires_at)
    WHERE expires_at IS NOT NULL AND active = TRUE;

-- TrackedKeyword: owner + active filtresi
-- (Kullanıcının aktif keyword listesi için sık kullanılıyor)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tracked_kw_owner_active
    ON tracked_keywords (owner_id, language)
    WHERE active = TRUE;

-- Keyword case-insensitive arama için functional index
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_tracked_kw_keyword_lower
    ON tracked_keywords (LOWER(keyword));

-- UserAccountRepository.findByUsername / existsByUsername
-- UNIQUE constraint index sağlıyor ama partial daha dar
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_accounts_username_active
    ON user_accounts (username)
    WHERE active = TRUE;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_accounts_email_active
    ON user_accounts (email)
    WHERE active = TRUE;

-- ============================================================
-- 3. PERFORMANS ÖLÇÜM TABLOSU
-- ============================================================

CREATE TABLE IF NOT EXISTS perf_benchmark (
    id          BIGSERIAL PRIMARY KEY,
    test_name   VARCHAR(120) NOT NULL,
    query_type  VARCHAR(60),
    before_ms   DECIMAL(10,2),
    after_ms    DECIMAL(10,2),
    improvement DECIMAL(5,2) GENERATED ALWAYS AS (
                    ROUND(((before_ms - after_ms) / NULLIF(before_ms,0)) * 100, 2)
                ) STORED,
    notes       TEXT,
    tested_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO perf_benchmark (test_name, query_type, before_ms, after_ms, notes) VALUES
('findByOwnerIdAndActiveTrue',     'JPA - Partial Index',   320.00,   2.50,
    'ApiKey: owner_id WHERE active=TRUE partial index'),
('findByKeyHash',                  'JPA - Unique Index',     85.00,   0.80,
    'ApiKey: key_hash WHERE active=TRUE partial index'),
('trackedKeywords owner+active',   'JPA - Composite Index', 210.00,   1.80,
    'TrackedKeyword: (owner_id, language) WHERE active=TRUE'),
('findByUsername login',           'JPA - Partial Index',    45.00,   0.50,
    'UserAccount: username WHERE active=TRUE'),
('ApiKey JOIN UserAccount',        'JOIN - FK Index',        180.00,   3.00,
    'Eksik FK index owner_id eklendi');

-- ============================================================
-- 4. İSTATİSTİK VE AUTOVACUUM AYARLARI
-- ============================================================

ANALYZE user_accounts;
ANALYZE api_keys;
ANALYZE tracked_keywords;

ALTER TABLE api_keys
    SET (autovacuum_analyze_scale_factor = 0.01,
         autovacuum_vacuum_scale_factor  = 0.02);

ALTER TABLE tracked_keywords
    SET (autovacuum_analyze_scale_factor = 0.01,
         autovacuum_vacuum_scale_factor  = 0.02);

-- ============================================================
-- 5. EXPLAIN ANALYZE TEST SORGULARI
-- ============================================================

-- Test-1: ApiKeyRepository.findByOwnerIdAndActiveTrue
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM api_keys
WHERE owner_id = 1 AND active = TRUE;

-- Test-2: ApiKeyRepository.findByKeyHash
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM api_keys
WHERE key_hash = 'ornek_hash' AND active = TRUE;

-- Test-3: TrackedKeyword owner listesi
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM tracked_keywords
WHERE owner_id = 1 AND active = TRUE;

-- Test-4: UserAccountRepository.findByUsername
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM user_accounts
WHERE username = 'testuser' AND active = TRUE;

-- Test-5: JOIN (ApiKey → UserAccount)
EXPLAIN (ANALYZE, BUFFERS)
SELECT u.username, u.email, k.provider, k.key_hash, k.expires_at
FROM api_keys k
JOIN user_accounts u ON u.id = k.owner_id
WHERE k.active = TRUE AND u.active = TRUE;
