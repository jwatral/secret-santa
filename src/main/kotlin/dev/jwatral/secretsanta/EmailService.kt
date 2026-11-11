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
    @Value("\${internal.default-from}") val defaultFrom: String,
    @Value("\${internal.year:#{T(java.time.Year).now().getValue()}}") val year: Int,
    @Value("\${internal.event-name:}") val eventName: String,
    @Value("\${internal.extra-details:}") val extraDetails: String,
) {

    private val log = KotlinLogging.logger { }

    suspend fun send(
        participant: Participant,
        receiverName: String,
        doSend: Boolean = false,
        from: String = defaultFrom,
    ) {
        val language = participant.language
        val subject = EmailTemplates.getSubject(language, year, eventName)
        val text = EmailTemplates.getBody(participant.name, receiverName, language, extraDetails)

        deliver(to = participant.email, subject = subject, text = text, from = from, doSend = doSend)
    }

    /**
     * Sends a single email to verify the SMTP configuration. It uses the real template with sample names,
     * so it also checks deliverability (spam filtering) of the actual email and works as a preview.
     */
    suspend fun sendSmtpTest(to: String, from: String = defaultFrom) {
        deliver(
            to = to,
            subject = EmailTemplates.getSubject(Language.PL, year, eventName),
            text = EmailTemplates.getBody("Mikołaj", "Śnieżynka", Language.PL, extraDetails),
            from = from,
            doSend = true,
        )
    }

    private suspend fun deliver(to: String, subject: String, text: String, from: String, doSend: Boolean) {
        val msg = SimpleMailMessage().apply {
            setTo(to)
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
