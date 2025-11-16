package dev.jwatral.secretsanta

fun List<MatchedParticipant>.prettyPrint() = this.joinToString { it.giver.participant.name + " -> " + it.receiver.participant.name }

