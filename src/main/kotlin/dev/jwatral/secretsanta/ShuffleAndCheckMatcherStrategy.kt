package dev.jwatral.secretsanta

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!test")
@ConditionalOnProperty(name = ["internal.matcher-strategy"], havingValue = "shuffle")
class ShuffleAndCheckMatcherStrategy(
    @Value("\${internal.max-shuffles}") private val maxShuffles: Int
) : MatcherStrategy {

    private val log = KotlinLogging.logger { }

    override fun match(participants: Participants): MatchedParticipants {
        val participantsWithGroups =
            participants.flatMapIndexed { index, list -> list.map { ParticipantWithGroup(it, index) } }

        repeat(maxShuffles) { i ->
            val givers = participantsWithGroups
            val receivers = givers.shuffled()
            val pairs = givers.zip(receivers)
            val pairsSet = pairs.toSet()

            if (pairs.none { (giver, receiver) ->
                    giver.group == receiver.group ||
                    giver.participant == receiver.participant ||
                    pairsSet.contains(receiver to giver)
                }) {
                log.info { "Matched participants after [${i + 1}] shuffles using shuffle-and-check strategy." }
                return pairs
            }
        }

        throw MatcherException("Did not find a solution after $maxShuffles shuffles.")
    }
}
