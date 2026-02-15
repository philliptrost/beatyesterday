package com.beatyesterday.application.import

import com.beatyesterday.domain.activity.ActivityId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Orchestrates the full import pipeline.
 * Runs each import stage sequentially in the correct order.
 */
@Service
class RunImportUseCase(
    private val importAthlete: ImportAthleteUseCase,
    private val importActivities: ImportActivitiesUseCase,
    private val importGear: ImportGearUseCase,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun execute(restrictToActivityIds: List<ActivityId>? = null) {
        val startTime = System.currentTimeMillis()
        logger.info("=== Starting import pipeline ===")

        // Stage 1: Import athlete profile
        importAthlete.execute()

        // Stage 2: Import activities
        importActivities.execute(restrictToIds = restrictToActivityIds)

        // Stage 3: Import gear referenced by activities
        importGear.execute()

        val duration = (System.currentTimeMillis() - startTime) / 1000.0
        logger.info("=== Import pipeline complete in %.1f seconds ===".format(duration))
    }
}
