package com.tdg.shrimpfarm.api.service

import com.tdg.shrimpfarm.api.domain.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, Long> {
  fun findByUsername(username: String): Optional<User>
}
