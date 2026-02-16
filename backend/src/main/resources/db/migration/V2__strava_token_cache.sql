-- V2: Create Strava token cache table for distributed token storage
-- Supports multi-instance deployments (Cloud Run, Railway, etc.)

CREATE TABLE strava_tokens (
    athlete_id VARCHAR(255) PRIMARY KEY,
    access_token VARCHAR(1024) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index on expires_at for efficient expiry checks
CREATE INDEX idx_strava_tokens_expires_at ON strava_tokens(expires_at);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_strava_tokens_updated_at
    BEFORE UPDATE ON strava_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
