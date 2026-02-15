package com.beatyesterday.domain.common

@JvmInline
value class Meter(val value: Double) : Comparable<Meter> {
    fun toKilometer(): Kilometer = Kilometer(value / 1000.0)
    fun toFeet(): Double = value * FEET_FACTOR

    fun toUnitSystem(unitSystem: UnitSystem): Double = when (unitSystem) {
        UnitSystem.METRIC -> value
        UnitSystem.IMPERIAL -> toFeet()
    }

    operator fun plus(other: Meter): Meter = Meter(value + other.value)
    override fun compareTo(other: Meter): Int = value.compareTo(other.value)
    override fun toString(): String = "%.0f".format(value)

    companion object {
        private const val FEET_FACTOR = 3.28084
        val ZERO = Meter(0.0)
    }
}
