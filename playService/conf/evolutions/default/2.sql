# Scenarios schema

# --- !Ups

CREATE TABLE scenarios (
  id varchar(255) NOT NULL,
  scenario varchar NOT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE scenarios;