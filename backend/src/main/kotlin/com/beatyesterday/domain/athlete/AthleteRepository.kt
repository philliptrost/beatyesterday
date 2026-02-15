package com.beatyesterday.domain.athlete

interface AthleteRepository {
    fun save(athlete: Athlete)
    fun find(): Athlete?
}
