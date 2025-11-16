package dev.jwatral.secretsanta

class ShuffleAndCheckMatcherStrategyTest : MatcherStrategyTestBase() {
    override val matcher: MatcherStrategy = ShuffleAndCheckMatcherStrategy(1000)
}
