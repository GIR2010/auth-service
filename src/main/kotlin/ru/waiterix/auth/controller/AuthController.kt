package ru.waiterix.auth.controller

import ru.waiterix.auth.security.JwtTokenProvider
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/login")
@Validated
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        )

        val user = authentication.principal as UserDetails
        val roles = user.authorities.map { it.authority }

        val accessToken = jwtTokenProvider.createAccessToken(user.username, roles)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.username)

        return ResponseEntity.ok(AuthResponse(accessToken, refreshToken))
    }
}