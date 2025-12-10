CREATE TABLE if not exists idempotency_keys (
    id VARCHAR(255) PRIMARY KEY,
    response_json TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
