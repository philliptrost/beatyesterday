package com.beatyesterday.web.controller

import com.beatyesterday.infrastructure.strava.StravaOAuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Handles the Strava OAuth2 flow. The frontend redirects the user to Strava's
 * authorization page, Strava redirects back with a code, and we exchange it for tokens.
 */
@RestController
@RequestMapping("/api/oauth")
class OAuthController(
    private val stravaOAuthService: StravaOAuthService,
) {

    /**
     * Returns the Strava OAuth authorization URL for the frontend to redirect to.
     */
    @GetMapping("/strava/url")
    fun getAuthUrl(@RequestParam redirectUri: String): Map<String, String> {
        val url = "https://www.strava.com/oauth/authorize" +
            "?client_id=${stravaOAuthService.clientId}" +
            "&response_type=code" +
            "&redirect_uri=$redirectUri" +
            // read = public profile, activity:read_all = all activities including private,
            // profile:read_all = full profile details.
            "&scope=read,activity:read_all,profile:read_all" +
            "&approval_prompt=auto"

        return mapOf("url" to url)
    }

    /**
     * Exchanges an authorization code for tokens.
     * The frontend redirects here after Strava OAuth callback.
     */
    @GetMapping("/strava/callback")
    fun handleCallback(@RequestParam code: String): Map<String, Any> {
        val tokens = stravaOAuthService.exchangeCodeForTokens(code)
        return mapOf(
            "success" to true,
            "athlete" to (tokens["athlete"] ?: emptyMap<String, Any>()),
            "refresh_token" to (tokens["refresh_token"] ?: ""),
        )
    }

    @GetMapping("/strava/status")
    fun getStatus(): Map<String, Boolean> {
        return mapOf("authenticated" to stravaOAuthService.hasValidToken())
    }
}
