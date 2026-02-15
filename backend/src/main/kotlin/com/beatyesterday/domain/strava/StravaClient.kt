package com.beatyesterday.domain.strava

/**
 * Port (interface) for Strava API access.
 * The domain defines what it needs; infrastructure provides the implementation.
 */
interface StravaClient {
    /** Returns the authenticated user's athlete profile. */
    fun getAthlete(): Map<String, Any?>

    /** Paginated fetch of activity summaries. Strava allows max 200 per page. */
    fun getActivities(page: Int, perPage: Int = 200): List<Map<String, Any?>>

    /** Gets full detailed data for a single activity (includes splits, laps, etc.). */
    fun getActivityDetail(activityId: Long): Map<String, Any?>

    /** Gets equipment details (bike or shoe) by Strava gear ID. */
    fun getGear(gearId: String): Map<String, Any?>
}
