-- V3__Add_User_Profiles_And_Likes.sql

-- Create user_profiles table (used by OnboardingService for language preference)
CREATE TABLE IF NOT EXISTS user_profiles (
    id          BIGSERIAL PRIMARY KEY,
    device_id   VARCHAR(255) NOT NULL UNIQUE,
    language_code VARCHAR(10),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_profile_device ON user_profiles(device_id);
