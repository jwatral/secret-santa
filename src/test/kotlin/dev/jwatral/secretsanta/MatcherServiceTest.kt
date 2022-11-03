package dev.jwatral.secretsanta

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MatcherServiceTest {

    @Test
    fun `four participants in two groups should not be matched within same group`() {
        val service = MatcherService(100)

        val participant1 = participant("1_1")
        val participant2 = participant("2_1")
        val participants = listOf(
            listOf(participant1, participant("1_2")),
            listOf(participant2, participant("2_2"))
        )

        val matchParticipants = service.matchParticipants(participants)

        assertEquals(4, matchParticipants.size)
        assertEquals(0, matchParticipants.find { it.first.participant == participant1 }?.first?.group)
        assertEquals(1, matchParticipants.find { it.first.participant == participant2 }?.first?.group)
        matchParticipants.forEach {
            assertNotEquals(it.first.group, it.second.group)
        }
    }
}
