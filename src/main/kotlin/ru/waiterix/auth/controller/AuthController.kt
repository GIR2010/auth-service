package ru.waiterix.auth.controller

import ru.waiterix.auth.security.JwtTokenProvider
import ru.waiterix.auth.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import java.util.*

@RestController
@RequestMapping
@Validated
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
//    private val emailService: EmailService // Предполагается, что есть EmailService
) {

    @GetMapping("/hello")
    fun hello(): String {
        return "Hello"
    }
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        try {
            val authentication: Authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )
            val user = authentication.principal as UserDetails
            val roles = user.authorities.map { it.authority }

            val accessToken = jwtTokenProvider.createAccessToken(user.username, roles)
            val refreshToken = jwtTokenProvider.createRefreshToken(user.username)

            return ResponseEntity.ok(AuthResponse(accessToken, refreshToken))

        } catch (e: Exception) {
            println("ERROR MESSAGE: ${e.message},\nSTACK TRACE:\n${e.stackTraceToString()}")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<String> {
        // Проверка, существует ли пользователь с таким же именем или email
        if (userService.findByUsername(registerRequest.username).isPresent) {
            return ResponseEntity.badRequest().body("Username is already taken.")
        }

        if (userService.findByEmail(registerRequest.email).isPresent) {
            return ResponseEntity.badRequest().body("Email is already in use.")
        }

        // Создание нового пользователя
        val verificationCode = UUID.randomUUID().toString()
        val user = userService.createUser(registerRequest.username, registerRequest.password, registerRequest.email)
        user.verificationCode = verificationCode
        userService.saveUser(user)

        // Отправка письма подтверждения
//        emailService.sendVerificationEmail(user.email, verificationCode)

        return ResponseEntity.ok("User registered successfully. Please check your email to verify your account.")
    }

    @GetMapping("/verify")
    fun verifyAccount(@RequestParam("code") code: String): ResponseEntity<String> {
        val userOpt = userService.findByVerificationCode(code)
        return if (userOpt.isPresent) {
            val user = userOpt.get()
            userService.enableUser(user.id)
            ResponseEntity.ok("Account verified successfully.")
        } else {
            ResponseEntity.badRequest().body("Invalid verification code.")
        }
    }

    // Добавьте эндпоинты для сброса пароля и другие по необходимости
}