-- V3__Add_News_Expiration.sql
-- Migration to add news expiration support (5-day auto-removal)

-- Add expires_at column to news table
ALTER TABLE news ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;

-- Update existing news records to expire 5 days from now
UPDATE news SET expires_at = created_at + INTERVAL '5 days' WHERE expires_at IS NULL;

-- Make expires_at NOT NULL after populating existing data
ALTER TABLE news ALTER COLUMN expires_at SET NOT NULL;

-- Create index on expires_at for efficient querying
CREATE INDEX IF NOT EXISTS idx_news_expires_at ON news(expires_at);

