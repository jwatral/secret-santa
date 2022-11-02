package dev.jwatral.secretsanta

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("secret")
class EmailServiceTest {

    @Autowired
    lateinit var emailService: EmailService
    @Value("\${internal.default-email}")
    lateinit var defaultEmail: String

    @Test
    internal fun sendAnEmail() {
        emailService.send(defaultEmail, "Test text", "Test subject")
    }
}
