package com.tdg.shrimpfarm.api.controller;

import com.tdg.shrimpfarm.api.config.SecurityProperties
import com.tdg.shrimpfarm.api.domain.User
import com.tdg.shrimpfarm.api.service.UserService
import com.tdg.shrimpfarm.api.utils.add
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class UsersController(private val userService: UserService) {

    @RequestMapping(value = ["/api/v1/login"], method = [RequestMethod.POST])
    fun login(@RequestBody user: User): ResponseEntity<*> {
        val token = userService.doLogin(user)
        return ResponseEntity(token, HttpStatus.OK)
    }
}