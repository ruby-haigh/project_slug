package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.service.SpotifyPlaylistService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SpotifyAuthController {
    private final SpotifyPlaylistService spotifyPlaylistService;

    public SpotifyAuthController(SpotifyPlaylistService spotifyPlaylistService) {
        this.spotifyPlaylistService = spotifyPlaylistService;
    }

    @GetMapping("/spotify/connect")
    public RedirectView connectSpotify() {
        if (!spotifyPlaylistService.hasClientConfiguration()) {
            return new RedirectView("/spotify/setup-help");
        }

        return new RedirectView(spotifyPlaylistService.buildAuthorizationUrl());
    }

    @GetMapping("/spotify/callback")
    @ResponseBody
    public String spotifyCallback(@RequestParam(required = false) String code,
                                  @RequestParam(required = false) String error) {
        if (error != null && !error.isBlank()) {
            return "Spotify authorisation failed: " + error;
        }

        if (code == null || code.isBlank()) {
            return "No Spotify authorisation code was returned.";
        }

        String refreshToken = spotifyPlaylistService.exchangeCodeForRefreshToken(code);
        if (refreshToken == null || refreshToken.isBlank()) {
            return "Spotify did not return a refresh token. Try connecting again.";
        }

        return "Spotify connected. Copy this refresh token into SPOTIFY_REFRESH_TOKEN:\n\n" + refreshToken;
    }

    @GetMapping("/spotify/setup-help")
    @ResponseBody
    public String spotifySetupHelp() {
        return "Add SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET to the app first, then open /spotify/connect.";
    }

    @GetMapping("/spotify/status")
    @ResponseBody
    public String spotifyStatus() {
        return "Spotify client configured: " + spotifyPlaylistService.hasClientConfiguration()
                + "\nSpotify refresh token configured: " + spotifyPlaylistService.hasRefreshToken()
                + "\nSpotify playlist sync ready: " + spotifyPlaylistService.isConfigured()
                + "\nLast Spotify sync status: " + spotifyPlaylistService.getLastSyncStatus();
    }
}
