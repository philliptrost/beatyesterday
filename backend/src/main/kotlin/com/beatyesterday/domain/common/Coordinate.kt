package com.beatyesterday.domain.common

/** GPS coordinate for an activity's starting location. */
data class Coordinate(
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        /** Parses a Strava [lat, lng] array into a Coordinate. Returns null if data is missing. */
        fun fromLatLng(latLng: List<*>?): Coordinate? {
            if (latLng == null || latLng.size < 2) return null
            val lat = (latLng[0] as? Number)?.toDouble() ?: return null
            val lng = (latLng[1] as? Number)?.toDouble() ?: return null
            return Coordinate(lat, lng)
        }
    }
}
