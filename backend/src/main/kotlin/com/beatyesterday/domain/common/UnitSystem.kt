package com.beatyesterday.domain.common

/** Metric vs Imperial unit system. Defaults to METRIC; imperial is ready for future use. */
enum class UnitSystem {
    METRIC,
    IMPERIAL;

    val distanceLabel: String
        get() = when (this) {
            METRIC -> "km"
            IMPERIAL -> "mi"
        }

    val elevationLabel: String
        get() = when (this) {
            METRIC -> "m"
            IMPERIAL -> "ft"
        }

    val speedLabel: String
        get() = when (this) {
            METRIC -> "km/h"
            IMPERIAL -> "mph"
        }
}
