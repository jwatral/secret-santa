package dev.jwatral.secretsanta

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import java.util.*

@Service
class MainService(
    private val participantReader: ParticipantReader,
    private val matcherService: MatcherService,
    private val emailService: EmailService,
    @Value("\${internal.text-template}") private val textTemplate: String,
    @Value("\${internal.default-email}") private val defaultEmail: String,
) : CommandLineRunner {

    private val log = KotlinLogging.logger { }

    override fun run(vararg args: String) {
        log.info { "Started with args: ${args.asList()}" }
        val doSend = args.contains("doSend")
        log.info { "doSend param is $doSend - app ${if (doSend) "will" else "won't"} send emails." }
        val testEmail = args.contains("testEmail")
        val participants = participantReader.parseParticipants()
        log.info { "Loaded following participants: $participants" }
        val matched = matcherService.matchParticipants(participants)
        log.info { "Matched participants: ${matched.prettyPrint()}" }
        val matchedFromTheSameGroup = matched.filter { it.first.group == it.second.group }
        if (matchedFromTheSameGroup.isNotEmpty()) {
            println("Following matched participants are from the same groups, continue anyway (type 'yes')?")
            println(matchedFromTheSameGroup.prettyPrint())
            val input = readln()
            if (input != "yes") {
                return
            }
        }
        matched.forEach { (who, whom) ->
            log.info { "Notifying ${who.participant.name} about being secret santa for ${whom.participant.name}" }
            sendEmail(who.participant, whom.participant, doSend, testEmail)
        }
    }

    private fun sendEmail(who: Participant, whom: Participant, doSend: Boolean, testEmail: Boolean) {
        emailService.send(
            mail = if (testEmail) defaultEmail else who.email,
            text = textTemplate.replace("{who}", who.name).replace("{giftFor}", whom.name),
            doSend = doSend
        )
    }
}
