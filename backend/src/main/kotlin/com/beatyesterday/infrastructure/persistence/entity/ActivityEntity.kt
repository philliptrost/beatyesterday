package com.beatyesterday.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "activity",
    indexes = [
        Index(name = "idx_activity_start_date", columnList = "start_date_time"),
        Index(name = "idx_activity_sport_type", columnList = "sport_type"),
        Index(name = "idx_activity_gear_id", columnList = "gear_id"),
    ]
)
class ActivityEntity(
    @Id
    val id: String,

    @Column(name = "start_date_time", nullable = false)
    val startDateTime: Instant,

    @Column(name = "sport_type", nullable = false, length = 50)
    val sportType: String,

    @Column(nullable = false, length = 500)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    @Column(name = "distance_km", nullable = false)
    val distanceKm: Double,

    @Column(name = "elevation_m", nullable = false)
    val elevationM: Double,

    @Column(name = "start_latitude")
    val startLatitude: Double?,

    @Column(name = "start_longitude")
    val startLongitude: Double?,

    val calories: Int?,

    @Column(name = "average_power")
    val averagePower: Int?,

    @Column(name = "max_power")
    val maxPower: Int?,

    @Column(name = "average_speed_kmh", nullable = false)
    val averageSpeedKmh: Double,

    @Column(name = "max_speed_kmh", nullable = false)
    val maxSpeedKmh: Double,

    @Column(name = "average_heart_rate")
    val averageHeartRate: Int?,

    @Column(name = "max_heart_rate")
    val maxHeartRate: Int?,

    @Column(name = "average_cadence")
    val averageCadence: Int?,

    @Column(name = "moving_time_seconds", nullable = false)
    val movingTimeSeconds: Int,

    @Column(name = "kudo_count", nullable = false)
    val kudoCount: Int = 0,

    @Column(name = "device_name")
    val deviceName: String?,

    @Column(columnDefinition = "TEXT")
    val polyline: String?,

    @Column(name = "gear_id")
    val gearId: String?,

    @Column(name = "is_commute", nullable = false)
    val isCommute: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
