package com.beatyesterday.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "gear")
class GearEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val name: String,

    @Column(name = "distance_m", nullable = false)
    val distanceM: Double = 0.0,

    @Column(name = "is_retired", nullable = false)
    val isRetired: Boolean = false,

    @Column(name = "created_on", nullable = false)
    val createdOn: Instant = Instant.now(),
)
