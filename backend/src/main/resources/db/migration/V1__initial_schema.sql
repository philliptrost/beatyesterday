CREATE TABLE athlete (
    id              VARCHAR(255) PRIMARY KEY,
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    profile_image   VARCHAR(1024),
    sex             VARCHAR(1),
    birth_date      DATE,
    raw_data        JSONB NOT NULL DEFAULT '{}'
);

CREATE TABLE gear (
    id              VARCHAR(255) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    distance_m      DOUBLE PRECISION NOT NULL DEFAULT 0,
    is_retired      BOOLEAN NOT NULL DEFAULT FALSE,
    created_on      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE activity (
    id                      VARCHAR(255) PRIMARY KEY,
    start_date_time         TIMESTAMPTZ NOT NULL,
    sport_type              VARCHAR(50) NOT NULL,
    name                    VARCHAR(500) NOT NULL,
    description             TEXT,
    distance_km             DOUBLE PRECISION NOT NULL,
    elevation_m             DOUBLE PRECISION NOT NULL,
    start_latitude          DOUBLE PRECISION,
    start_longitude         DOUBLE PRECISION,
    calories                INTEGER,
    average_power           INTEGER,
    max_power               INTEGER,
    average_speed_kmh       DOUBLE PRECISION NOT NULL,
    max_speed_kmh           DOUBLE PRECISION NOT NULL,
    average_heart_rate      INTEGER,
    max_heart_rate          INTEGER,
    average_cadence         INTEGER,
    moving_time_seconds     INTEGER NOT NULL,
    kudo_count              INTEGER NOT NULL DEFAULT 0,
    device_name             VARCHAR(255),
    polyline                TEXT,
    gear_id                 VARCHAR(255) REFERENCES gear(id),
    is_commute              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_start_date ON activity(start_date_time);
CREATE INDEX idx_activity_sport_type ON activity(sport_type);
CREATE INDEX idx_activity_gear_id ON activity(gear_id);
