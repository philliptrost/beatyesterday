package com.beatyesterday.domain.common

/** Speed in km/h. Strava sends speed as m/s; use [fromMetersPerSecond] to convert. */
@JvmInline
value class KmPerHour(val value: Double) {
    fun toMph(): Double = value * 0.621371
    fun toMetersPerSecond(): Double = value / 3.6

    /** Pace format (seconds per km), useful for running activities. */
    fun toSecPerKm(): Double {
        if (value <= 0.0) return 0.0
        return 3600.0 / value
    }

    override fun toString(): String = "%.1f".format(value)

    companion object {
        val ZERO = KmPerHour(0.0)

        fun fromMetersPerSecond(mps: Double): KmPerHour = KmPerHour(mps * 3.6)
    }
}
