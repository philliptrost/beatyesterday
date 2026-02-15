package com.beatyesterday.application.import

import com.beatyesterday.domain.athlete.Athlete
import com.beatyesterday.domain.athlete.AthleteRepository
import com.beatyesterday.domain.strava.StravaClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Simplest import use case — fetches the authenticated athlete's profile from Strava
 * and upserts it into the local database.
 *
 * Always runs first in the import pipeline so the dashboard has profile data
 * (name, avatar, etc.) to display even before activities finish importing.
 */
@Service
class ImportAthleteUseCase(
    private val stravaClient: StravaClient,
    private val athleteRepository: AthleteRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun execute() {
        logger.info("Importing athlete profile...")
        val rawData = stravaClient.getAthlete()
        val athlete = Athlete.fromStravaData(rawData)
        athleteRepository.save(athlete)
        logger.info("Imported athlete: ${athlete.fullName}")
    }
}
