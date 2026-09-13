DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS databasechangelog CASCADE;
DROP TABLE IF EXISTS databasechangeloglock CASCADE;

SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'tasks'
ORDER BY ordinal_position;

SELECT id, username, email, role, password, deleted, created_at
FROM users;


SELECT id, title, description, status, owner_id, deleted, deleted_at, created_at
FROM tasks;

SELECT id, title, status FROM tasks WHERE deleted = false;

SELECT id, title, status
FROM tasks
WHERE owner_id = 1 AND deleted = false AND status = 'IN_PROGRESS';

SELECT id, title, status, created_at, updated_at
FROM tasks
WHERE id = 1;

SELECT id, title, deleted, deleted_at
FROM tasks
WHERE id = 1;

SELECT id, title
FROM tasks
WHERE id = 1 AND deleted = false;
