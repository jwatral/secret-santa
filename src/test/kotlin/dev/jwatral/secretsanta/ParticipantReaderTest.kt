package dev.jwatral.secretsanta

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["internal.participant-file-location=classpath:testParticipantsEmpty.json"])
class ParticipantReaderTest {

    @Autowired
    lateinit var participantReader: ParticipantReader

    @Test
    fun testParticipantReaderForDefaultFileLocation() {
        val participants = participantReader.parseParticipants()
        assertEquals(0, participants.size)
    }

    @Test
    fun testParticipantReaderForSpecifiedFileLocation() {
        val participants = participantReader.parseParticipants("classpath:testParticipants.json")
        assertEquals(2, participants.size)
    }
}
