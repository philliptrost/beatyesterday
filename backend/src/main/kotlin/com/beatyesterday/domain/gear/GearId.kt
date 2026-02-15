package com.beatyesterday.domain.gear

/**
 * Type-safe wrapper for gear identifiers, stored with a "gear-" prefix.
 * Strava gear IDs are strings like "b12345678" (bikes) or "g12345678" (shoes).
 * The prefix avoids ambiguity with other ID types in our domain.
 *
 * @JvmInline means zero-overhead wrapper at runtime.
 */
@JvmInline
value class GearId(val value: String) {
    fun toStravaId(): String = value.removePrefix("gear-")

    companion object {
        fun fromStravaId(stravaId: String): GearId = GearId("gear-$stravaId")
        fun none(): GearId = GearId("gear-none")
    }
}
