package com.beatyesterday.domain.activity

import com.beatyesterday.domain.common.Coordinate
import com.beatyesterday.domain.common.Kilometer
import com.beatyesterday.domain.common.KmPerHour
import com.beatyesterday.domain.common.Meter
import com.beatyesterday.domain.gear.GearId
import java.time.Instant

/**
 * Core domain entity representing a single athletic activity (e.g. a run, ride, swim).
 *
 * This is the central model of the app — most features revolve around importing,
 * storing, and displaying activities. Fields use value objects (Kilometer, Meter, etc.)
 * instead of raw doubles to enforce unit correctness throughout the codebase.
 *
 * Nullable fields (calories, heartRate, power, etc.) reflect that not all Strava activities
 * have sensor data — it depends on what devices the athlete uses.
 */
data class Activity(
    val id: ActivityId,
    val startDateTime: Instant,
    val sportType: SportType,
    val name: String,
    val description: String?,
    val distance: Kilometer,
    val elevation: Meter,
    val startingCoordinate: Coordinate?,
    val calories: Int?,
    val averagePower: Int?,               // Watts — only present if a power meter is used
    val maxPower: Int?,
    val averageSpeed: KmPerHour,
    val maxSpeed: KmPerHour,
    val averageHeartRate: Int?,            // BPM — only present with a heart rate monitor
    val maxHeartRate: Int?,
    val averageCadence: Int?,              // RPM — steps/min for running, pedal RPM for cycling
    val movingTimeInSeconds: Int,          // Strava's "moving time" excludes time stopped
    val kudoCount: Int,
    val deviceName: String?,
    val polyline: String?,                 // Google-encoded polyline for route mapping (future feature)
    val gearId: GearId?,                   // Links to the athlete's equipment (e.g. bike, shoes)
    val isCommute: Boolean,
) {
    /** Formats moving time as H:MM:SS or M:SS for display in the UI */
    val movingTimeFormatted: String
        get() {
            val hours = movingTimeInSeconds / 3600
            val minutes = (movingTimeInSeconds % 3600) / 60
            val seconds = movingTimeInSeconds % 60
            return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
            else "%d:%02d".format(minutes, seconds)
        }

    /** Used for aggregations (monthly/yearly stats) where decimal hours are more useful */
    val movingTimeInHours: Double
        get() = movingTimeInSeconds / 3600.0

    /** Deep link back to this activity on strava.com */
    val stravaUrl: String
        get() = "https://www.strava.com/activities/${id.toStravaId()}"

    companion object {
        /**
         * Factory method to create an Activity from raw Strava API JSON.
         *
         * Strava returns untyped JSON (Map<String, Any?>), so we do safe casts throughout.
         * See: https://developers.strava.com/docs/reference/#api-Activities
         */
        fun fromStravaData(data: Map<String, Any?>): Activity {
            // Strava has both "sport_type" (newer, more specific) and "type" (legacy, broader)
            val sportTypeValue = data["sport_type"] as? String ?: data["type"] as? String ?: "Workout"
            // Strava returns speeds in meters/second — we convert to km/h for our domain
            val avgSpeedMps = (data["average_speed"] as? Number)?.toDouble() ?: 0.0
            val maxSpeedMps = (data["max_speed"] as? Number)?.toDouble() ?: 0.0
            val startLatLng = data["start_latlng"] as? List<*>

            return Activity(
                id = ActivityId.fromStravaId((data["id"] as Number).toLong()),
                startDateTime = Instant.parse(
                    (data["start_date"] as String)
                ),
                // Fall back to WORKOUT if Strava adds a new sport type we don't recognize yet
                sportType = try {
                    SportType.fromStravaValue(sportTypeValue)
                } catch (e: IllegalArgumentException) {
                    SportType.WORKOUT
                },
                name = data["name"] as? String ?: "Untitled Activity",
                description = data["description"] as? String,
                distance = Kilometer.fromMeters((data["distance"] as? Number)?.toDouble() ?: 0.0), // Strava sends meters
                elevation = Meter((data["total_elevation_gain"] as? Number)?.toDouble() ?: 0.0), // Total climbed, in meters
                startingCoordinate = Coordinate.fromLatLng(startLatLng),
                calories = (data["calories"] as? Number)?.toInt(),
                averagePower = (data["average_watts"] as? Number)?.toInt(),
                maxPower = (data["max_watts"] as? Number)?.toInt(),
                averageSpeed = KmPerHour.fromMetersPerSecond(avgSpeedMps),
                maxSpeed = KmPerHour.fromMetersPerSecond(maxSpeedMps),
                averageHeartRate = (data["average_heartrate"] as? Number)?.toInt(),
                maxHeartRate = (data["max_heartrate"] as? Number)?.toInt(),
                averageCadence = (data["average_cadence"] as? Number)?.toInt(),
                movingTimeInSeconds = (data["moving_time"] as? Number)?.toInt() ?: 0,
                kudoCount = (data["kudos_count"] as? Number)?.toInt() ?: 0,
                deviceName = data["device_name"] as? String,
                polyline = (data["map"] as? Map<*, *>)?.get("summary_polyline") as? String, // Nested inside Strava's "map" object
                gearId = (data["gear_id"] as? String)?.let { GearId.fromStravaId(it) },
                isCommute = data["commute"] as? Boolean ?: false,
            )
        }
    }
}
