package com.beatyesterday.infrastructure.strava

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

class StravaApiClientTest {

    private val mockOAuthService = mockk<StravaOAuthService>()
    private val mockRestClient = mockk<RestClient>()
    private val mockRequestHeadersUriSpec = mockk<RestClient.RequestHeadersUriSpec<*>>()
    private val mockRequestHeadersSpec = mockk<RestClient.RequestHeadersSpec<*>>()
    private val mockResponseSpec = mockk<RestClient.ResponseSpec>()

    private lateinit var client: StravaApiClient

    @BeforeEach
    fun setUp() {
        every { mockOAuthService.getAccessToken() } returns "test-token"

        client = StravaApiClient(
            oauthService = mockOAuthService,
            baseUrl = "https://www.strava.com",
        )
        // Replace the internally-created RestClient with our mock
        val restClientField = StravaApiClient::class.java.getDeclaredField("restClient")
        restClientField.isAccessible = true
        restClientField.set(client, mockRestClient)

        // Set retry delay to 1ms so 429 tests don't wait 60 seconds
        val delayField = StravaApiClient::class.java.getDeclaredField("retryDelayMs")
        delayField.isAccessible = true
        delayField.set(client, 1L)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun stubGetSuccess(response: Any?) {
        every { mockRestClient.get() } returns mockRequestHeadersUriSpec
        every { mockRequestHeadersUriSpec.uri(any<String>()) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.header(any(), any()) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.retrieve() } returns mockResponseSpec
        every { mockResponseSpec.body(any<ParameterizedTypeReference<*>>()) } returns response
    }

    private fun stubGetError(status: HttpStatus) {
        every { mockRestClient.get() } returns mockRequestHeadersUriSpec
        every { mockRequestHeadersUriSpec.uri(any<String>()) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.header(any(), any()) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.retrieve() } returns mockResponseSpec
        every { mockResponseSpec.body(any<ParameterizedTypeReference<*>>()) } throws
            HttpClientErrorException(status)
    }

    private fun stubGetErrorThenSuccess(status: HttpStatus, successResponse: Any?) {
        var callCount = 0
        every { mockRestClient.get() } returns mockRequestHeadersUriSpec
        every { mockRequestHeadersUriSpec.uri(any<String>()) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.header(any(), any()) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.retrieve() } returns mockResponseSpec
        every { mockResponseSpec.body(any<ParameterizedTypeReference<*>>()) } answers {
            callCount++
            if (callCount == 1) throw HttpClientErrorException(status)
            else successResponse
        }
    }

    // --- authorizedGet (tested via getAthlete/getGear) ---

    @Test
    fun `getAthlete returns data on success`() {
        val expected = mapOf<String, Any?>("id" to 12345, "firstname" to "Test")
        stubGetSuccess(expected)

        val result = client.getAthlete()

        assertEquals(expected, result)
    }

    @Test
    fun `getGear returns empty map on 404`() {
        stubGetError(HttpStatus.NOT_FOUND)

        val result = client.getGear("b12345")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAthlete retries on 429 then succeeds`() {
        val expected = mapOf<String, Any?>("id" to 12345)
        stubGetErrorThenSuccess(HttpStatus.TOO_MANY_REQUESTS, expected)

        val result = client.getAthlete()

        assertEquals(expected, result)
    }

    @Test
    fun `getAthlete throws after max retries on 429`() {
        stubGetError(HttpStatus.TOO_MANY_REQUESTS)

        assertThrows<HttpClientErrorException> {
            client.getAthlete()
        }
    }

    @Test
    fun `getAthlete refreshes token on 401 then succeeds`() {
        val expected = mapOf<String, Any?>("id" to 12345)
        stubGetErrorThenSuccess(HttpStatus.UNAUTHORIZED, expected)
        every { mockOAuthService.clearCache() } just runs

        val result = client.getAthlete()

        assertEquals(expected, result)
        verify(exactly = 1) { mockOAuthService.clearCache() }
    }

    @Test
    fun `getAthlete throws on 401 if already refreshed`() {
        // Always return 401
        stubGetError(HttpStatus.UNAUTHORIZED)
        every { mockOAuthService.clearCache() } just runs

        assertThrows<HttpClientErrorException> {
            client.getAthlete()
        }
        // clearCache called once, then the second 401 throws
        verify(exactly = 1) { mockOAuthService.clearCache() }
    }

    // --- authorizedGetList (tested via getActivities) ---

    @Test
    fun `getActivities returns data on success`() {
        val expected = listOf(mapOf<String, Any?>("id" to 1), mapOf<String, Any?>("id" to 2))
        stubGetSuccess(expected)

        val result = client.getActivities(page = 1, perPage = 200)

        assertEquals(2, result.size)
    }

    @Test
    fun `getActivities returns empty list on 404`() {
        stubGetError(HttpStatus.NOT_FOUND)

        val result = client.getActivities(page = 1)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getActivities retries on 429 then succeeds`() {
        val expected = listOf(mapOf<String, Any?>("id" to 1))
        stubGetErrorThenSuccess(HttpStatus.TOO_MANY_REQUESTS, expected)

        val result = client.getActivities(page = 1)

        assertEquals(1, result.size)
    }

    @Test
    fun `getActivities refreshes token on 401 then succeeds`() {
        val expected = listOf(mapOf<String, Any?>("id" to 1))
        stubGetErrorThenSuccess(HttpStatus.UNAUTHORIZED, expected)
        every { mockOAuthService.clearCache() } just runs

        val result = client.getActivities(page = 1)

        assertEquals(1, result.size)
        verify(exactly = 1) { mockOAuthService.clearCache() }
    }
}
