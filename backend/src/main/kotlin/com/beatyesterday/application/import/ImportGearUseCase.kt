package com.beatyesterday.application.import

import com.beatyesterday.domain.activity.ActivityRepository
import com.beatyesterday.domain.gear.Gear
import com.beatyesterday.domain.gear.GearId
import com.beatyesterday.domain.gear.GearRepository
import com.beatyesterday.domain.strava.StravaClient
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ImportGearUseCase(
    private val stravaClient: StravaClient,
    private val gearRepository: GearRepository,
    private val activityRepository: ActivityRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Finds all unique gear IDs referenced by activities and imports their details.
     */
    fun execute() {
        logger.info("Importing gear...")

        // Collect unique gear IDs from all activities
        val gearIds = mutableSetOf<GearId>()
        var page = 0
        while (true) {
            val activities = activityRepository.findAll(PageRequest.of(page, 500))
            activities.content.mapNotNull { it.gearId }.forEach { gearIds.add(it) }
            if (!activities.hasNext()) break
            page++
        }

        logger.info("Found ${gearIds.size} unique gear IDs")

        for (gearId in gearIds) {
            if (gearRepository.findById(gearId) != null) {
                continue
            }

            val rawData = stravaClient.getGear(gearId.toStravaId())
            if (rawData.isEmpty()) {
                logger.warn("Gear ${gearId.value} not found on Strava")
                continue
            }

            val gear = Gear.fromStravaData(rawData)
            gearRepository.save(gear)
            logger.info("Imported gear: ${gear.name}")
        }
    }
}
