package com.beatyesterday.infrastructure.persistence.mapper

import com.beatyesterday.domain.common.Meter
import com.beatyesterday.domain.gear.Gear
import com.beatyesterday.domain.gear.GearId
import com.beatyesterday.infrastructure.persistence.entity.GearEntity

fun GearEntity.toDomain(): Gear = Gear(
    id = GearId(id),
    name = name,
    distance = Meter(distanceM),
    isRetired = isRetired,
    createdOn = createdOn,
)

fun Gear.toEntity(): GearEntity = GearEntity(
    id = id.value,
    name = name,
    distanceM = distance.value,
    isRetired = isRetired,
    createdOn = createdOn,
)
