# add discription column to scenario

# --- !Ups

ALTER TABLE scenarios
  ADD COLUMN description text NOT NULL DEFAULT '';

# --- !Downs
