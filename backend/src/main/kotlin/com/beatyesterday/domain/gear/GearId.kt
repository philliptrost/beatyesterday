package com.beatyesterday.domain.gear

@JvmInline
value class GearId(val value: String) {
    fun toStravaId(): String = value.removePrefix("gear-")

    companion object {
        fun fromStravaId(stravaId: String): GearId = GearId("gear-$stravaId")
        fun none(): GearId = GearId("gear-none")
    }
}
