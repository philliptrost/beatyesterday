package com.beatyesterday.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate

/**
 * JPA entity for the athlete table. rawData uses Hibernate's @JdbcTypeCode(SqlTypes.JSON)
 * to map to PostgreSQL's JSONB column type.
 */
@Entity
@Table(name = "athlete")
class AthleteEntity(
    @Id
    val id: String,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(name = "profile_image", length = 1024)
    val profileImage: String?,

    @Column(length = 1)
    val sex: String?,

    @Column(name = "birth_date")
    val birthDate: LocalDate?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", nullable = false, columnDefinition = "jsonb")
    val rawData: Map<String, Any?> = emptyMap(),
)
