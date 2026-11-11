package dev.jwatral.secretsanta

object EmailTemplates {

    /** e.g. "SecretSanta 2026 - Dorośli: Wylosowałeś/aś osobę!" (event name part is omitted when blank). */
    fun getSubject(language: Language, year: Int, eventName: String = ""): String {
        val prefix = "SecretSanta $year" + if (eventName.isNotBlank()) " - $eventName" else ""
        val text = when (language) {
            Language.PL -> "Wylosowałeś/aś osobę!"
            Language.EN -> "You have drawn a person!"
            Language.DE -> "Du hast eine Person gezogen!"
        }
        return "$prefix: $text"
    }

    /** [extraDetails] (e.g. date and place of the party) is added as a separate paragraph when not blank. */
    fun getBody(giverName: String, receiverName: String, language: Language, extraDetails: String = ""): String {
        val (greeting, drawn, wishes) = when (language) {
            Language.PL -> Triple("Cześć $giverName! 👋", "🎅 Twoja wylosowana osoba w Secret Santa to: $receiverName! 🎁", "Wesołych Świąt! 🎄✨")
            Language.EN -> Triple("Hi $giverName! 👋", "🎅 You have drawn $receiverName in our Secret Santa game! 🎁", "Merry Christmas! 🎄✨")
            Language.DE -> Triple("Hallo $giverName! 👋", "🎅 Du hast $receiverName in unserem Secret Santa-Spiel gezogen! 🎁", "Frohe Weihnachten! 🎄✨")
        }
        return listOf(greeting, drawn, extraDetails.trim(), wishes)
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
    }
}
