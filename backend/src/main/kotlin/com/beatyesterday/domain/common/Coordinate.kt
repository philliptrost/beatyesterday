package com.beatyesterday.domain.common

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        fun fromLatLng(latLng: List<*>?): Coordinate? {
            if (latLng == null || latLng.size < 2) return null
            val lat = (latLng[0] as? Number)?.toDouble() ?: return null
            val lng = (latLng[1] as? Number)?.toDouble() ?: return null
            return Coordinate(lat, lng)
        }
    }
}
