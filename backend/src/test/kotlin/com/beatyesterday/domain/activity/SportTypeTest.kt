package com.beatyesterday.domain.activity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SportTypeTest {

    @Test
    fun `fromStravaValue maps known types`() {
        assertEquals(SportType.TRAIL_RUN, SportType.fromStravaValue("TrailRun"))
        assertEquals(SportType.RIDE, SportType.fromStravaValue("Ride"))
        assertEquals(SportType.MOUNTAIN_BIKE_RIDE, SportType.fromStravaValue("MountainBikeRide"))
        assertEquals(SportType.SWIM, SportType.fromStravaValue("Swim"))
    }

    @Test
    fun `fromStravaValue throws for unknown type`() {
        assertThrows<IllegalArgumentException> {
            SportType.fromStravaValue("UnknownSportType")
        }
    }

    @Test
    fun `all entries have unique stravaValues`() {
        val values = SportType.entries.map { it.stravaValue }
        assertEquals(values.size, values.toSet().size, "Duplicate stravaValues found: ${values.groupBy { it }.filter { it.value.size > 1 }.keys}")
    }

    @Test
    fun `all sport types map to a valid activity type`() {
        SportType.entries.forEach { sportType ->
            // Should not throw — every sport type must have an activity type
            val activityType = sportType.activityType
            assert(activityType.displayName.isNotBlank()) { "${sportType.name} has blank activity type display name" }
        }
    }
}
