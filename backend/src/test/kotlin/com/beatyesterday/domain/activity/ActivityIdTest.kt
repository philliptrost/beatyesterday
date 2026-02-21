package com.beatyesterday.domain.activity

import com.beatyesterday.domain.gear.GearId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ActivityIdTest {

    @Test
    fun `fromStravaId creates prefixed ID`() {
        val id = ActivityId.fromStravaId(12345L)
        assertEquals("activity-12345", id.value)
    }

    @Test
    fun `toStravaId strips prefix`() {
        assertEquals(12345L, ActivityId("activity-12345").toStravaId())
    }
}

class GearIdTest {

    @Test
    fun `fromStravaId creates prefixed ID`() {
        val id = GearId.fromStravaId("b12345678")
        assertEquals("gear-b12345678", id.value)
    }

    @Test
    fun `toStravaId strips prefix`() {
        assertEquals("b12345678", GearId("gear-b12345678").toStravaId())
    }

    @Test
    fun `none creates gear-none ID`() {
        assertEquals("gear-none", GearId.none().value)
    }
}
