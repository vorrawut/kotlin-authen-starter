package com.tdg.shrimpfarm.api.controller

import com.tdg.shrimpfarm.api.domain.Role
import com.tdg.shrimpfarm.api.domain.User
import com.tdg.shrimpfarm.api.dtos.request.LoginRequest
import com.tdg.shrimpfarm.api.dtos.request.SignupRequest
import com.tdg.shrimpfarm.api.dtos.response.JwtResponse
import com.tdg.shrimpfarm.api.dtos.response.MessageResponse
import com.tdg.shrimpfarm.api.enums.ERole
import com.tdg.shrimpfarm.api.repositories.RoleRepository
import com.tdg.shrimpfarm.api.repositories.UserRepository
import com.tdg.shrimpfarm.api.security.JwtUtils
import com.tdg.shrimpfarm.api.service.UserDetailsImpl
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.validation.Valid


@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
class AuthController(
    var authenticationManager: AuthenticationManager,
    var userRepository: UserRepository,
    var roleRepository: RoleRepository,
    var jwtUtils: JwtUtils,
    @Qualifier("passwordEncoder") var encoder: PasswordEncoder
) {

    @PostMapping("/signin")
    fun authenticateUser(@RequestBody loginRequest: @Valid LoginRequest?): ResponseEntity<*> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest!!.username, loginRequest.password)
        )
        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtUtils.generateJwtToken(authentication)
        val userDetails = authentication.principal as UserDetailsImpl
        val roles = userDetails.authorities.stream()
            .map { item: GrantedAuthority -> item.authority }
            .collect(Collectors.toList())
        return ResponseEntity.ok(
            JwtResponse(
                jwt,
                userDetails.id,
                userDetails.username,
                userDetails.email,
                roles
            )
        )
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: @Valid SignupRequest?): ResponseEntity<*> {
        if (userRepository.existsByUsername(signUpRequest!!.username)!!) {
            return ResponseEntity
                .badRequest()
                .body(MessageResponse("Error: Username is already taken!"))
        }
        if (userRepository.existsByEmail(signUpRequest.email)!!) {
            return ResponseEntity
                .badRequest()
                .body(MessageResponse("Error: Email is already in use!"))
        }

        // Create new user's account
        val user = User(
            signUpRequest.username,
            signUpRequest.email,
            encoder.encode(signUpRequest.password)
        )
        val strRoles = signUpRequest.role
        val roles: MutableSet<Role> = HashSet()
        if (strRoles == null) {
            val userRole: Role = roleRepository.findByName(ERole.ROLE_USER)
                ?.orElseThrow(Supplier { RuntimeException("Error: Role is not found.") })!!
            roles.add(userRole)
        } else {
            strRoles.forEach(Consumer { role: String? ->
                when (role) {
                    "admin" -> {
                        val adminRole: Role = roleRepository.findByName(ERole.ROLE_ADMIN)
                            ?.orElseThrow(Supplier {
                                RuntimeException(
                                    "Error: Role is not found."
                                )
                            })!!
                        roles.add(adminRole)
                    }
                    "mod" -> {
                        val modRole: Role = roleRepository.findByName(ERole.ROLE_MODERATOR)
                            ?.orElseThrow(Supplier {
                                RuntimeException(
                                    "Error: Role is not found."
                                )
                            })!!
                        roles.add(modRole)
                    }
                    else -> {
                        val userRole: Role = roleRepository.findByName(ERole.ROLE_USER)
                            ?.orElseThrow(Supplier {
                                RuntimeException(
                                    "Error: Role is not found."
                                )
                            })!!
                        roles.add(userRole)
                    }
                }
            })
        }
        user.roles = roles
        userRepository.save(user)
        return ResponseEntity.ok(MessageResponse("User registered successfully!"))
    }
}