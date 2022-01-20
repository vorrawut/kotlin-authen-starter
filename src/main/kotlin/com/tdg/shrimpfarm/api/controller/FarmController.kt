package com.tdg.shrimpfarm.api.controller

import com.tdg.shrimpfarm.api.response.FarmResponse
import com.tdg.shrimpfarm.api.service.FarmService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FarmController(val service: FarmService) {

    @GetMapping("/api/v1/farm")
    fun getFarm(): ResponseEntity<*> {
        val farm = FarmResponse(id = "1", name = "farm001")
        service.init()
        return ResponseEntity(farm, HttpStatus.OK)
    }
}