-- This table stores each user's response to each prompt
-- for a specific group cycle.
-- One submitted form will usually create multiple rows here due to
-- one row per prompt answer.

CREATE TABLE group_responses (
                                 id BIGSERIAL PRIMARY KEY,
                                 group_cycle_id BIGINT NOT NULL,
                                 group_id BIGINT NOT NULL,
                                 user_id BIGINT NOT NULL,
                                 prompt_id BIGINT NOT NULL,
                                 response_text TEXT NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_group_responses_cycle
                                     FOREIGN KEY (group_cycle_id) REFERENCES group_cycles(id) ON DELETE CASCADE,

                                 CONSTRAINT fk_group_responses_group
                                     FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,

                                 CONSTRAINT fk_group_responses_user
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                                 CONSTRAINT fk_group_responses_prompt
                                     FOREIGN KEY (prompt_id) REFERENCES prompts(id) ON DELETE CASCADE
);