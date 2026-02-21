package com.beatyesterday.web.controller

import com.beatyesterday.domain.activity.*
import com.beatyesterday.domain.common.Kilometer
import com.beatyesterday.domain.common.KmPerHour
import com.beatyesterday.domain.common.Meter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

class ActivityControllerTest {

    private val mockRepository = mockk<ActivityRepository>()
    private lateinit var controller: ActivityController

    private val sampleActivity = Activity(
        id = ActivityId.fromStravaId(1L),
        startDateTime = Instant.parse("2025-01-15T10:00:00Z"),
        sportType = SportType.RUN,
        name = "Morning Run",
        description = null,
        distance = Kilometer(10.0),
        elevation = Meter(100.0),
        startingCoordinate = null,
        calories = 500,
        averagePower = null,
        maxPower = null,
        averageSpeed = KmPerHour(12.0),
        maxSpeed = KmPerHour(15.0),
        averageHeartRate = 150,
        maxHeartRate = 175,
        averageCadence = 170,
        movingTimeInSeconds = 3000,
        kudoCount = 5,
        deviceName = "Garmin",
        polyline = null,
        gearId = null,
        isCommute = false,
    )

    @BeforeEach
    fun setUp() {
        controller = ActivityController(mockRepository)
    }

    @Test
    fun `getActivities with valid sort field succeeds`() {
        val page = PageImpl(listOf(sampleActivity))
        every { mockRepository.findAll(any<Pageable>()) } returns page

        val result = controller.getActivities(
            page = 0, size = 50, sort = "startDateTime", direction = "desc", sportType = null
        )

        assertEquals(1, result.totalElements)
    }

    @Test
    fun `getActivities with invalid sort field returns 400`() {
        val ex = assertThrows<ResponseStatusException> {
            controller.getActivities(
                page = 0, size = 50, sort = "badField", direction = "desc", sportType = null
            )
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `getActivities accepts all valid sort fields`() {
        val validFields = listOf(
            "startDateTime", "sportType", "name", "distanceKm", "elevationM",
            "averageSpeedKmh", "movingTimeSeconds", "averageHeartRate", "kudoCount", "calories"
        )
        val page = PageImpl(listOf(sampleActivity))
        every { mockRepository.findAll(any<Pageable>()) } returns page

        for (field in validFields) {
            // Should not throw
            controller.getActivities(page = 0, size = 50, sort = field, direction = "desc", sportType = null)
        }
    }

    @Test
    fun `getActivities filters by sportType when provided`() {
        val page = PageImpl(listOf(sampleActivity))
        every { mockRepository.findAllBySportType(SportType.RUN, any()) } returns page

        val result = controller.getActivities(
            page = 0, size = 50, sort = "startDateTime", direction = "desc", sportType = "Run"
        )

        assertEquals(1, result.totalElements)
        verify { mockRepository.findAllBySportType(SportType.RUN, any()) }
    }

    @Test
    fun `getActivities without sportType calls findAll`() {
        val page = PageImpl(listOf(sampleActivity))
        every { mockRepository.findAll(any<Pageable>()) } returns page

        controller.getActivities(
            page = 0, size = 50, sort = "startDateTime", direction = "desc", sportType = null
        )

        verify { mockRepository.findAll(any<Pageable>()) }
    }

    @Test
    fun `getActivity returns 404 for unknown id`() {
        every { mockRepository.findById(any()) } returns null

        val response = controller.getActivity("activity-99999")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `getActivity returns activity for valid id`() {
        every { mockRepository.findById(ActivityId("activity-1")) } returns sampleActivity

        val response = controller.getActivity("activity-1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("activity-1", response.body!!.id)
    }
}
