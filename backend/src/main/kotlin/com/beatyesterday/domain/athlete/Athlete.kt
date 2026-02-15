package com.beatyesterday.domain.athlete

import java.time.LocalDate

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
