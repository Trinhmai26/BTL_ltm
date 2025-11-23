-- Migration script to add avatar_url column to existing users table
-- Run this script if you already have the users table without avatar_url column

USE login_system;

-- Add avatar_url column if it doesn't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500) AFTER email;

-- Optional: Update existing users with default avatar URLs
UPDATE users 
SET avatar_url = CONCAT('https://ui-avatars.com/api/?name=', username, '&size=120&background=2563eb&color=fff')
WHERE avatar_url IS NULL;

SELECT 'Migration completed successfully!' AS status;
