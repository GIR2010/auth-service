package ru.waiterix.auth.controller

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test-mail")
class TestMailController(private val mailSender: JavaMailSender) {

    @GetMapping
    fun sendTestMail(): String {
        val message = SimpleMailMessage()
        message.setTo("test@example.com")
        message.setSubject("Test Mail")
        message.setText("This is a test email sent from the Auth Service.")
        mailSender.send(message)
        return "Test email sent!"
    }
}