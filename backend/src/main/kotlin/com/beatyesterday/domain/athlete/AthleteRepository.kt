package com.beatyesterday.domain.athlete

/**
 * Repository for the single-tenant athlete.
 */
interface AthleteRepository {
    fun save(athlete: Athlete)

    /** Returns the first (and only) athlete, or null if none has been imported yet. */
    fun find(): Athlete?
}
