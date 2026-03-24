ALTER TABLE group_responses
    ADD COLUMN spotify_track_url VARCHAR(500);

ALTER TABLE group_cycles
    ADD COLUMN spotify_playlist_id VARCHAR(255),
    ADD COLUMN spotify_playlist_url VARCHAR(500);
