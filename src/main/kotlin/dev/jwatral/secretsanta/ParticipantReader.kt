package dev.jwatral.secretsanta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class ParticipantReader(
    val objectMapper: ObjectMapper,
    val resourceLoader: ResourceLoader,
    @Value("\${internal.participant-file-location}") val defaultParticipantFileLocation: String,
) {

    fun parseParticipants(participantFileLocation: String = defaultParticipantFileLocation): Participants =
        objectMapper.readValue(resourceLoader.getResource(participantFileLocation).file)

}
