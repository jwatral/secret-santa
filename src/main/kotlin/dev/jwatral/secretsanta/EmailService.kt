package dev.jwatral.secretsanta

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    val javaMailSender: JavaMailSender,
    @Value("\${internal.default-subject}") val defaultSubject: String,
    @Value("\${internal.default-from}") val defaultFrom: String,
) {

    private val log = KotlinLogging.logger { }

    fun send(
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
            javaMailSender.send(msg)
        }
    }
}
