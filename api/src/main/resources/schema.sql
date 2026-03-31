CREATE TABLE IF NOT EXISTS security_tokens (
    token_hash TEXT PRIMARY KEY,
    created_at TEXT NOT NULL,
    expires_at TEXT NOT NULL,
    revoked_at TEXT
);

CREATE TABLE IF NOT EXISTS security_rate_windows (
    token_hash TEXT NOT NULL,
    window_minute INTEGER NOT NULL,
    request_count INTEGER NOT NULL,
    PRIMARY KEY (token_hash, window_minute)
);
