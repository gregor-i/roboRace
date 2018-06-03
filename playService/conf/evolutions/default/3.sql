# owner row for games and scenarios

# --- !Ups

ALTER TABLE scenarios ADD COLUMN owner varchar(255);
ALTER TABLE scenarios ALTER COLUMN owner SET DEFAULT 'owner';
ALTER TABLE scenarios ALTER owner SET NOT NULL;

ALTER TABLE games ADD COLUMN owner varchar(255);
ALTER TABLE games ALTER COLUMN owner SET DEFAULT 'owner';
ALTER TABLE games ALTER owner SET NOT NULL;

# --- !Downs

ALTER TABLE scenarios DROP COLUMN owner;

ALTER TABLE games DROP COLUMN owner;
