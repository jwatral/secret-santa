package dev.jwatral.secretsanta

import org.springframework.stereotype.Service

@Service
class MatcherService {

    fun matchParticipants(participants: Participants): MatchedParticipants =
        participants
            .mapIndexed{ index, list -> list.map { ParticipantWithGroup(it, index) } }
            .shuffled()
            .map { it.shuffled() }
            .transpose()
            .flatten()
            .zipWithNext()
            .let { it + (it.last().second to it.first().first) }
}
