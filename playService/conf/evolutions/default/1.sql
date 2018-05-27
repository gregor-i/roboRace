# Games schema

# --- !Ups

CREATE TABLE games (
  id varchar(255) NOT NULL,
  game varchar NOT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE games;