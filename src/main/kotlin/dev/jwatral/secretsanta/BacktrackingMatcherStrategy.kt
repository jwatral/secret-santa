package dev.jwatral.secretsanta

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["internal.matcher-strategy"], havingValue = "backtracking")
class BacktrackingMatcherStrategy : MatcherStrategy {

    private val log = KotlinLogging.logger { }

    override fun match(participants: Participants): List<MatchedParticipant> {
        val participantsWithGroups =
            participants.flatMapIndexed { index, list -> list.map { ParticipantWithGroup(it, index) } }

        val givers = participantsWithGroups.shuffled()
        val receivers = participantsWithGroups.toMutableList()
        val assignments = mutableListOf<MatchedParticipant>()

        if (findAssignments(givers, receivers, assignments)) {
            log.info { "Successfully matched all participants using backtracking strategy." }
            return assignments
        }

        throw MatcherException("Could not find a valid secret santa assignment for the given participants and constraints.")
    }

    private fun findAssignments(
        givers: List<ParticipantWithGroup>,
        availableReceivers: MutableList<ParticipantWithGroup>,
        assignments: MutableList<MatchedParticipant>
    ): Boolean {
        if (givers.isEmpty()) return true

        val giver = givers.first()

        for (receiver in availableReceivers.shuffled()) {
            if (giver.participant != receiver.participant && giver.group != receiver.group) {
                assignments.add(MatchedParticipant(giver, receiver))
                availableReceivers.remove(receiver)

                if (findAssignments(givers.drop(1), availableReceivers, assignments)) return true

                availableReceivers.add(receiver)
                assignments.removeLast()
            }
        }
        return false
    }
}