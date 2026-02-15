package com.beatyesterday.infrastructure.persistence.repository

import com.beatyesterday.domain.athlete.Athlete
import com.beatyesterday.domain.athlete.AthleteRepository
import com.beatyesterday.infrastructure.persistence.mapper.toDomain
import com.beatyesterday.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Repository

@Repository
class JpaAthleteRepository(
    private val springDataRepo: SpringDataAthleteRepository,
) : AthleteRepository {

    override fun save(athlete: Athlete) {
        springDataRepo.save(athlete.toEntity())
    }

    override fun find(): Athlete? =
        springDataRepo.findAll().firstOrNull()?.toDomain()
}
