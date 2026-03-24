-- one reaction per user per response
-- same emoji clicked again = remove
-- different emoji clicked = update existing row

CREATE TABLE group_response_reactions (
                                          id BIGSERIAL PRIMARY KEY,
                                          group_response_id BIGINT NOT NULL,
                                          user_id BIGINT NOT NULL,
                                          emoji VARCHAR(20) NOT NULL,

                                          CONSTRAINT fk_group_response_reactions_response
                                              FOREIGN KEY (group_response_id) REFERENCES group_responses(id) ON DELETE CASCADE,

                                          CONSTRAINT fk_group_response_reactions_user
                                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);