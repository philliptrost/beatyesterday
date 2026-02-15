package com.beatyesterday.domain.activity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ActivityRepository {
    fun save(activity: Activity)
    fun findById(id: ActivityId): Activity?
    fun findAll(pageable: Pageable): Page<Activity>
    fun findAllBySportType(sportType: SportType, pageable: Pageable): Page<Activity>
    fun exists(id: ActivityId): Boolean
    fun count(): Long
    fun deleteById(id: ActivityId)
}
