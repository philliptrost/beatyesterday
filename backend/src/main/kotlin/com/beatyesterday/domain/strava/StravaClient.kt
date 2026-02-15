package com.beatyesterday.domain.strava

/**
 * Port (interface) for Strava API access.
 * The domain defines what it needs; infrastructure provides the implementation.
 */
interface StravaClient {
    fun getAthlete(): Map<String, Any?>
    fun getActivities(page: Int, perPage: Int = 200): List<Map<String, Any?>>
    fun getActivityDetail(activityId: Long): Map<String, Any?>
    fun getGear(gearId: String): Map<String, Any?>
}
