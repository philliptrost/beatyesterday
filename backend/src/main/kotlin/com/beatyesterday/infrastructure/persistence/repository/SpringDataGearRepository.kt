package com.beatyesterday.infrastructure.persistence.repository

import com.beatyesterday.infrastructure.persistence.entity.GearEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataGearRepository : JpaRepository<GearEntity, String>
