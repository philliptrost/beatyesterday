package com.beatyesterday.web.controller

import com.beatyesterday.domain.activity.ActivityId
import com.beatyesterday.domain.activity.ActivityRepository
import com.beatyesterday.domain.activity.SportType
import com.beatyesterday.web.dto.ActivityDetailDto
import com.beatyesterday.web.dto.ActivitySummaryDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/activities")
class ActivityController(
    private val activityRepository: ActivityRepository,
) {

    @GetMapping
    fun getActivities(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(defaultValue = "startDateTime") sort: String,
        @RequestParam(defaultValue = "desc") direction: String,
        @RequestParam(required = false) sportType: String?,
    ): Page<ActivitySummaryDto> {
        val sortDirection = if (direction == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort))

        val activities = if (sportType != null) {
            val type = SportType.fromStravaValue(sportType)
            activityRepository.findAllBySportType(type, pageable)
        } else {
            activityRepository.findAll(pageable)
        }

        return activities.map { ActivitySummaryDto.from(it) }
    }

    @GetMapping("/{id}")
    fun getActivity(@PathVariable id: String): ResponseEntity<ActivityDetailDto> {
        val activity = activityRepository.findById(ActivityId(id))
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ActivityDetailDto.from(activity))
    }
}
