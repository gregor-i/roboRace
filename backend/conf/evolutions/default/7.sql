# remove creationTime column to games

# --- !Ups

ALTER TABLE games
  DROP COLUMN creationTime;

# --- !Downs
