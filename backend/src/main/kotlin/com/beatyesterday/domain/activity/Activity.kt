package com.beatyesterday.domain.activity

import com.beatyesterday.domain.common.Coordinate
import com.beatyesterday.domain.common.Kilometer
import com.beatyesterday.domain.common.KmPerHour
import com.beatyesterday.domain.common.Meter
import com.beatyesterday.domain.gear.GearId
import java.time.Instant

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
    val averagePower: Int?,
    val maxPower: Int?,
    val averageSpeed: KmPerHour,
    val maxSpeed: KmPerHour,
    val averageHeartRate: Int?,
    val maxHeartRate: Int?,
    val averageCadence: Int?,
    val movingTimeInSeconds: Int,
    val kudoCount: Int,
    val deviceName: String?,
    val polyline: String?,
    val gearId: GearId?,
    val isCommute: Boolean,
) {
    val movingTimeFormatted: String
        get() {
            val hours = movingTimeInSeconds / 3600
            val minutes = (movingTimeInSeconds % 3600) / 60
            val seconds = movingTimeInSeconds % 60
            return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
            else "%d:%02d".format(minutes, seconds)
        }

    val movingTimeInHours: Double
        get() = movingTimeInSeconds / 3600.0

    val stravaUrl: String
        get() = "https://www.strava.com/activities/${id.toStravaId()}"

    companion object {
        /**
         * Creates an Activity from raw Strava API JSON data.
         * Maps Strava field names to our domain model.
         */
        fun fromStravaData(data: Map<String, Any?>): Activity {
            val sportTypeValue = data["sport_type"] as? String ?: data["type"] as? String ?: "Workout"
            val avgSpeedMps = (data["average_speed"] as? Number)?.toDouble() ?: 0.0
            val maxSpeedMps = (data["max_speed"] as? Number)?.toDouble() ?: 0.0
            val startLatLng = data["start_latlng"] as? List<*>

            return Activity(
                id = ActivityId.fromStravaId((data["id"] as Number).toLong()),
                startDateTime = Instant.parse(
                    (data["start_date"] as String)
                ),
                sportType = try {
                    SportType.fromStravaValue(sportTypeValue)
                } catch (e: IllegalArgumentException) {
                    SportType.WORKOUT
                },
                name = data["name"] as? String ?: "Untitled Activity",
                description = data["description"] as? String,
                distance = Kilometer.fromMeters((data["distance"] as? Number)?.toDouble() ?: 0.0),
                elevation = Meter((data["total_elevation_gain"] as? Number)?.toDouble() ?: 0.0),
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
                polyline = (data["map"] as? Map<*, *>)?.get("summary_polyline") as? String,
                gearId = (data["gear_id"] as? String)?.let { GearId.fromStravaId(it) },
                isCommute = data["commute"] as? Boolean ?: false,
            )
        }
    }
}
