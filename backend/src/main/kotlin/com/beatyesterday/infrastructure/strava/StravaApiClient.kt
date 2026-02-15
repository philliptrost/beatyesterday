package com.beatyesterday.infrastructure.strava

import com.beatyesterday.domain.strava.StravaClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

/**
 * "Adapter" implementation of the StravaClient port. Uses Spring's RestClient to call
 * the Strava v3 API. Handles rate limiting (429) with a 60-second sleep and retries.
 */
@Service
class StravaApiClient(
    private val oauthService: StravaOAuthService,
    @Value("\${strava.api-base-url}") private val baseUrl: String,
) : StravaClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()
    private val apiBase = "$baseUrl/api/v3"

    override fun getAthlete(): Map<String, Any?> {
        logger.info("Fetching athlete profile")
        return authorizedGet("/athlete")
    }

    override fun getActivities(page: Int, perPage: Int): List<Map<String, Any?>> {
        logger.info("Fetching activities page=$page perPage=$perPage")
        return authorizedGetList("/athlete/activities?page=$page&per_page=$perPage")
    }

    override fun getActivityDetail(activityId: Long): Map<String, Any?> {
        logger.debug("Fetching activity detail for $activityId")
        return authorizedGet("/activities/$activityId")
    }

    override fun getGear(gearId: String): Map<String, Any?> {
        logger.debug("Fetching gear $gearId")
        return authorizedGet("/gear/$gearId")
    }

    // Attaches the OAuth Bearer token to every request. Handles rate limiting
    // (429 -> wait 60s and retry) and not-found (404 -> return empty).
    private fun authorizedGet(path: String): Map<String, Any?> {
        return try {
            restClient.get()
                .uri("$apiBase$path")
                .header("Authorization", "Bearer ${oauthService.getAccessToken()}")
                .retrieve()
                .body(object : ParameterizedTypeReference<Map<String, Any?>>() {})
                ?: emptyMap()
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                logger.warn("Strava rate limit reached. Waiting 60 seconds...")
                Thread.sleep(60_000)
                return authorizedGet(path)
            }
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                logger.warn("Strava resource not found: $path")
                return emptyMap()
            }
            throw e
        }
    }

    // Separate method from authorizedGet because of Java type erasure — ParameterizedTypeReference
    // needs the exact generic type (List<Map<...>> vs Map<...>) to deserialize correctly.
    private fun authorizedGetList(path: String): List<Map<String, Any?>> {
        return try {
            restClient.get()
                .uri("$apiBase$path")
                .header("Authorization", "Bearer ${oauthService.getAccessToken()}")
                .retrieve()
                .body(object : ParameterizedTypeReference<List<Map<String, Any?>>>() {})
                ?: emptyList()
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                logger.warn("Strava rate limit reached. Waiting 60 seconds...")
                Thread.sleep(60_000)
                return authorizedGetList(path)
            }
            throw e
        }
    }
}
