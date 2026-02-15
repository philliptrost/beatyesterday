package com.beatyesterday.infrastructure.strava

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class StravaOAuthService(
    @Value("\${strava.client-id}") val clientId: String,
    @Value("\${strava.client-secret}") private val clientSecret: String,
    @Value("\${strava.refresh-token}") private val refreshToken: String,
    @Value("\${strava.api-base-url}") private val baseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()
    private var cachedAccessToken: String? = null

    /**
     * Gets a valid access token, refreshing if needed.
     * Strava access tokens expire after 6 hours, but we cache for the session.
     */
    fun getAccessToken(): String {
        cachedAccessToken?.let { return it }

        logger.info("Refreshing Strava access token...")
        val response = restClient.post()
            .uri("$baseUrl/oauth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "grant_type" to "refresh_token",
                    "refresh_token" to refreshToken,
                )
            )
            .retrieve()
            .body(Map::class.java) as Map<String, Any>

        val token = response["access_token"] as String
        cachedAccessToken = token
        logger.info("Strava access token refreshed successfully")
        return token
    }

    /**
     * Exchanges an authorization code for a refresh token.
     * Used during the initial OAuth setup flow.
     */
    fun exchangeCodeForTokens(code: String): Map<String, Any> {
        val response = restClient.post()
            .uri("$baseUrl/oauth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "grant_type" to "authorization_code",
                    "code" to code,
                )
            )
            .retrieve()
            .body(Map::class.java) as Map<String, Any>

        return response
    }

    fun hasValidToken(): Boolean = try {
        getAccessToken()
        true
    } catch (e: Exception) {
        logger.debug("No valid Strava token: ${e.message}")
        false
    }

    fun clearCache() {
        cachedAccessToken = null
    }
}
