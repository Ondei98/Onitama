package com.project.onitama

import android.util.Log
import kotlin.math.max

data class AIResult(val move: AIMove?, val score: Int)data class AIMove(val pieceRow: Int, val pieceCol: Int, val targetRow: Int, val targetCol: Int, val card: Card)

// --- FIX 1: The Engine now learns what the player values are ---
class OnitamaAIEngine(
    private val redPlayer: Int,
    private val bluePlayer: Int,
    private val redKing: Int,
    private val blueKing: Int,
    private val redPiece: Int,
    private val bluePiece: Int
) {
    private val winScore: Int = 10000
    private val kingScore: Int = 900
    private val pawnScore: Int = 100
    private val pieceAdvantageBonus: Int = 50
    private val centerControlScore: Int = 5
    private val pawnAdvantageScore: Int = 2

    fun findBestMove(
        board: Array<IntArray>, aiPlayer: Int, opponentPlayer: Int,
        aiCards: List<Card>, opponentCards: List<Card>, neutralCard: Card, depth: Int
    ): AIResult {
        return negamax(
            board = board, aiPlayer = aiPlayer, opponentPlayer = opponentPlayer,
            aiCards = aiCards, opponentCards = opponentCards, neutralCard = neutralCard,
            depth = depth, alpha = -Int.MAX_VALUE, beta = Int.MAX_VALUE, playerToMove = aiPlayer
        )
    }

    private fun evaluateBoard(board: Array<IntArray>, playerToScore: Int, opponent: Int): Int {
        val playerMasterPiece = if (playerToScore == redPlayer) redKing else blueKing
        val opponentMasterPiece = if (opponent == redPlayer) redKing else blueKing

        // 1. Check for immediate win/loss by Temple Arch (Stream)
        val opponentTempleRow = if (playerToScore == redPlayer) 0 else 4
        if (board[opponentTempleRow][2] == playerMasterPiece) return winScore
        val playerTempleRow = if (playerToScore == redPlayer) 4 else 0
        if (board[playerTempleRow][2] == opponentMasterPiece) return -winScore

        var playerMaterial = 0
        var opponentMaterial = 0
        var playerPieceCount = 0
        var opponentPieceCount = 0
        var playerPositionalScore = 0
        var isPlayerMasterAlive = false
        var isOpponentMasterAlive = false

        for (r in board.indices) {
            for (c in board[r].indices) {
                val piece = board[r][c]
                if (piece == 0) continue

                val isMyPiece = (playerToScore == redPlayer && (piece == redPiece || piece == redKing)) ||
                        (playerToScore == bluePlayer && (piece == bluePiece || piece == blueKing))

                if (isMyPiece) {
                    playerPieceCount++
                    if (piece == playerMasterPiece) {
                        isPlayerMasterAlive = true
                        playerMaterial += kingScore
                    } else {
                        playerMaterial += pawnScore
                    }
                    // Add positional bonuses
                    if (r in 1..3 && c in 1..3) playerPositionalScore += centerControlScore
                    val progress = if (playerToScore == redPlayer) 4 - r else r
                    playerPositionalScore += progress * pawnAdvantageScore
                } else {
                    opponentPieceCount++
                    if (piece == opponentMasterPiece) {
                        isOpponentMasterAlive = true
                        opponentMaterial += kingScore
                    } else {
                        opponentMaterial += pawnScore
                    }
                }
            }
        }

        // 2. Check for win/loss by capturing the King (Stone)
        if (!isOpponentMasterAlive) return winScore
        if (!isPlayerMasterAlive) return -winScore

        val materialAdvantage = playerMaterial - opponentMaterial
        val pieceCountAdvantage = (playerPieceCount - opponentPieceCount) * pieceAdvantageBonus // Each piece advantage is worth a pawn

        return materialAdvantage + pieceCountAdvantage + playerPositionalScore
    }


    private fun negamax(
        board: Array<IntArray>, aiPlayer: Int, opponentPlayer: Int,
        aiCards: List<Card>, opponentCards: List<Card>, neutralCard: Card,
        depth: Int, alpha: Int, beta: Int, playerToMove: Int
    ): AIResult {
        val opponent = if (playerToMove == aiPlayer) opponentPlayer else aiPlayer

        val boardValue = evaluateBoard(board, playerToMove, opponent)

        if (depth == 0 || boardValue >= winScore || boardValue <= -winScore) {
            return AIResult(null, boardValue)
        }

        var bestMove: AIMove? = null
        var maxScore = -Int.MAX_VALUE
        var currentAlpha = alpha

        val (currentCards, otherCards) = if (playerToMove == aiPlayer) Pair(aiCards, opponentCards) else Pair(opponentCards, aiCards)
        val allPossibleMoves = generateAllMovesForPlayer(board, playerToMove, currentCards)

        for (move in allPossibleMoves) {
            val newBoard = board.map { it.clone() }.toTypedArray()
            newBoard[move.targetRow][move.targetCol] = newBoard[move.pieceRow][move.pieceCol]
            newBoard[move.pieceRow][move.pieceCol] = 0

            val immediateEval = evaluateBoard(newBoard, playerToMove, opponent)
            if (immediateEval >= winScore) {
                val winningScore = (winScore - (10 - depth))
                if (winningScore > maxScore) {
                    maxScore = winningScore
                    bestMove = move
                }
                currentAlpha = max(currentAlpha, maxScore)
                if (currentAlpha >= beta) break
                continue
            }

            val nextPlayerCards = currentCards.minus(move.card).plus(neutralCard)
            val nextNeutralCard = move.card
            val result = negamax(
                board = newBoard, aiPlayer = aiPlayer, opponentPlayer = opponentPlayer,
                aiCards = if (playerToMove == aiPlayer) nextPlayerCards else otherCards,
                opponentCards = if (playerToMove == opponentPlayer) nextPlayerCards else otherCards,
                neutralCard = nextNeutralCard, depth = depth - 1, alpha = -beta, beta = -currentAlpha, playerToMove = opponent
            )

            val currentScore = -result.score


            if (depth == 3 && playerToMove == aiPlayer) {
                val pieceName = if (board[move.pieceRow][move.pieceCol] == blueKing || board[move.pieceRow][move.pieceCol] == redKing) "King" else "Pawn"
                val targetContent = if (board[move.targetRow][move.targetCol] == 0) "empty" else "CAPTURE"
                Log.d("AI_THINKING",
                    "Move: $pieceName (${move.pieceRow},${move.pieceCol}) -> (${move.targetRow},${move.targetCol}) " +
                            "($targetContent) with ${move.card.name}. Final Score: $currentScore"
                )
            }

            if (currentScore > maxScore) {
                maxScore = currentScore
                bestMove = move
            }
            currentAlpha = max(currentAlpha, maxScore)
            if (currentAlpha >= beta) break
        }
        return AIResult(bestMove, maxScore)
    }

    private fun generateAllMovesForPlayer(board: Array<IntArray>, player: Int, playerCards: List<Card>): List<AIMove> {
        val moves = mutableListOf<AIMove>()
        val friendlyPieces = if (player == redPlayer) listOf(redPiece, redKing) else listOf(bluePiece, blueKing)
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (board[r][c] in friendlyPieces) {
                    for (card in playerCards) {
                        val adjustedMoves = getAdjustedMoves(card, player)
                        for (move in adjustedMoves) {
                            val targetRow = r + move.deltaRow
                            val targetCol = c + move.deltaCol
                            if (targetRow in board.indices && targetCol in board[0].indices && board[targetRow][targetCol] !in friendlyPieces) {
                                moves.add(AIMove(r, c, targetRow, targetCol, card))
                            }
                        }
                    }
                }
            }
        }
        return moves
    }

    private fun getAdjustedMoves(card: Card, player: Int): List<Move> {
        return if (player == redPlayer) card.possibleMoves
        else card.possibleMoves.map { move -> Move(-move.deltaRow, -move.deltaCol) }
    }
}
