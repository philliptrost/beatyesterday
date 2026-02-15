package com.beatyesterday.infrastructure.persistence.repository

import com.beatyesterday.domain.gear.Gear
import com.beatyesterday.domain.gear.GearId
import com.beatyesterday.domain.gear.GearRepository
import com.beatyesterday.infrastructure.persistence.mapper.toDomain
import com.beatyesterday.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Repository

@Repository
class JpaGearRepository(
    private val springDataRepo: SpringDataGearRepository,
) : GearRepository {

    override fun save(gear: Gear) {
        springDataRepo.save(gear.toEntity())
    }

    override fun findById(id: GearId): Gear? =
        springDataRepo.findById(id.value).orElse(null)?.toDomain()

    override fun findAll(): List<Gear> =
        springDataRepo.findAll().map { it.toDomain() }
}
