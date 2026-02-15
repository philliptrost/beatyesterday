package com.beatyesterday.domain.common

/**
 * Value class for distances in kilometers. Using a value class instead of a raw Double
 * prevents accidentally mixing up units (e.g., passing meters where km is expected).
 * Supports conversion to miles for future imperial mode via [toMiles].
 *
 * @JvmInline means no runtime allocation overhead -- the JVM sees a plain Double.
 */
@JvmInline
value class Kilometer(val value: Double) : Comparable<Kilometer> {
    fun toMiles(): Double = value * MILES_FACTOR
    fun toMeter(): Meter = Meter(value * 1000.0)

    fun toUnitSystem(unitSystem: UnitSystem): Double = when (unitSystem) {
        UnitSystem.METRIC -> value
        UnitSystem.IMPERIAL -> toMiles()
    }

    operator fun plus(other: Kilometer): Kilometer = Kilometer(value + other.value)
    override fun compareTo(other: Kilometer): Int = value.compareTo(other.value)
    override fun toString(): String = "%.2f".format(value)

    companion object {
        private const val MILES_FACTOR = 0.621371
        val ZERO = Kilometer(0.0)

        fun fromMeters(meters: Double): Kilometer = Kilometer(meters / 1000.0)
    }
}
