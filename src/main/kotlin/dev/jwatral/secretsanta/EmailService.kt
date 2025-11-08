package dev.jwatral.secretsanta

import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class EmailService(
    val javaMailSender: JavaMailSender,
    @Value("\${internal.default-subject}") val defaultSubject: String,
    @Value("\${internal.default-from}") val defaultFrom: String,
) {

    private val log = KotlinLogging.logger { }

    suspend fun send(
        mail: String,
        text: String,
        doSend: Boolean = false,
        subject: String = defaultSubject,
        from: String = defaultFrom,
    ) {
        val msg = SimpleMailMessage().apply {
            setTo(mail)
            setText(text)
            setSubject(subject)
            setFrom(from)
        }
        log.info(msg.toString())
        if (doSend) {
            retry(limitAttempts(3) + constantDelay(10000)) {
                withTimeout(20.toDuration(DurationUnit.SECONDS)) {
                    javaMailSender.send(msg)
                }
            }
        }
    }
}
