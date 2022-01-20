package controller

import models.response.FarmResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FarmController {

    @GetMapping("/api/v1/farm")
    fun getFarm(): ResponseEntity<*> {
        val farm = FarmResponse(id = "1", name = "farm001")
        return ResponseEntity(farm, HttpStatus.OK)
    }
}