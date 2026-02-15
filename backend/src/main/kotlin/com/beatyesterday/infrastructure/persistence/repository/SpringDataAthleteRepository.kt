package com.beatyesterday.infrastructure.persistence.repository

import com.beatyesterday.infrastructure.persistence.entity.AthleteEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataAthleteRepository : JpaRepository<AthleteEntity, String>
