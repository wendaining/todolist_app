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

CREATE TABLE IF NOT EXISTS tasks (
    id TEXT NOT NULL,
    token_key TEXT NOT NULL,
    title TEXT NOT NULL,
    status TEXT NOT NULL,
    priority TEXT NOT NULL,
    due_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    completed_at TEXT,
    PRIMARY KEY (token_key, id)
);

CREATE INDEX IF NOT EXISTS idx_tasks_token_key ON tasks(token_key);
CREATE INDEX IF NOT EXISTS idx_tasks_updated_at ON tasks(token_key, updated_at);
