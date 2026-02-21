package com.beatyesterday.domain.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.abs

class ValueObjectsTest {

    // --- Kilometer ---

    @Test
    fun `Kilometer fromMeters converts correctly`() {
        assertEquals(5.0, Kilometer.fromMeters(5000.0).value)
    }

    @Test
    fun `Kilometer toMiles converts correctly`() {
        val miles = Kilometer(10.0).toMiles()
        assert(abs(miles - 6.21371) < 0.001) { "Expected ~6.21371 but got $miles" }
    }

    @Test
    fun `Kilometer toMeter round-trips`() {
        assertEquals(5000.0, Kilometer(5.0).toMeter().value)
    }

    @Test
    fun `Kilometer plus adds correctly`() {
        assertEquals(Kilometer(5.0), Kilometer(3.0) + Kilometer(2.0))
    }

    @Test
    fun `Kilometer ZERO is zero`() {
        assertEquals(0.0, Kilometer.ZERO.value)
    }

    @Test
    fun `Kilometer toUnitSystem returns km for METRIC`() {
        assertEquals(10.0, Kilometer(10.0).toUnitSystem(UnitSystem.METRIC))
    }

    @Test
    fun `Kilometer toUnitSystem returns miles for IMPERIAL`() {
        val result = Kilometer(10.0).toUnitSystem(UnitSystem.IMPERIAL)
        assert(abs(result - 6.21371) < 0.001)
    }

    // --- Meter ---

    @Test
    fun `Meter toFeet converts correctly`() {
        val feet = Meter(100.0).toFeet()
        assert(abs(feet - 328.084) < 0.001) { "Expected ~328.084 but got $feet" }
    }

    @Test
    fun `Meter toKilometer converts correctly`() {
        assertEquals(1.5, Meter(1500.0).toKilometer().value)
    }

    @Test
    fun `Meter plus adds correctly`() {
        assertEquals(Meter(300.0), Meter(100.0) + Meter(200.0))
    }

    @Test
    fun `Meter ZERO is zero`() {
        assertEquals(0.0, Meter.ZERO.value)
    }

    // --- KmPerHour ---

    @Test
    fun `KmPerHour fromMetersPerSecond converts correctly`() {
        assertEquals(36.0, KmPerHour.fromMetersPerSecond(10.0).value)
    }

    @Test
    fun `KmPerHour toMetersPerSecond round-trips`() {
        val mps = KmPerHour(36.0).toMetersPerSecond()
        assert(abs(mps - 10.0) < 0.001) { "Expected ~10.0 but got $mps" }
    }

    @Test
    fun `KmPerHour toSecPerKm calculates pace`() {
        assertEquals(300.0, KmPerHour(12.0).toSecPerKm())
    }

    @Test
    fun `KmPerHour toSecPerKm handles zero speed`() {
        assertEquals(0.0, KmPerHour(0.0).toSecPerKm())
    }

    @Test
    fun `KmPerHour ZERO is zero`() {
        assertEquals(0.0, KmPerHour.ZERO.value)
    }

    // --- Coordinate ---

    @Test
    fun `Coordinate fromLatLng parses valid list`() {
        val coord = Coordinate.fromLatLng(listOf(51.5074, -0.1278))
        assertEquals(51.5074, coord?.latitude)
        assertEquals(-0.1278, coord?.longitude)
    }

    @Test
    fun `Coordinate fromLatLng returns null for null input`() {
        assertEquals(null, Coordinate.fromLatLng(null))
    }

    @Test
    fun `Coordinate fromLatLng returns null for too-short list`() {
        assertEquals(null, Coordinate.fromLatLng(listOf(51.5074)))
    }

    @Test
    fun `Coordinate fromLatLng returns null for non-numeric values`() {
        assertEquals(null, Coordinate.fromLatLng(listOf("not", "numbers")))
    }

    // --- UnitSystem ---

    @Test
    fun `UnitSystem METRIC has correct labels`() {
        assertEquals("km", UnitSystem.METRIC.distanceLabel)
        assertEquals("m", UnitSystem.METRIC.elevationLabel)
        assertEquals("km/h", UnitSystem.METRIC.speedLabel)
    }

    @Test
    fun `UnitSystem IMPERIAL has correct labels`() {
        assertEquals("mi", UnitSystem.IMPERIAL.distanceLabel)
        assertEquals("ft", UnitSystem.IMPERIAL.elevationLabel)
        assertEquals("mph", UnitSystem.IMPERIAL.speedLabel)
    }
}
