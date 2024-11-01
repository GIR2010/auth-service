package ru.waiterix.auth.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import ru.waiterix.auth.model.User
import ru.waiterix.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun findByUsername(username: String): Optional<User> {
        val user = userRepository.findByUsername(username)
        println(user.toString())
        return user
    }

    fun findByEmail(email: String): Optional<User> {
        return userRepository.findByEmail(email)
    }

    fun saveUser(user: User): User {
        user.password = passwordEncoder
            .encode(user.password)
        user.updatedAt = Instant.now()
        return userRepository.save(user)
    }

    fun createUser(username: String, password: String, email: String): User {
        val user = User(
            username = username,
            password = password,
            email = email,
            roles = mutableSetOf("ROLE_USER")
        )
        return saveUser(user)
    }

    fun enableUser(id: Long): User {
        val user = userRepository.findById(id).orElseThrow {
            NoSuchElementException("User not found with id: $id")
        }
        user.enableUser()
        user.verificationCode = null
        user.updatedAt = Instant.now()
        return userRepository.save(user)
    }

    fun findByVerificationCode(code: String): Optional<User> {
        return userRepository.findByVerificationCode(code)
    }
}