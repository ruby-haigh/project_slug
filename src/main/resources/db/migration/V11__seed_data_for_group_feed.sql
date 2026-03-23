INSERT INTO users (email) VALUES ('bob@example.com');
INSERT INTO users (email) VALUES ('jane@example.com');
INSERT INTO users (email) VALUES ('toby@example.com');
INSERT INTO users (email) VALUES ('rich@example.com');

INSERT INTO groups (name) VALUES ('Uni Friends');

INSERT INTO group_memberships (user_id, group_id) VALUES (1, 1);
INSERT INTO group_memberships (user_id, group_id) VALUES (2, 1);
INSERT INTO group_memberships (user_id, group_id) VALUES (3, 1);
INSERT INTO group_memberships (user_id, group_id) VALUES (4, 1);

INSERT INTO group_cycles (group_id, cycle_start, cycle_end) VALUES (1, '2026-03-01', '2026-03-31');

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text)
VALUES (
        1, 1, 1, 1,
        'I passed my driving test'
       );
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text)
VALUES (
        1, 1, 2, 1,
        'I got a promotion at work'
       );
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text)
VALUES (
        1, 1, 1, 1,
        'I beat my 5K PB'
       );
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text)
VALUES (
        1, 1, 1, 1,
        'I made it through Monday'
       );

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text)
VALUES (
        1, 1, 1, 3,
        'Breaking Bad. Absolutely, best show ever!'
       );
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text)
VALUES (
        1, 1, 2, 3,
        'The Harry Potter franchise for the 1000th time. Always.'
       );
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text)
VALUES (
        1, 1, 1, 3,
        'Twilight. Nope.'
       );
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text)
VALUES (
        1, 1, 1, 3,
        'Daredevil in preparation for the new season!'
       );


