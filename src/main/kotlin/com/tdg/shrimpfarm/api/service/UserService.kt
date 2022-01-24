package com.tdg.shrimpfarm.api.service

import com.tdg.shrimpfarm.api.config.SecurityProperties
import com.tdg.shrimpfarm.api.domain.User
import com.tdg.shrimpfarm.api.utils.add
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(private val securityProperties: SecurityProperties) {

    fun doLogin(user: User): String {
        val authClaims: MutableList<String> = mutableListOf()
        return Jwts.builder()
            .setSubject((user).username)
            .claim("auth", authClaims)
            .setExpiration(Date().add(Calendar.DAY_OF_MONTH, 2))
            .signWith(Keys.hmacShaKeyFor(securityProperties.secret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()
    }
}