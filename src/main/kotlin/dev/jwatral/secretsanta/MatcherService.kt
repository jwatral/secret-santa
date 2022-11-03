package dev.jwatral.secretsanta

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class MatcherService(
    @Value("\${internal.max-shuffles}") private val maxShuffles: Int
) {

    fun matchParticipants(participants: Participants): MatchedParticipants {
        val participantsWithGroups = participants.flatMapIndexed { index: Int, list: List<Participant> -> list.map { ParticipantWithGroup(it, index) } }
        var counter = 0
        while (counter < maxShuffles) {
            val pairs = participantsWithGroups.shuffled().zipWithNext().let { it + (it.last().second to it.first().first) }
            if (pairs.none { it.first.group == it.second.group }) {
                return pairs
            }
            counter++
        }
        throw RuntimeException("Did not found solution after $maxShuffles shuffles.")
    }
}
