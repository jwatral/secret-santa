package dev.jwatral.secretsanta

data class Participant(val name: String, val email: String, val language: Language = Language.PL)

enum class Language {
    PL, EN, DE
}

data class ParticipantWithGroup(val participant: Participant, val group: Int)

typealias Participants = List<List<Participant>>

data class MatchedParticipant(val giver: ParticipantWithGroup, val receiver: ParticipantWithGroup)
