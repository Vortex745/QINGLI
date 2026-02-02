USE xianqu_pdb;

-- Check valid users
SELECT id, username, nickname FROM sys_user;

-- Check goods and their user_ids
SELECT id, name, user_id FROM bus_goods LIMIT 10;
