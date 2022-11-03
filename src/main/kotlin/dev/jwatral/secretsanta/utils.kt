package dev.jwatral.secretsanta


fun <E> List<List<E>>.transpose(): List<List<E>> {
    // Helpers
    fun <E> List<E>.head(): E = this.first()
    fun <E> List<E>.tail(): List<E> = this.takeLast(this.size - 1)
    fun <E> E.append(xs: List<E>): List<E> = listOf(this).plus(xs)

    filter { it.isNotEmpty() }.let { ys ->
        return when (ys.isNotEmpty()) {
            true -> ys.map { it.head() }.append(ys.map { it.tail() }.transpose())
            else -> emptyList()
        }
    }
}

fun MatchedParticipants.prettyPrint() = this.joinToString { it.first.participant.name + " -> " + it.second.participant.name }

