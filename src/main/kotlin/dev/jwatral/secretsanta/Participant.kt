package dev.jwatral.secretsanta

data class Participant(val name: String, val email: String)

typealias Participants = List<List<Participant>>
