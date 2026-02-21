package com.beatyesterday.infrastructure.strava

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import java.time.Instant

class StravaOAuthServiceTest {

    private val mockRestClient = mockk<RestClient>()
    private val mockRequestBodyUriSpec = mockk<RestClient.RequestBodyUriSpec>()
    private val mockRequestBodySpec = mockk<RestClient.RequestBodySpec>(relaxed = true)
    private val mockResponseSpec = mockk<RestClient.ResponseSpec>()

    private lateinit var service: StravaOAuthService

    @BeforeEach
    fun setUp() {
        service = StravaOAuthService(
            clientId = "test-client-id",
            clientSecret = "test-client-secret",
            refreshToken = "test-refresh-token",
            baseUrl = "https://www.strava.com",
        )
        // Replace the internally-created RestClient with our mock
        val field = StravaOAuthService::class.java.getDeclaredField("restClient")
        field.isAccessible = true
        field.set(service, mockRestClient)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun stubTokenResponse(accessToken: String, expiresAt: Long) {
        every { mockRestClient.post() } returns mockRequestBodyUriSpec
        every { mockRequestBodyUriSpec.uri(any<String>()) } returns mockRequestBodySpec
        every { mockRequestBodySpec.contentType(any()) } returns mockRequestBodySpec
        every { mockRequestBodySpec.body(any<Any>()) } returns mockRequestBodySpec
        every { mockRequestBodySpec.retrieve() } returns mockResponseSpec
        @Suppress("UNCHECKED_CAST")
        every { mockResponseSpec.body(Map::class.java) } returns mapOf(
            "access_token" to accessToken,
            "expires_at" to expiresAt,
        )
    }

    @Test
    fun `getAccessToken refreshes when no cached token`() {
        val futureExpiry = Instant.now().epochSecond + 21600 // 6 hours from now
        stubTokenResponse("fresh-token", futureExpiry)

        val token = service.getAccessToken()

        assertEquals("fresh-token", token)
        verify(exactly = 1) { mockRestClient.post() }
    }

    @Test
    fun `getAccessToken returns cached token when not expired`() {
        val futureExpiry = Instant.now().epochSecond + 21600
        stubTokenResponse("cached-token", futureExpiry)

        // First call — refreshes
        service.getAccessToken()
        // Second call — should use cache
        val token = service.getAccessToken()

        assertEquals("cached-token", token)
        verify(exactly = 1) { mockRestClient.post() } // Only one refresh call
    }

    @Test
    fun `getAccessToken refreshes when token expired`() {
        val pastExpiry = Instant.now().epochSecond - 100 // Already expired
        stubTokenResponse("first-token", pastExpiry)
        service.getAccessToken()

        // Now stub a new token for the refresh
        val futureExpiry = Instant.now().epochSecond + 21600
        stubTokenResponse("refreshed-token", futureExpiry)
        val token = service.getAccessToken()

        assertEquals("refreshed-token", token)
        verify(exactly = 2) { mockRestClient.post() } // Two refresh calls
    }

    @Test
    fun `getAccessToken refreshes within 60s buffer of expiry`() {
        // Token expires in 30 seconds — within the 60s buffer
        val nearExpiry = Instant.now().epochSecond + 30
        stubTokenResponse("about-to-expire-token", nearExpiry)
        service.getAccessToken()

        // Should refresh because we're within 60s of expiry
        val futureExpiry = Instant.now().epochSecond + 21600
        stubTokenResponse("new-token", futureExpiry)
        val token = service.getAccessToken()

        assertEquals("new-token", token)
        verify(exactly = 2) { mockRestClient.post() }
    }

    @Test
    fun `clearCache forces next call to refresh`() {
        val futureExpiry = Instant.now().epochSecond + 21600
        stubTokenResponse("token-1", futureExpiry)
        service.getAccessToken()

        service.clearCache()

        stubTokenResponse("token-2", futureExpiry)
        val token = service.getAccessToken()

        assertEquals("token-2", token)
        verify(exactly = 2) { mockRestClient.post() }
    }

    @Test
    fun `hasValidToken returns true with valid token`() {
        val futureExpiry = Instant.now().epochSecond + 21600
        stubTokenResponse("valid-token", futureExpiry)

        assertTrue(service.hasValidToken())
    }

    @Test
    fun `hasValidToken returns false when refresh fails`() {
        every { mockRestClient.post() } throws RuntimeException("Network error")

        assertFalse(service.hasValidToken())
    }
}
