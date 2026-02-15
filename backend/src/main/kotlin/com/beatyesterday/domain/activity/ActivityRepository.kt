package com.beatyesterday.domain.activity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Port in hexagonal architecture: the domain layer defines the repository interface
 * it needs, and the infrastructure layer provides the JPA implementation
 * (JpaActivityRepository). This keeps the domain free of Spring/JPA dependencies
 * and makes it easy to swap storage (e.g., in-memory for tests).
 */
interface ActivityRepository {
    fun save(activity: Activity)
    fun findById(id: ActivityId): Activity?
    fun findAll(pageable: Pageable): Page<Activity>
    fun findAllBySportType(sportType: SportType, pageable: Pageable): Page<Activity>
    fun exists(id: ActivityId): Boolean
    fun count(): Long
    fun deleteById(id: ActivityId)
}
