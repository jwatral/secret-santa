package dev.jwatral.secretsanta

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("!test")
class MainService(
    private val participantReader: ParticipantReader,
    private val matcherService: MatcherService,
    private val emailService: EmailService,
    @Value("\${internal.default-email}") private val defaultEmail: String,
    @Value("\${internal.do-send}") private val doSend: Boolean,
    @Value("\${internal.use-test-email}") private val useTestEmail: Boolean,
    @Value("\${internal.matcher-strategy}") private val matcherStrategy: String,
    @Value("\${internal.smtp-test:false}") private val smtpTest: Boolean,
    @Value("\${internal.confirm-before-send:false}") private val confirmBeforeSend: Boolean,
) : CommandLineRunner {

    private val log = KotlinLogging.logger { }

    override fun run(vararg args: String) = runBlocking {
        log.info { "Started with args: ${args.asList()}" }
        log.info { "Year: ${emailService.year}, event name: '${emailService.eventName}'" }
        if (smtpTest) {
            log.info { "SMTP test mode - sending a single test email to $defaultEmail, no matching is done." }
            emailService.sendSmtpTest(defaultEmail)
            log.info { "SMTP test email sent successfully to $defaultEmail." }
            return@runBlocking
        }
        log.info { "Using '$matcherStrategy' matching strategy." }
        log.info { "doSend param is $doSend - app ${if (doSend) "will" else "won't"} send emails." }
        val participants = participantReader.parseParticipants()
        log.info { "Loaded following participants: $participants" }
        val matched = matcherService.matchParticipants(participants)
        log.info { "Matched participants: ${matched.prettyPrint()}" }
        validateMatches(participants, matched)
        if (doSend && confirmBeforeSend && !confirmSending(matched)) {
            log.warn { "SENDING ABORTED - no emails were sent." }
            return@runBlocking
        }
        matched.forEach { (giver, receiver) ->
            sendEmail(giver.participant, receiver.participant)
        }
    }

    private fun confirmSending(matched: List<MatchedParticipant>): Boolean {
        println()
        println("=== Pairs (${matched.size}) ===")
        matched.forEach { (giver, receiver) -> println("  ${giver.participant.name} -> ${receiver.participant.name}") }
        println()
        val target = if (useTestEmail) "to the test address $defaultEmail" else "to the REAL participants"
        println("Type 'send' to send ${matched.size} emails $target (anything else aborts):")
        System.out.flush()
        val answer = readlnOrNull()?.trim()
        log.info { "Confirmation answer: '$answer'" }
        return answer == "send"
    }

    private fun validateMatches(participants: Participants, matched: List<MatchedParticipant>) {
        val givers = matched.map { it.giver.participant }.toSet()
        val receivers = matched.map { it.receiver.participant }.toSet()
        val participantsSet = participants.flatten().toSet()

        if (givers != participantsSet || receivers != participantsSet) {
            log.error { "Validation failed: Not all participants give and receive exactly one gift." }
            log.error { "Participants: $participantsSet" }
            log.error { "Givers:       $givers" }
            log.error { "Receivers:    $receivers" }
            throw IllegalStateException("Matching validation failed.")
        }
        log.info { "Validation of matches was successful." }
    }

    private suspend fun sendEmail(giver: Participant, receiver: Participant) {
        log.info { "Notifying ${giver.name} about being secret santa for ${receiver.name}" }

        val giverForEmail = if (useTestEmail) {
            giver.copy(email = defaultEmail)
        } else {
            giver
        }

        emailService.send(
            participant = giverForEmail,
            receiverName = receiver.name,
            doSend = doSend
        )
    }
}
