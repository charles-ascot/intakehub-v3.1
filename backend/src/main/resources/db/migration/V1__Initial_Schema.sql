-- Providers registry
CREATE TABLE providers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    base_url VARCHAR(1024) NOT NULL,
    auth_type VARCHAR(50) NOT NULL,
    rate_limit_requests INT,
    rate_limit_window_seconds INT,
    cloudflare_tunnel BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    priority INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Encrypted credentials (Fernet)
CREATE TABLE credentials (
    id UUID PRIMARY KEY,
    provider_id UUID NOT NULL REFERENCES providers(id),
    encrypted_data BYTEA NOT NULL,
    last_rotated_at TIMESTAMP,
    last_used_at TIMESTAMP
);

-- Raw data (as-received from providers)
CREATE TABLE raw_data (
    id UUID PRIMARY KEY,
    provider_id UUID NOT NULL REFERENCES providers(id),
    data_type VARCHAR(50),
    external_id VARCHAR(255),
    raw_payload JSONB NOT NULL,
    fetched_at TIMESTAMP NOT NULL,
    checksum VARCHAR(64) -- deduplication
);
CREATE INDEX idx_raw_data_provider_timestamp ON raw_data(provider_id, fetched_at DESC);

-- Normalized data (common schema)
CREATE TABLE normalized_data (
    id UUID PRIMARY KEY,
    source_provider_id UUID REFERENCES providers(id),
    entity_id VARCHAR(255),
    entity_type VARCHAR(50),
    normalized_payload JSONB NOT NULL,
    normalized_at TIMESTAMP NOT NULL,
    UNIQUE(source_provider_id, entity_id, entity_type, normalized_at)
);

-- Health status tracking
CREATE TABLE provider_health (
    id UUID PRIMARY KEY,
    provider_id UUID NOT NULL REFERENCES providers(id),
    is_healthy BOOLEAN NOT NULL,
    response_time_ms INT,
    error_message TEXT,
    checked_at TIMESTAMP NOT NULL,
    UNIQUE(provider_id, checked_at)
);

-- Audit logging
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    provider_id UUID REFERENCES providers(id),
    user_id VARCHAR(255),
    status VARCHAR(50),
    details JSONB,
    logged_at TIMESTAMP DEFAULT NOW()
);
