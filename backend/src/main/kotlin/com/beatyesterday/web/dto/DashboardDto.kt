package com.beatyesterday.web.dto

/**
 * DTOs (Data Transfer Objects) for the dashboard endpoint. These define the exact JSON shape
 * sent to the frontend — keeping DTOs separate from domain models lets us control the API
 * contract independently.
 */

data class DashboardDto(
    val athlete: AthleteDto?,
    val recentActivities: List<ActivitySummaryDto>,
    val monthlyStats: List<MonthlyStatDto>,
    val yearlyStats: List<YearlyStatDto>,
    val sportBreakdown: List<SportBreakdownDto>,
    val totalActivities: Long,
    val totalDistanceKm: Double,
    val totalElevationM: Double,
    val totalMovingTimeHours: Double,
)

data class AthleteDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val profileImageUrl: String?,
)

data class MonthlyStatDto(
    val year: Int,
    val month: Int,
    val monthName: String,
    val activityCount: Int,
    val totalDistanceKm: Double,
    val totalElevationM: Double,
    val totalMovingTimeHours: Double,
)

data class YearlyStatDto(
    val year: Int,
    val activityCount: Int,
    val totalDistanceKm: Double,
    val totalElevationM: Double,
    val totalMovingTimeHours: Double,
)

data class SportBreakdownDto(
    val sportType: String,
    val displayName: String,
    val activityCount: Int,
    val totalDistanceKm: Double,
    val totalMovingTimeHours: Double,
)
