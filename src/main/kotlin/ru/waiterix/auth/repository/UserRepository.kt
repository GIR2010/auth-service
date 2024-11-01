package ru.waiterix.auth.repository

import ru.waiterix.auth.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun findByVerificationCode(code: String): Optional<User>
    fun findByResetPasswordCode(code: String): Optional<User>
}