package com.beatyesterday.domain.activity

@JvmInline
value class ActivityId(val value: String) {
    fun toStravaId(): Long = value.removePrefix("activity-").toLong()

    companion object {
        fun fromStravaId(stravaId: Long): ActivityId = ActivityId("activity-$stravaId")
    }
}
