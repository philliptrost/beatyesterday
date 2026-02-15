package com.beatyesterday.infrastructure.persistence.repository

import com.beatyesterday.infrastructure.persistence.entity.ActivityEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataActivityRepository : JpaRepository<ActivityEntity, String> {
    fun findBySportType(sportType: String, pageable: Pageable): Page<ActivityEntity>
}
