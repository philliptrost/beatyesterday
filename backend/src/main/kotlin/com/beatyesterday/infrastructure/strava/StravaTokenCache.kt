package com.beatyesterday.infrastructure.strava

import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Interface for caching Strava access tokens.
 * Implementations can use in-memory cache (dev) or database (production).
 */
interface StravaTokenCache {
    /**
     * Get a valid access token from cache.
     * Returns null if no token exists or token is expired.
     */
    fun get(athleteId: String): String?

    /**
     * Store an access token in cache with 6-hour expiry.
     * Strava access tokens expire after 6 hours.
     */
    fun set(athleteId: String, accessToken: String)

    /**
     * Clear the cached token for an athlete.
     */
    fun clear(athleteId: String)
}

/**
 * In-memory token cache for local development.
 * Simple map-based implementation - does not work across multiple instances.
 */
@Component
@Profile("dev", "default")
class InMemoryTokenCache : StravaTokenCache {
    private data class CachedToken(
        val accessToken: String,
        val expiresAt: Instant
    )

    private val cache = mutableMapOf<String, CachedToken>()

    override fun get(athleteId: String): String? {
        val cached = cache[athleteId] ?: return null

        return if (Instant.now().isBefore(cached.expiresAt)) {
            cached.accessToken
        } else {
            // Token expired, remove from cache
            cache.remove(athleteId)
            null
        }
    }

    override fun set(athleteId: String, accessToken: String) {
        val expiresAt = Instant.now().plus(6, ChronoUnit.HOURS)
        cache[athleteId] = CachedToken(accessToken, expiresAt)
    }

    override fun clear(athleteId: String) {
        cache.remove(athleteId)
    }
}

/**
 * Database-backed token cache for production/cloud deployments.
 * Stores tokens in PostgreSQL for sharing across multiple instances.
 */
@Component
@Profile("!dev & !default")
class DatabaseTokenCache(
    private val jdbcTemplate: JdbcTemplate
) : StravaTokenCache {

    override fun get(athleteId: String): String? {
        val sql = """
            SELECT access_token
            FROM strava_tokens
            WHERE athlete_id = ?
              AND expires_at > CURRENT_TIMESTAMP
        """.trimIndent()

        return try {
            jdbcTemplate.queryForObject(sql, String::class.java, athleteId)
        } catch (e: Exception) {
            // No token found or expired
            null
        }
    }

    override fun set(athleteId: String, accessToken: String) {
        val sql = """
            INSERT INTO strava_tokens (athlete_id, access_token, expires_at)
            VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL '6 hours')
            ON CONFLICT (athlete_id)
            DO UPDATE SET
                access_token = EXCLUDED.access_token,
                expires_at = EXCLUDED.expires_at,
                updated_at = CURRENT_TIMESTAMP
        """.trimIndent()

        jdbcTemplate.update(sql, athleteId, accessToken)
    }

    override fun clear(athleteId: String) {
        val sql = "DELETE FROM strava_tokens WHERE athlete_id = ?"
        jdbcTemplate.update(sql, athleteId)
    }
}
