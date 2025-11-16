package dev.jwatral.secretsanta

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

abstract class MatcherStrategyTestBase {

    protected abstract val matcher: MatcherStrategy

    @Test
    fun `should match participants successfully`() {
        val participants = listOf(
            listOf(Participant("A", "a@a.com")),
            listOf(Participant("B", "b@b.com")),
            listOf(Participant("C", "c@c.com"))
        )
        val result = matcher.match(participants)
        assertEquals(3, result.size)
        val givers = result.map { it.giver.participant }
        val receivers = result.map { it.receiver.participant }
        assertTrue(givers.containsAll(participants.flatten()))
        assertTrue(receivers.containsAll(participants.flatten()))
    }

    @Test
    fun `should throw MatcherException when no assignment is possible`() {
        val participants = listOf(
            listOf(Participant("A", "a@a.com"), Participant("B", "b@b.com")),
            listOf(Participant("C", "c@c.com"))
        )
        assertThrows(MatcherException::class.java) {
            matcher.match(participants)
        }
    }

    @Test
    fun `should not match participant with themselves`() {
        val participants = listOf(
            listOf(Participant("A", "a@a.com")),
            listOf(Participant("B", "b@b.com")),
            listOf(Participant("C", "c@c.com"))
        )
        val result = matcher.match(participants)
        result.forEach { (giver, receiver) ->
            assertNotEquals(giver.participant, receiver.participant)
        }
    }

    @Test
    fun `should not match participants from the same group`() {
        val participants = listOf(
            listOf(Participant("A", "a@a.com"), Participant("B", "b@b.com")),
            listOf(Participant("C", "c@c.com"), Participant("D", "d@d.com")),
            listOf(Participant("E", "e@e.com"), Participant("F", "f@f.com"))
        )
        val result = matcher.match(participants)
        result.forEach { (giver, receiver) ->
            assertNotEquals(giver.group, receiver.group)
        }
    }

    @Test
    fun `should handle a more complex matching scenario`() {
        val participants = listOf(
            listOf(Participant("A", "a@a.com"), Participant("B", "b@b.com")),
            listOf(Participant("C", "c@c.com")),
            listOf(Participant("D", "d@d.com"), Participant("E", "e@e.com"), Participant("F", "f@f.com"))
        )
        val result = matcher.match(participants)
        assertEquals(6, result.size)
        val givers = result.map { it.giver.participant }
        val receivers = result.map { it.receiver.participant }
        assertTrue(givers.containsAll(participants.flatten()))
        assertTrue(receivers.containsAll(participants.flatten()))
    }
}
