package com.tdg.shrimpfarm.api.repositories

import com.tdg.shrimpfarm.api.domain.ERole
import com.tdg.shrimpfarm.api.domain.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface RoleRepository : JpaRepository<Role?, Long?> {
    fun findByName(name: ERole?): Optional<Role?>?
}