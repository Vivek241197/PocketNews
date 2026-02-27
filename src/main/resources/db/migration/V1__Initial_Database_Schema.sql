-- V1__Initial_Database_Schema.sql
-- This is the initial database schema for PocketNews application

-- Create Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL UNIQUE,
    age INT NOT NULL DEFAULT 18,
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url TEXT,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_category_slug ON categories(slug);

CREATE TABLE news (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    short_headline VARCHAR(300) NOT NULL,
    short_content TEXT NOT NULL,
    content TEXT NOT NULL,
    image_url TEXT,
    source VARCHAR(255),
    view_count BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    published_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_news_category FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE CASCADE
);

CREATE INDEX idx_news_category ON news(category_id);
CREATE INDEX idx_news_published ON news(published_at);
CREATE INDEX idx_news_view_count ON news(view_count);
CREATE INDEX idx_news_expires_at ON news(expires_at);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    news_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    likes_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_comments_news FOREIGN KEY (news_id)
        REFERENCES news(id) ON DELETE CASCADE
);

CREATE INDEX idx_comment_news ON comments(news_id);
CREATE INDEX idx_comment_device ON comments(device_id);

CREATE TABLE bookmarks (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    news_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_bookmarks_news FOREIGN KEY (news_id)
        REFERENCES news(id) ON DELETE CASCADE,
    CONSTRAINT unique_device_news_bookmarks UNIQUE (device_id, news_id)
);

CREATE INDEX idx_device_id ON bookmarks(device_id);
CREATE INDEX idx_expires_at ON bookmarks(expires_at);

CREATE TABLE device_category_preferences (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pref_category FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT unique_device_category UNIQUE (device_id, category_id)
);

CREATE INDEX idx_pref_device ON device_category_preferences(device_id);
CREATE INDEX idx_pref_category ON device_category_preferences(category_id);

CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    preferred_categories TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_preferences_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);
