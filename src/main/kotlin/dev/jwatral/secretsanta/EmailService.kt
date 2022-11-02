package dev.jwatral.secretsanta

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

    fun send(
        mail: String,
        text: String,
        subject: String = defaultSubject,
        from: String = defaultFrom,
    ) {
        val msg = SimpleMailMessage().apply {
            setTo(mail)
            setText(text)
            setSubject(subject)
            setFrom(from)
        }
        javaMailSender.send(msg)
    }
}
