package com.ascot.intakehub.controller

import com.ascot.intakehub.service.DataIntakeService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/data")
@CrossOrigin
class DataController(private val dataIntakeService: DataIntakeService) {

    @PostMapping("/intake")
    fun triggerIntake(
        @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false) provider: String?
    ): Flux<String> {
        val targetDate = date ?: LocalDate.now()
        return dataIntakeService.intakeRacecards(targetDate, provider)
    }
}
