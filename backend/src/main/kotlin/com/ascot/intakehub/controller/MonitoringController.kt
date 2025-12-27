package com.ascot.intakehub.controller

import com.ascot.intakehub.service.MonitoringService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/health")
@CrossOrigin
class MonitoringController(private val monitoringService: MonitoringService) {

    @GetMapping
    fun getHealth(): Map<String, Boolean> {
        return monitoringService.getAllHealth()
    }
}
