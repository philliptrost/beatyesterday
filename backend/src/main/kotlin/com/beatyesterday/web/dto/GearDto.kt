package com.beatyesterday.web.dto

import com.beatyesterday.domain.gear.Gear

/** DTO for gear items. Includes both distanceKm and distanceM so the frontend can display either without conversion. */
data class GearDto(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val distanceM: Double,
    val isRetired: Boolean,
) {
    companion object {
        fun from(gear: Gear) = GearDto(
            id = gear.id.value,
            name = gear.name,
            distanceKm = gear.distanceInKm.value,
            distanceM = gear.distance.value,
            isRetired = gear.isRetired,
        )
    }
}
