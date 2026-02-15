package com.beatyesterday.domain.gear

import com.beatyesterday.domain.common.Kilometer
import com.beatyesterday.domain.common.Meter
import java.time.Instant

data class Gear(
    val id: GearId,
    val name: String,
    val distance: Meter,
    val isRetired: Boolean,
    val createdOn: Instant,
) {
    val distanceInKm: Kilometer get() = distance.toKilometer()

    companion object {
        fun fromStravaData(data: Map<String, Any?>): Gear = Gear(
            id = GearId.fromStravaId(data["id"] as String),
            name = data["name"] as? String ?: "Unknown Gear",
            distance = Meter((data["distance"] as? Number)?.toDouble() ?: 0.0),
            isRetired = data["retired"] as? Boolean ?: false,
            createdOn = Instant.now(),
        )
    }
}
