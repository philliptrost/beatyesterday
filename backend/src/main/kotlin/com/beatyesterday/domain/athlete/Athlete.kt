package com.beatyesterday.domain.athlete

import java.time.LocalDate

// TODO: Multi-user support will require adding authentication (OAuth per user)
//  and scoping all queries by athlete ID to ensure data isolation.

/**
 * Represents the Strava athlete. Currently single-tenant (one athlete only).
 *
 * [rawData] stores the full Strava API JSON response as JSONB in Postgres,
 * preserving fields we don't model yet for future use.
 *
 * [birthDate] is usually null because Strava rarely provides it in the API response.
 */
data class Athlete(
    val id: String,
    val firstName: String,
    val lastName: String,
    val profileImageUrl: String?,
    val sex: String?,
    val birthDate: LocalDate?,
    val rawData: Map<String, Any?> = emptyMap(),
) {
    val fullName: String get() = "$firstName $lastName"

    companion object {
        fun fromStravaData(data: Map<String, Any?>): Athlete = Athlete(
            id = (data["id"] as Number).toString(),
            firstName = data["firstname"] as? String ?: "",
            lastName = data["lastname"] as? String ?: "",
            profileImageUrl = data["profile"] as? String,
            sex = data["sex"] as? String,
            birthDate = null,
            rawData = data,
        )
    }
}
