package com.project.onitama // Assuming this is your package

// --- Core Data Structures ---
data class Move(val deltaRow: Int, val deltaCol: Int)

data class HighlightedMoveWithCard(
    val row: Int,
    val col: Int,
    val isCapture: Boolean,
    val enablingCard: Card // List of cards that can enable this move
)


enum class CardStampColor { RED, BLUE } // Choose first player

data class Card(
    val name: String,
    val possibleMoves: List<Move>,
    val stampColor: CardStampColor
)

// --- Card Definitions ---

val TIGER_CARD = Card(
    name = "Tiger",
    possibleMoves = listOf(Move(-2, 0), Move(1, 0)),
    stampColor = CardStampColor.BLUE
)

val CRAB_CARD = Card(
    name = "Crab",
    possibleMoves = listOf(Move(0, -2), Move(0, 2), Move(-1, 0)),
    stampColor = CardStampColor.BLUE
)

val MONKEY_CARD = Card(
    name = "Monkey",
    possibleMoves = listOf(Move(-1, -1), Move(-1, 1), Move(1, -1), Move(1, 1)),
    stampColor = CardStampColor.BLUE
)

val CRANE_CARD = Card(
    name = "Crane",
    possibleMoves = listOf(Move(-1, 0), Move(1, -1), Move(1, 1)),
    stampColor = CardStampColor.BLUE
)

val DRAGON_CARD = Card(
    name = "Dragon",
    possibleMoves = listOf(Move(-1, -2), Move(-1, 2), Move(1, -1), Move(1, 1)),
    stampColor = CardStampColor.RED
)

val ELEPHANT_CARD = Card(
    name = "Elephant",
    possibleMoves = listOf(Move(-1, -1), Move(-1, 1), Move(0, -1), Move(0, 1)),
    stampColor = CardStampColor.RED
)

val MANTIS_CARD = Card(
    name = "Mantis",
    possibleMoves = listOf(Move(-1, -1), Move(-1, 1), Move(1, 0)),
    stampColor = CardStampColor.RED
)

val BOAR_CARD = Card(
    name = "Boar",
    possibleMoves = listOf(Move(-1, 0), Move(0, -1), Move(0, 1)),
    stampColor = CardStampColor.RED
)

val FROG_CARD = Card(
    name = "Frog",
    possibleMoves = listOf(Move(-1, -1), Move(0, -2), Move(1, 1)),
    stampColor = CardStampColor.RED
)

val GOOSE_CARD = Card(
    name = "Goose",
    possibleMoves = listOf(Move(-1, -1), Move(0, -1), Move(0, 1), Move(1, 1)),
    stampColor = CardStampColor.BLUE
)

val HORSE_CARD = Card(
    name = "Horse",
    possibleMoves = listOf(Move(-1, 0), Move(0, -1), Move(1, 0)),
    stampColor = CardStampColor.RED
)

val EEL_CARD = Card(
    name = "Eel",
    possibleMoves = listOf(Move(-1, -1), Move(0, 1), Move(1, -1)),
    stampColor = CardStampColor.BLUE
)

val COBRA_CARD = Card(
    name = "Cobra",
    possibleMoves = listOf(Move(0, -1), Move(-1, 1), Move(1, 1)),
    stampColor = CardStampColor.RED
)

val OX_CARD = Card(
    name = "Ox",
    possibleMoves = listOf(Move(-1, 0), Move(0, 1), Move(1, 0)),
    stampColor = CardStampColor.BLUE
)

val RABBIT_CARD = Card(
    name = "Rabbit",
    possibleMoves = listOf(Move(-1, 1), Move(0, 2), Move(1, -1)),
    stampColor = CardStampColor.BLUE
)

val ROOSTER_CARD = Card(
    name = "Rooster",
    possibleMoves = listOf(Move(-1, 1), Move(0, -1), Move(0, 1), Move(1, -1)),
    stampColor = CardStampColor.RED
)

// --- Collection of All Game Cards ---
val ALL_GAME_CARDS: List<Card> = listOf(
    TIGER_CARD,
    CRAB_CARD,
    MONKEY_CARD,
    CRANE_CARD,
    DRAGON_CARD,
    ELEPHANT_CARD,
    MANTIS_CARD,
    BOAR_CARD,
    FROG_CARD,
    GOOSE_CARD,
    HORSE_CARD,
    EEL_CARD,
    COBRA_CARD,
    OX_CARD,
    RABBIT_CARD,
    ROOSTER_CARD
).distinctBy { it.name }

fun getAdjustedMoves(card: Card, player: Int, RED_PLAYER: Int): List<Move> {
    return if (player == RED_PLAYER) {
        card.possibleMoves
    } else { // BLUE_PLAYER
        card.possibleMoves.map { move ->
            Move(-move.deltaRow, -move.deltaCol)
        }
    }
}
