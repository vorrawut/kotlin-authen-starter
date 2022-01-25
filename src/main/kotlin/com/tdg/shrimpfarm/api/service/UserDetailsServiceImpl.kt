package com.tdg.shrimpfarm.api.service

import com.tdg.shrimpfarm.api.domain.User
import com.tdg.shrimpfarm.api.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class UserDetailsServiceImpl(var userRepository: UserRepository) : UserDetailsService {

    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: User = userRepository.findByUsername(username)
            ?.orElseThrow { UsernameNotFoundException("User Not Found with username: $username") }!!
        return UserDetailsImpl.build(user)
    }
}