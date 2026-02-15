package com.beatyesterday.infrastructure.persistence.mapper

// Extension functions that convert between domain models and JPA entities.
// This "anti-corruption layer" ensures the domain stays clean -- domain models use
// value objects (Kilometer, Meter, etc.) while entities use raw primitives for JPA compatibility.

import com.beatyesterday.domain.activity.Activity
import com.beatyesterday.domain.activity.ActivityId
import com.beatyesterday.domain.activity.SportType
import com.beatyesterday.domain.common.Coordinate
import com.beatyesterday.domain.common.Kilometer
import com.beatyesterday.domain.common.KmPerHour
import com.beatyesterday.domain.common.Meter
import com.beatyesterday.domain.gear.GearId
import com.beatyesterday.infrastructure.persistence.entity.ActivityEntity

fun ActivityEntity.toDomain(): Activity = Activity(
    id = ActivityId(id),
    startDateTime = startDateTime,
    sportType = try {
        SportType.fromStravaValue(sportType)
    } catch (e: IllegalArgumentException) {
        SportType.WORKOUT
    },
    name = name,
    description = description,
    distance = Kilometer(distanceKm),
    elevation = Meter(elevationM),
    startingCoordinate = if (startLatitude != null && startLongitude != null)
        Coordinate(startLatitude, startLongitude) else null,
    calories = calories,
    averagePower = averagePower,
    maxPower = maxPower,
    averageSpeed = KmPerHour(averageSpeedKmh),
    maxSpeed = KmPerHour(maxSpeedKmh),
    averageHeartRate = averageHeartRate,
    maxHeartRate = maxHeartRate,
    averageCadence = averageCadence,
    movingTimeInSeconds = movingTimeSeconds,
    kudoCount = kudoCount,
    deviceName = deviceName,
    polyline = polyline,
    gearId = gearId?.let { GearId(it) },
    isCommute = isCommute,
)

fun Activity.toEntity(): ActivityEntity = ActivityEntity(
    id = id.value,
    startDateTime = startDateTime,
    sportType = sportType.stravaValue,
    name = name,
    description = description,
    distanceKm = distance.value,
    elevationM = elevation.value,
    startLatitude = startingCoordinate?.latitude,
    startLongitude = startingCoordinate?.longitude,
    calories = calories,
    averagePower = averagePower,
    maxPower = maxPower,
    averageSpeedKmh = averageSpeed.value,
    maxSpeedKmh = maxSpeed.value,
    averageHeartRate = averageHeartRate,
    maxHeartRate = maxHeartRate,
    averageCadence = averageCadence,
    movingTimeSeconds = movingTimeInSeconds,
    kudoCount = kudoCount,
    deviceName = deviceName,
    polyline = polyline,
    gearId = gearId?.value,
    isCommute = isCommute,
)
