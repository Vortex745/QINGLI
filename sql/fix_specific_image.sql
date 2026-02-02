-- Fix specific missing image 404 error
-- File: f7a30ed7-6dc6-4aca-8ffa-2279b7d8ce6b.jpeg
-- 1. sys_user
UPDATE sys_user
SET avatar_url = NULL
WHERE avatar_url LIKE '%f7a30ed7-6dc6-4aca-8ffa-2279b7d8ce6b%';
UPDATE sys_user
SET bg_image = NULL
WHERE bg_image LIKE '%f7a30ed7-6dc6-4aca-8ffa-2279b7d8ce6b%';
-- 2. bus_post
-- Check if user_avatar exists (if denormalized) - silently fails if column unlikely, but I'll standardise logic
-- Safe update using dynamic SQL is too complex for this tool, I'll just run plain UPDATEs and ignore errors.
-- Actually I can't ignore errors in a single script easily if one fails.
-- I'll do them one by one in terminal.
-- This file is for reference or if I can run it.