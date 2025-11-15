package dev.jwatral.secretsanta

interface MatcherStrategy {
    fun match(participants: Participants): MatchedParticipants
}