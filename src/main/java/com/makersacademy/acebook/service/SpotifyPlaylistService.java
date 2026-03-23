package com.makersacademy.acebook.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makersacademy.acebook.model.Group;
import com.makersacademy.acebook.model.GroupCycle;
import com.makersacademy.acebook.model.GroupResponse;
import com.makersacademy.acebook.repository.GroupCycleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpotifyPlaylistService {
    private static final Pattern TRACK_URL_PATTERN = Pattern.compile("open\\.spotify\\.com/track/([A-Za-z0-9]+)");
    private static final Pattern TRACK_URI_PATTERN = Pattern.compile("spotify:track:([A-Za-z0-9]+)");

    private final GroupCycleRepository groupCycleRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String appBaseUrl;
    private volatile String lastSyncStatus = "No Spotify sync attempted yet.";

    public SpotifyPlaylistService(GroupCycleRepository groupCycleRepository,
                                  @Value("${spotify.client-id:}") String clientId,
                                  @Value("${spotify.client-secret:}") String clientSecret,
                                  @Value("${spotify.refresh-token:}") String refreshToken,
                                  @Value("${app.base-url}") String appBaseUrl) {
        this.groupCycleRepository = groupCycleRepository;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.appBaseUrl = appBaseUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean isConfigured() {
        return !clientId.isBlank() && !clientSecret.isBlank() && !refreshToken.isBlank();
    }

    public boolean hasRefreshToken() {
        return !refreshToken.isBlank();
    }

    public String getLastSyncStatus() {
        return lastSyncStatus;
    }

    public boolean looksLikeSpotifyPrompt(String promptText) {
        if (promptText == null) {
            return false;
        }

        String normalized = promptText.toLowerCase();
        return normalized.contains("spotify")
                || normalized.contains("song")
                || normalized.contains("track")
                || normalized.contains("playlist");
    }

    public boolean hasClientConfiguration() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    public String buildAuthorizationUrl() {
        String redirectUri = URLEncoder.encode(appBaseUrl + "/spotify/callback", StandardCharsets.UTF_8);
        String scopes = URLEncoder.encode("playlist-read-private playlist-modify-private playlist-modify-public", StandardCharsets.UTF_8);
        return "https://accounts.spotify.com/authorize?response_type=code"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&scope=" + scopes
                + "&show_dialog=true"
                + "&redirect_uri=" + redirectUri;
    }

    public String exchangeCodeForRefreshToken(String code) {
        try {
            String basicAuth = Base64.getEncoder()
                    .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

            String requestBody = "grant_type=authorization_code"
                    + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(appBaseUrl + "/spotify/callback", StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://accounts.spotify.com/api/token"))
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new RuntimeException("Spotify auth code exchange failed with status " + response.statusCode());
            }

            JsonNode json = objectMapper.readTree(response.body());
            return json.path("refresh_token").asText(null);
        } catch (Exception exception) {
            throw new RuntimeException("Could not exchange Spotify code for refresh token: " + exception.getMessage(), exception);
        }
    }

    public String normalizeSpotifyTrackUrl(String rawValue) {
        String trackId = extractTrackId(rawValue);
        if (trackId == null) {
            return null;
        }

        return "https://open.spotify.com/track/" + trackId;
    }

    public List<SpotifyTrackLink> buildTrackLinks(List<String> trackUrls) {
        List<SpotifyTrackLink> links = new ArrayList<>();
        if (trackUrls == null || trackUrls.isEmpty()) {
            return links;
        }

        String accessToken = null;
        if (isConfigured()) {
            try {
                accessToken = fetchAccessToken();
            } catch (Exception exception) {
                System.out.println("Spotify track metadata lookup failed: " + exception.getMessage());
            }
        }

        int index = 1;
        for (String trackUrl : trackUrls) {
            String trackId = extractTrackId(trackUrl);
            if (trackId == null) {
                links.add(new SpotifyTrackLink(trackUrl, "Open song " + index, ""));
                index++;
                continue;
            }

            if (accessToken == null) {
                links.add(new SpotifyTrackLink(trackUrl, "Open song " + index, ""));
                index++;
                continue;
            }

            try {
                links.add(fetchTrackDetails(trackId, trackUrl, accessToken, index));
            } catch (Exception exception) {
                System.out.println("Spotify track detail fetch failed: " + exception.getMessage());
                links.add(new SpotifyTrackLink(trackUrl, "Open song " + index, ""));
            }
            index++;
        }

        return links;
    }

    public String syncPlaylistForCycle(Group group, GroupCycle cycle, List<GroupResponse> responses) {
        if (!isConfigured()) {
            lastSyncStatus = "Spotify is not fully configured in the running app.";
            return cycle.getSpotifyPlaylistUrl();
        }

        Set<String> uris = new LinkedHashSet<>();
        for (GroupResponse response : responses) {
            String trackId = extractTrackId(response.getSpotifyTrackUrl());
            if (trackId == null) {
                trackId = extractTrackId(response.getResponseText());
            }
            if (trackId != null) {
                uris.add("spotify:track:" + trackId);
            }
        }

        if (uris.isEmpty()) {
            lastSyncStatus = "No Spotify track URIs were found in this cycle.";
            return cycle.getSpotifyPlaylistUrl();
        }

        try {
            String accessToken = fetchAccessToken();
            if (accessToken == null) {
                lastSyncStatus = "Spotify access token request returned no token.";
                return cycle.getSpotifyPlaylistUrl();
            }

            if (cycle.getSpotifyPlaylistId() == null || cycle.getSpotifyPlaylistId().isBlank()) {
                PlaylistDetails playlist = createPlaylist(group, cycle, accessToken);
                if (playlist == null) {
                    lastSyncStatus = "Spotify playlist creation returned no playlist details.";
                    return cycle.getSpotifyPlaylistUrl();
                }

                cycle.setSpotifyPlaylistId(playlist.id());
                cycle.setSpotifyPlaylistUrl(playlist.url());
            }

            addPlaylistItems(cycle.getSpotifyPlaylistId(), new ArrayList<>(uris), accessToken);
            groupCycleRepository.save(cycle);
            lastSyncStatus = "Spotify playlist sync succeeded for cycle " + cycle.getId()
                    + " with " + uris.size() + " track(s).";
            return cycle.getSpotifyPlaylistUrl();
        } catch (Exception exception) {
            lastSyncStatus = "Spotify playlist sync failed: " + exception.getMessage();
            System.out.println(lastSyncStatus);
            return cycle.getSpotifyPlaylistUrl();
        }
    }

    private String fetchAccessToken() throws IOException, InterruptedException {
        String basicAuth = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        String requestBody = "grant_type=refresh_token&refresh_token="
                + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Spotify auth failed with status " + response.statusCode());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.path("access_token").asText(null);
    }

    private PlaylistDetails createPlaylist(Group group, GroupCycle cycle, String accessToken) throws IOException, InterruptedException {
        String cycleLabel = cycle.getCycleStart() == null
                ? "Issue"
                : cycle.getCycleStart().toLocalDate().format(DateTimeFormatter.ISO_DATE);

        String requestBody = objectMapper.writeValueAsString(
                new CreatePlaylistRequest(
                        group.getName() + " - " + cycleLabel,
                        "Songs shared in Snail Mail for " + group.getName(),
                        true
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me/playlists"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Spotify playlist creation failed with status " + response.statusCode());
        }

        JsonNode json = objectMapper.readTree(response.body());
        String playlistId = json.path("id").asText(null);
        String playlistUrl = json.path("external_urls").path("spotify").asText(null);

        if (playlistId == null || playlistUrl == null) {
            return null;
        }

        return new PlaylistDetails(playlistId, playlistUrl);
    }

    private void addPlaylistItems(String playlistId, List<String> urisToAdd, String accessToken) throws IOException, InterruptedException {
        if (urisToAdd.isEmpty()) {
            return;
        }

        String joinedUris = String.join(",", urisToAdd);
        String encodedUris = URLEncoder.encode(joinedUris, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?uris=" + encodedUris))
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Spotify playlist update failed with status "
                    + response.statusCode() + " and body " + response.body());
        }
    }

    private String extractTrackId(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String trimmed = rawValue.trim();

        Matcher uriMatcher = TRACK_URI_PATTERN.matcher(trimmed);
        if (uriMatcher.find()) {
            return uriMatcher.group(1);
        }

        Matcher urlMatcher = TRACK_URL_PATTERN.matcher(trimmed);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        }

        return null;
    }

    private SpotifyTrackLink fetchTrackDetails(String trackId,
                                               String trackUrl,
                                               String accessToken,
                                               int fallbackIndex) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/tracks/" + trackId))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("Spotify track lookup failed with status "
                    + response.statusCode() + " and body " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        String title = json.path("name").asText("Open song " + fallbackIndex);
        String artist = "";
        if (json.has("artists")) {
            artist = streamArtists(json.path("artists"));
        }

        return new SpotifyTrackLink(trackUrl, title, artist);
    }

    private String streamArtists(JsonNode artistsNode) {
        List<String> artistNames = new ArrayList<>();
        for (JsonNode artistNode : artistsNode) {
            String name = artistNode.path("name").asText(null);
            if (name != null && !name.isBlank()) {
                artistNames.add(name);
            }
        }
        return artistNames.stream().collect(Collectors.joining(", "));
    }

    private record CreatePlaylistRequest(String name,
                                         String description,
                                         @JsonProperty("public") boolean isPublic) {}
    private record PlaylistDetails(String id, String url) {}

    public record SpotifyTrackLink(String url, String title, String artist) {}
}
