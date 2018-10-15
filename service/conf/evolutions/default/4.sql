# migrate scenarios without 'traps' field

# --- !Ups

UPDATE scenarios
set scenario = jsonb_set(scenario :: jsonb, Array['traps'] :: text[], '[]' :: jsonb)
where (scenario ::  jsonb) ->> 'traps' is NULL;

UPDATE games
set game = jsonb_set(game :: jsonb, '{scenario,traps}' :: text[], '[]' :: jsonb)
where (game ::  jsonb) #>> '{scenario,traps}' is NULL;

# --- !Downs
