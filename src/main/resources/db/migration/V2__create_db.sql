CREATE TABLE user_projection (
  id UUID,
  username VARCHAR(50),
  email VARCHAR(50),
  team_id UUID
);

CREATE TABLE team_projection (
  id UUID,
  label VARCHAR(50)
);