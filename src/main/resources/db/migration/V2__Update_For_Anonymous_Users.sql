-- V2__Update_For_Anonymous_Users.sql
-- Migration to remove user authentication and add device-based identification

-- Alter users table - remove authentication fields, add device_id and age
ALTER TABLE users DROP COLUMN IF EXISTS email;
ALTER TABLE users DROP COLUMN IF EXISTS username;
ALTER TABLE users DROP COLUMN IF EXISTS password;
ALTER TABLE users DROP COLUMN IF EXISTS first_name;
ALTER TABLE users DROP COLUMN IF EXISTS last_name;
ALTER TABLE users DROP COLUMN IF EXISTS profile_image_url;
ALTER TABLE users DROP COLUMN IF EXISTS bio;
ALTER TABLE users DROP COLUMN IF EXISTS is_active;
ALTER TABLE users DROP COLUMN IF EXISTS is_email_verified;

-- Add new columns for anonymous users
ALTER TABLE users ADD COLUMN IF NOT EXISTS device_id VARCHAR(255) NOT NULL UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS age INT NOT NULL DEFAULT 18;
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(10) NOT NULL DEFAULT 'en';

-- Alter comments table - replace user_id with device_id
ALTER TABLE comments DROP CONSTRAINT IF EXISTS fk_comments_user;
ALTER TABLE comments DROP COLUMN IF EXISTS user_id;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS device_id VARCHAR(255) NOT NULL DEFAULT 'anonymous';

-- Alter likes table - replace user_id with device_id
ALTER TABLE likes DROP CONSTRAINT IF EXISTS fk_likes_user;
ALTER TABLE likes DROP CONSTRAINT IF EXISTS unique_news_likes;
ALTER TABLE likes DROP COLUMN IF EXISTS user_id;
ALTER TABLE likes ADD COLUMN IF NOT EXISTS device_id VARCHAR(255) NOT NULL DEFAULT 'anonymous';
ALTER TABLE likes ADD CONSTRAINT unique_news_device_likes UNIQUE (news_id, device_id);

-- Alter bookmarks table - replace user_id with device_id
ALTER TABLE bookmarks DROP CONSTRAINT IF EXISTS fk_bookmarks_user;
ALTER TABLE bookmarks DROP CONSTRAINT IF EXISTS unique_user_news_bookmarks;
ALTER TABLE bookmarks DROP COLUMN IF EXISTS user_id;
ALTER TABLE bookmarks ADD COLUMN IF NOT EXISTS device_id VARCHAR(255) NOT NULL DEFAULT 'anonymous';
ALTER TABLE bookmarks ADD CONSTRAINT unique_device_news_bookmarks UNIQUE (device_id, news_id);

-- Simplify user_preferences table
ALTER TABLE user_preferences DROP COLUMN IF EXISTS language;
ALTER TABLE user_preferences DROP COLUMN IF EXISTS theme;
ALTER TABLE user_preferences DROP COLUMN IF EXISTS notifications_enabled;

