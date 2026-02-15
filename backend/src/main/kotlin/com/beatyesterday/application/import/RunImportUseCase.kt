package com.beatyesterday.application.import

import com.beatyesterday.domain.activity.ActivityId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Orchestrates the full import pipeline — the top-level "run everything" use case.
 *
 * Runs the three import stages sequentially in a fixed order:
 *   1. Athlete  ->  2. Activities  ->  3. Gear
 *
 * Order matters because each stage depends on data from the previous one:
 *   - Athlete must exist before activities so the dashboard has profile data to display.
 *   - Activities must exist before gear because gear IDs are discovered from imported activities.
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
