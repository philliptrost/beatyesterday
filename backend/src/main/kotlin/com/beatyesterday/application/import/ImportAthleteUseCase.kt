package com.beatyesterday.application.import

import com.beatyesterday.domain.athlete.Athlete
import com.beatyesterday.domain.athlete.AthleteRepository
import com.beatyesterday.domain.strava.StravaClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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
