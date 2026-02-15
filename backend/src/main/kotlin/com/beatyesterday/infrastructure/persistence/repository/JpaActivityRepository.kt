package com.beatyesterday.infrastructure.persistence.repository

import com.beatyesterday.domain.activity.Activity
import com.beatyesterday.domain.activity.ActivityId
import com.beatyesterday.domain.activity.ActivityRepository
import com.beatyesterday.domain.activity.SportType
import com.beatyesterday.infrastructure.persistence.mapper.toDomain
import com.beatyesterday.infrastructure.persistence.mapper.toEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

/**
 * "Adapter" that bridges the domain's ActivityRepository interface to Spring Data JPA.
 * This is the hexagonal architecture pattern -- the domain defines the port (interface),
 * and this class is the adapter (implementation).
 */
@Repository
class JpaActivityRepository(
    private val springDataRepo: SpringDataActivityRepository,
) : ActivityRepository {

    override fun save(activity: Activity) {
        springDataRepo.save(activity.toEntity())
    }

    override fun findById(id: ActivityId): Activity? =
        springDataRepo.findById(id.value).orElse(null)?.toDomain()

    override fun findAll(pageable: Pageable): Page<Activity> =
        springDataRepo.findAll(pageable).map { it.toDomain() }

    override fun findAllBySportType(sportType: SportType, pageable: Pageable): Page<Activity> =
        springDataRepo.findBySportType(sportType.stravaValue, pageable).map { it.toDomain() }

    override fun exists(id: ActivityId): Boolean =
        springDataRepo.existsById(id.value)

    override fun count(): Long =
        springDataRepo.count()

    override fun deleteById(id: ActivityId) =
        springDataRepo.deleteById(id.value)
}
