package com.beatyesterday.infrastructure.persistence.repository

import com.beatyesterday.infrastructure.persistence.entity.ActivityEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA auto-generates the implementation at runtime. We only need to define
 * the interface and Spring creates the SQL queries. findBySportType uses Spring Data's
 * method name conventions to generate a WHERE clause.
 */
interface SpringDataActivityRepository : JpaRepository<ActivityEntity, String> {
    fun findBySportType(sportType: String, pageable: Pageable): Page<ActivityEntity>
}
