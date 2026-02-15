package com.beatyesterday.application.import

import com.beatyesterday.domain.activity.Activity
import com.beatyesterday.domain.activity.ActivityId
import com.beatyesterday.domain.activity.ActivityRepository
import com.beatyesterday.domain.strava.StravaClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Fetches activities from the Strava API and saves new ones to the local database.
 *
 * Supports two modes:
 *   - Full paginated import (initial sync or periodic refresh): walks through every page
 *     of the athlete's activity list from Strava.
 *   - Targeted import by activity IDs: fetches and saves only the specified activities,
 *     useful for webhook-driven incremental imports.
 */
@Service
class ImportActivitiesUseCase(
    private val stravaClient: StravaClient,
    private val activityRepository: ActivityRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Imports activities from Strava.
     * If restrictToIds is provided, only those specific activities are imported.
     * Otherwise, performs a full paginated import of all activities.
     */
    fun execute(restrictToIds: List<ActivityId>? = null) {
        if (restrictToIds != null) {
            logger.info("Importing ${restrictToIds.size} specific activities...")
            restrictToIds.forEach { importSingle(it) }
            return
        }

        logger.info("Starting full activity import from Strava...")
        var page = 1
        var totalImported = 0
        var totalSkipped = 0

        // Paginate through all Strava activities — Strava returns max 200 per page,
        // empty list means we've reached the end.
        while (true) {
            val rawActivities = stravaClient.getActivities(page = page)
            if (rawActivities.isEmpty()) {
                logger.info("No more activities on page $page, import complete")
                break
            }

            for (raw in rawActivities) {
                val id = (raw["id"] as? Number)?.toLong() ?: continue
                val activityId = ActivityId.fromStravaId(id)

                // Idempotency — skip activities already in our DB so re-imports are safe.
                if (activityRepository.exists(activityId)) {
                    totalSkipped++
                    continue
                }

                val activity = Activity.fromStravaData(raw)
                activityRepository.save(activity)
                totalImported++
            }

            logger.info("Processed page $page: ${rawActivities.size} activities")
            page++
        }

        logger.info("Import complete: $totalImported new, $totalSkipped skipped")
    }

    private fun importSingle(id: ActivityId) {
        val raw = stravaClient.getActivityDetail(id.toStravaId())
        if (raw.isEmpty()) {
            logger.warn("Activity ${id.value} not found on Strava")
            return
        }
        val activity = Activity.fromStravaData(raw)
        activityRepository.save(activity)
        logger.info("Imported activity: ${activity.name}")
    }
}
