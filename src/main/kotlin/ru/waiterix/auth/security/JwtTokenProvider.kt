package ru.waiterix.auth.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import jakarta.annotation.PostConstruct
import javax.crypto.SecretKey
import java.util.Base64

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKeyBase64: String,
    @Value("\${jwt.access-token-expiration}") private val validityInMilliseconds: Long,
    @Value("\${jwt.refresh-token-expiration}") private val refreshValidityInMilliseconds: Long
) {

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        // Декодируем base64-строку в байты и создаём SecretKey
        val decodedKey = Base64.getDecoder().decode(secretKeyBase64)
        secretKey = Keys.hmacShaKeyFor(decodedKey)
    }

    fun createAccessToken(username: String, roles: List<String>): String {
        val claims: Claims = Jwts.claims().setSubject(username)
        claims["roles"] = roles

        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun createRefreshToken(username: String): String {
        val claims: Claims = Jwts.claims().setSubject(username)

        val now = Date()
        val validity = Date(now.time + refreshValidityInMilliseconds)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUsername(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun getRoles(token: String): List<String> {
        val claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
        @Suppress("UNCHECKED_CAST")
        return claims["roles"] as List<String>
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}