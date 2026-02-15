package com.beatyesterday.domain.activity

/**
 * Type-safe wrapper for activity identifiers. Stores IDs with an "activity-" prefix
 * (e.g., "activity-12345") to distinguish them from raw Strava numeric IDs and
 * prevent accidental misuse of plain strings/longs.
 *
 * @JvmInline ensures this is a zero-overhead wrapper at runtime -- the JVM sees
 * a plain String, not a wrapper object, so there is no allocation cost.
 */
@JvmInline
value class ActivityId(val value: String) {
    fun toStravaId(): Long = value.removePrefix("activity-").toLong()

    companion object {
        fun fromStravaId(stravaId: Long): ActivityId = ActivityId("activity-$stravaId")
    }
}
