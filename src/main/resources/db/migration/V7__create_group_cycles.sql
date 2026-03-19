-- This table stores each prompt cycle for a group.
-- A cycle is one round of shared prompts for that group


CREATE TABLE group_cycles (
                              id BIGSERIAL PRIMARY KEY,
                              group_id BIGINT NOT NULL,
                              cycle_start TIMESTAMP NOT NULL,
                              cycle_end TIMESTAMP NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_group_cycles_group
                                  FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);