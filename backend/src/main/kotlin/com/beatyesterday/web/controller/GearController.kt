package com.beatyesterday.web.controller

import com.beatyesterday.domain.gear.GearRepository
import com.beatyesterday.web.dto.GearDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/gear")
class GearController(
    private val gearRepository: GearRepository,
) {

    @GetMapping
    fun getAllGear(): List<GearDto> =
        gearRepository.findAll().map { GearDto.from(it) }
}
