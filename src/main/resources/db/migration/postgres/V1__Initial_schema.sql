CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

CREATE TABLE users (
                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                   email VARCHAR(255) NOT NULL UNIQUE,
                   password VARCHAR(255) NOT NULL,
                   name VARCHAR(255) NOT NULL,
                   created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                   updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ACTIVITIES TABLE
CREATE TABLE activities (
                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                    title VARCHAR(255) NOT NULL,
                    description TEXT,
                    activity_type VARCHAR(100) NOT NULL,
                    start_time TIMESTAMPTZ NOT NULL,
                    end_time TIMESTAMPTZ NOT NULL,
                    max_participants INTEGER NOT NULL,
                    location GEOMETRY(Point, 4326),
                    attributes JSONB,
                    creator_id UUID NOT NULL REFERENCES users(id),
                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- MANY-TO-MANY JOIN TABLE
CREATE TABLE activity_participants (
                       activity_id UUID NOT NULL REFERENCES activities(id) ON DELETE CASCADE,
                       user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       PRIMARY KEY (activity_id, user_id)
);

-- INDEXES
-- Spatial
CREATE INDEX idx_activities_location ON activities USING GIST(location);

-- Temporal and composite indexes
CREATE INDEX idx_activity_type_start_time ON activities(activity_type, start_time);
CREATE INDEX idx_activities_start_time ON activities(start_time);  -- Simple time index
CREATE INDEX idx_activities_creator ON activities(creator_id);

-- Users
CREATE UNIQUE INDEX idx_users_lower_email ON users (LOWER(email));