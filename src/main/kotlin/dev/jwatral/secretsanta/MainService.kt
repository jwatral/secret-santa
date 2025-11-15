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
    @Value("\${internal.text-template}") private val textTemplate: String,
    @Value("\${internal.default-email}") private val defaultEmail: String,
    @Value("\${internal.do-send}") private val doSend: Boolean,
    @Value("\${internal.use-test-email}") private val useTestEmail: Boolean,
    @Value("\${internal.matcher-strategy}") private val matcherStrategy: String,
) : CommandLineRunner {

    private val log = KotlinLogging.logger { }

    override fun run(vararg args: String) = runBlocking {
        log.info { "Started with args: ${args.asList()}" }
        log.info { "Using '$matcherStrategy' matching strategy." }
        log.info { "doSend param is $doSend - app ${if (doSend) "will" else "won't"} send emails." }
        val participants = participantReader.parseParticipants()
        log.info { "Loaded following participants: $participants" }
        val matched = matcherService.matchParticipants(participants)
        log.info { "Matched participants: ${matched.prettyPrint()}" }
        validateMatches(participants, matched)
        matched
            .groupBy { it.first.participant.email }
            .forEach { (email, matchedParticipants) ->
                sendGroupedEmail(email, matchedParticipants)
            }
    }

    private fun validateMatches(participants: Participants, matched: MatchedParticipants) {
        val givers = matched.map { it.first.participant }.toSet()
        val receivers = matched.map { it.second.participant }.toSet()
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

    private suspend fun sendGroupedEmail(email: String, participants: MatchedParticipants) {
        val text = participants.joinToString("\n") { (who, whom) ->
            log.info { "Notifying ${who.participant.name} about being secret santa for ${whom.participant.name}" }
            textTemplate.replace("{who}", who.participant.name).replace("{giftFor}", whom.participant.name)
        }
        emailService.send(
            mail = if (useTestEmail) defaultEmail else email,
            text = text,
            doSend = doSend
        )
    }
}
