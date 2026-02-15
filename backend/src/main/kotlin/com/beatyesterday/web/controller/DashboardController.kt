package com.beatyesterday.web.controller

import com.beatyesterday.domain.activity.ActivityRepository
import com.beatyesterday.domain.athlete.AthleteRepository
import com.beatyesterday.web.dto.ActivitySummaryDto
import com.beatyesterday.web.dto.AthleteDto
import com.beatyesterday.web.dto.DashboardDto
import com.beatyesterday.web.dto.MonthlyStatDto
import com.beatyesterday.web.dto.SportBreakdownDto
import com.beatyesterday.web.dto.YearlyStatDto
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Month
import java.time.ZoneId

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val activityRepository: ActivityRepository,
    private val athleteRepository: AthleteRepository,
) {

    @GetMapping
    fun getDashboard(): DashboardDto {
        // Fetch all activities for aggregation (for MVP, load a reasonable amount)
        val recentPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "startDateTime"))
        val recentActivities = activityRepository.findAll(recentPageable).content

        // Load more for stats (up to 10000 for a personal dashboard)
        val allPageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "startDateTime"))
        val allActivities = activityRepository.findAll(allPageable).content

        val athlete = athleteRepository.find()

        // Monthly stats (current year)
        val monthlyStats = allActivities
            .groupBy {
                val zoned = it.startDateTime.atZone(ZoneId.systemDefault())
                Pair(zoned.year, zoned.monthValue)
            }
            .map { (yearMonth, activities) ->
                MonthlyStatDto(
                    year = yearMonth.first,
                    month = yearMonth.second,
                    monthName = Month.of(yearMonth.second).name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    activityCount = activities.size,
                    totalDistanceKm = activities.sumOf { it.distance.value },
                    totalElevationM = activities.sumOf { it.elevation.value },
                    totalMovingTimeHours = activities.sumOf { it.movingTimeInHours },
                )
            }
            .sortedWith(compareBy({ it.year }, { it.month }))

        // Yearly stats
        val yearlyStats = allActivities
            .groupBy { it.startDateTime.atZone(ZoneId.systemDefault()).year }
            .map { (year, activities) ->
                YearlyStatDto(
                    year = year,
                    activityCount = activities.size,
                    totalDistanceKm = activities.sumOf { it.distance.value },
                    totalElevationM = activities.sumOf { it.elevation.value },
                    totalMovingTimeHours = activities.sumOf { it.movingTimeInHours },
                )
            }
            .sortedBy { it.year }

        // Sport type breakdown
        val sportBreakdown = allActivities
            .groupBy { it.sportType }
            .map { (sportType, activities) ->
                SportBreakdownDto(
                    sportType = sportType.stravaValue,
                    displayName = sportType.displayName,
                    activityCount = activities.size,
                    totalDistanceKm = activities.sumOf { it.distance.value },
                    totalMovingTimeHours = activities.sumOf { it.movingTimeInHours },
                )
            }
            .sortedByDescending { it.activityCount }

        return DashboardDto(
            athlete = athlete?.let {
                AthleteDto(
                    id = it.id,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    fullName = it.fullName,
                    profileImageUrl = it.profileImageUrl,
                )
            },
            recentActivities = recentActivities.map { ActivitySummaryDto.from(it) },
            monthlyStats = monthlyStats,
            yearlyStats = yearlyStats,
            sportBreakdown = sportBreakdown,
            totalActivities = activityRepository.count(),
            totalDistanceKm = allActivities.sumOf { it.distance.value },
            totalElevationM = allActivities.sumOf { it.elevation.value },
            totalMovingTimeHours = allActivities.sumOf { it.movingTimeInHours },
        )
    }
}
