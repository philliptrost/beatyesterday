package com.beatyesterday.infrastructure.persistence.mapper

import com.beatyesterday.domain.athlete.Athlete
import com.beatyesterday.infrastructure.persistence.entity.AthleteEntity

fun AthleteEntity.toDomain(): Athlete = Athlete(
    id = id,
    firstName = firstName,
    lastName = lastName,
    profileImageUrl = profileImage,
    sex = sex,
    birthDate = birthDate,
    rawData = rawData,
)

fun Athlete.toEntity(): AthleteEntity = AthleteEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    profileImage = profileImageUrl,
    sex = sex,
    birthDate = birthDate,
    rawData = rawData,
)
