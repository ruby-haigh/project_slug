-- Add a few more prompts for future cycles
INSERT INTO prompts (prompt) VALUES
                                 ('What song sums up your week?'),
                                 ('What is one small win from this week?'),
                                 ('What are you looking forward to next?');

-- Add more demo users
INSERT INTO users (email, name, bio) VALUES
                                         ('maya@example.com', 'Maya', 'Chronically online and proud.'),
                                         ('leo@example.com', 'Leo', 'Gym, coffee, repeat.'),
                                         ('nia@example.com', 'Nia', 'Bookshops and playlists.'),
                                         ('sam@example.com', 'Sam', 'Probably watching football.');

-- Add more groups
INSERT INTO groups (name, frequency, created_at) VALUES
                                                     ('Work Besties', 'MONTHLY', CURRENT_TIMESTAMP),
                                                     ('Family Chaos', 'MONTHLY', CURRENT_TIMESTAMP),
                                                     ('Sunday Reset', 'MONTHLY', CURRENT_TIMESTAMP);

-- Memberships for Work Besties
INSERT INTO group_memberships (user_id, group_id)
SELECT u.id, g.id
FROM users u, groups g
WHERE u.email IN ('maya@example.com', 'leo@example.com', 'nia@example.com')
  AND g.name = 'Work Besties';

-- Memberships for Family Chaos
INSERT INTO group_memberships (user_id, group_id)
SELECT u.id, g.id
FROM users u, groups g
WHERE u.email IN ('bob@example.com', 'jane@example.com', 'sam@example.com')
  AND g.name = 'Family Chaos';

-- Memberships for Sunday Reset
INSERT INTO group_memberships (user_id, group_id)
SELECT u.id, g.id
FROM users u, groups g
WHERE u.email IN ('rich@example.com', 'toby@example.com', 'maya@example.com', 'nia@example.com')
  AND g.name = 'Sunday Reset';

-- Create active cycles that are already past the 1-week feed lock window
INSERT INTO group_cycles (group_id, cycle_start, cycle_end, created_at)
SELECT id, '2026-03-01', '2026-03-31', CURRENT_TIMESTAMP
FROM groups
WHERE name IN ('Work Besties', 'Family Chaos', 'Sunday Reset');

-- Work Besties responses
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'Finally finished that horrible spreadsheet project.', '2026-03-03'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Work Besties'
  AND u.email = 'maya@example.com'
  AND p.prompt = 'Tell us about something you achieved recently.';

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'Got through the week without crying on Teams.', '2026-03-04'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Work Besties'
  AND u.email = 'leo@example.com'
  AND p.prompt = 'Tell us about something you achieved recently.';

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'Severance. I need everyone to catch up immediately.', '2026-03-05'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Work Besties'
  AND u.email = 'nia@example.com'
  AND p.prompt = 'What are you watching right now? Would you recommend it?';

-- Family Chaos responses
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'Mum made roast dinner and I am still thinking about it.', '2026-03-03'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Family Chaos'
  AND u.email = 'bob@example.com'
  AND p.prompt = 'What''s the best meal you''ve had recently?';

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'Honestly just looking forward to sleeping in this weekend.', '2026-03-04'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Family Chaos'
  AND u.email = 'jane@example.com'
  AND p.prompt = 'Are you looking forward to anything at the moment?';

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'I tried making tiramisu and somehow it worked.', '2026-03-05'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Family Chaos'
  AND u.email = 'sam@example.com'
  AND p.prompt = 'Have you tried anything new lately? How did it go?';

-- Sunday Reset responses
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'A slow morning, coffee, and absolutely no plans.', '2026-03-02'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Sunday Reset'
  AND u.email = 'rich@example.com'
  AND p.prompt = 'How are you looking after yourself at the moment?';

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'Trying to stop doomscrolling before bed. Mixed success.', '2026-03-03'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Sunday Reset'
  AND u.email = 'toby@example.com'
  AND p.prompt = 'How are you looking after yourself at the moment?';

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, created_at)
SELECT gc.id, g.id, u.id, p.id, 'A proper reset day, long walk, clean room, fresh playlist.', '2026-03-04'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Sunday Reset'
  AND u.email = 'maya@example.com'
  AND p.prompt = 'What''s something that made you smile recently?';

-- Spotify prompt responses with explicit track URLs
INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, spotify_track_url, created_at)
SELECT gc.id, g.id, u.id, p.id,
       'This has been on repeat all month.',
       'https://open.spotify.com/track/4PTG3Z6ehGkBFwjybzWkR8',
       '2026-03-06'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Work Besties'
  AND u.email = 'maya@example.com'
  AND p.prompt = 'Share a Spotify song for this month.';

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, spotify_track_url, created_at)
SELECT gc.id, g.id, u.id, p.id,
       'Instant mood lift.',
       'https://open.spotify.com/track/1lDWb6b6ieDQ2xT7ewTC3G',
       '2026-03-06'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Family Chaos'
  AND u.email = 'jane@example.com'
  AND p.prompt = 'Share a Spotify song for this month.';

INSERT INTO group_responses (group_cycle_id, group_id, user_id, prompt_id, response_text, spotify_track_url, created_at)
SELECT gc.id, g.id, u.id, p.id,
       'My Sunday reset anthem.',
       'https://open.spotify.com/track/2takcwOaAZWiXQijPHIx7B',
       '2026-03-06'
FROM group_cycles gc, groups g, users u, prompts p
WHERE gc.group_id = g.id
  AND g.name = 'Sunday Reset'
  AND u.email = 'rich@example.com'
  AND p.prompt = 'Share a Spotify song for this month.';

-- A few demo reactions so feeds feel alive
INSERT INTO group_response_reactions (group_response_id, user_id, emoji)
SELECT gr.id, u.id, '❤️'
FROM group_responses gr, users u
WHERE gr.response_text = 'Finally finished that horrible spreadsheet project.'
  AND u.email = 'leo@example.com';

INSERT INTO group_response_reactions (group_response_id, user_id, emoji)
SELECT gr.id, u.id, '😂'
FROM group_responses gr, users u
WHERE gr.response_text = 'Got through the week without crying on Teams.'
  AND u.email = 'maya@example.com';

INSERT INTO group_response_reactions (group_response_id, user_id, emoji)
SELECT gr.id, u.id, '👍'
FROM group_responses gr, users u
WHERE gr.response_text = 'A proper reset day, long walk, clean room, fresh playlist.'
  AND u.email = 'nia@example.com';