# add creationTime column to games

# --- !Ups

TRUNCATE TABLE games;

ALTER TABLE games
  ADD COLUMN creationTime timestamp NOT NULL;

# --- !Downs
