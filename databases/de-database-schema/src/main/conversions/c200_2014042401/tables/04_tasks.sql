SET search_path = public, pg_catalog;

--
-- Renames the existing template table to tasks and adds updated columns.
--
ALTER TABLE template RENAME TO tasks;
ALTER TABLE ONLY tasks RENAME COLUMN hid TO hid_v187;
ALTER TABLE ONLY tasks RENAME COLUMN id TO id_v187;
ALTER TABLE ONLY tasks RENAME COLUMN component_id TO component_id_v187;
ALTER TABLE ONLY tasks RENAME COLUMN type TO type_v187;
ALTER TABLE ONLY tasks ADD COLUMN id UUID DEFAULT (uuid_generate_v1());
UPDATE tasks SET id = CAST(id_v187 AS UUID) WHERE CHAR_LENGTH(id_v187) = 36;
ALTER TABLE ONLY tasks ADD COLUMN tool_id UUID;
ALTER TABLE ONLY tasks ADD COLUMN external_app_id varchar(255);

