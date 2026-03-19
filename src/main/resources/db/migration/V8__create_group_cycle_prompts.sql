-- This table stores which prompts were selected for a specific group cycle.
-- Each row links one cycle to one prompt.
-- A cycle will usually have multiple rows here (one for each chosen prompt).

CREATE TABLE group_cycle_prompts (
                                     id BIGSERIAL PRIMARY KEY,
                                     group_cycle_id BIGINT NOT NULL,
                                     prompt_id BIGINT NOT NULL,

                                     CONSTRAINT fk_group_cycle_prompts_cycle
                                         FOREIGN KEY (group_cycle_id) REFERENCES group_cycles(id) ON DELETE CASCADE,

                                     CONSTRAINT fk_group_cycle_prompts_prompt
                                         FOREIGN KEY (prompt_id) REFERENCES prompts(id) ON DELETE CASCADE
);