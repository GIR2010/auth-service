package ru.waiterix.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf().disable() // Отключаем CSRF для упрощения (не рекомендуется для продакшн)
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/actuator/**").permitAll() // Разрешаем доступ к эндпоинтам Actuator
                    .requestMatchers("/test-mail").permitAll() // Разрешаем доступ к /test-mail
                    .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
            }
            .formLogin(Customizer.withDefaults()) // Включаем стандартную форму логина
            .httpBasic(Customizer.withDefaults()) // Включаем HTTP Basic аутентификацию
        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user: UserDetails = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(user)
    }
}