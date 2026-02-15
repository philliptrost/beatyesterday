package com.beatyesterday.domain.gear

interface GearRepository {
    fun save(gear: Gear)
    fun findById(id: GearId): Gear?
    fun findAll(): List<Gear>
}
