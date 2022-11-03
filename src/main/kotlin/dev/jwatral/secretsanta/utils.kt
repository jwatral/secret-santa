package dev.jwatral.secretsanta

fun MatchedParticipants.prettyPrint() = this.joinToString { it.first.participant.name + " -> " + it.second.participant.name }

