package dev.jwatral.secretsanta

data class Participant(val name: String, val email: String)

data class ParticipantWithGroup(val participant: Participant, val group: Int)

typealias Participants = List<List<Participant>>

typealias MatchedParticipants = List<Pair<ParticipantWithGroup, ParticipantWithGroup>>
