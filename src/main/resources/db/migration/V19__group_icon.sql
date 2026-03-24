ALTER TABLE groups ADD COLUMN icon VARCHAR(10);
UPDATE groups SET icon = '✧' WHERE icon IS NULL;