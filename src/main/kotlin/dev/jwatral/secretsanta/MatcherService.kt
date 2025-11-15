package dev.jwatral.secretsanta

import org.springframework.stereotype.Service

@Service
class MatcherService(
    private val matcherStrategy: MatcherStrategy
) {

    fun matchParticipants(participants: Participants): MatchedParticipants {
        return matcherStrategy.match(participants)
    }
}

class MatcherException(message: String) : RuntimeException(message)
