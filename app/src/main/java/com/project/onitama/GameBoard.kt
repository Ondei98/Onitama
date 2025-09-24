package com.project.onitama // Make sure this matches your project's package name

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

const val EMPTY_CELL = 0
const val RED_PIECE = 1
const val BLUE_PIECE = 2
const val RED_KING = 3
const val BLUE_KING = 4

const val RED_PLAYER = 10
const val BLUE_PLAYER = 20
private var currentPlayer = RED_PLAYER


// USED CARDS
private lateinit var redPlayerCard1: Card
private lateinit var redPlayerCard2: Card
private lateinit var bluePlayerCard1: Card
private lateinit var bluePlayerCard2: Card
private lateinit var neutralCard: Card


private var currentlySelectedPlayerCard: Card? = null // This will store the card the player intends to use

// For storing the board state
private val boardState = Array(5) { IntArray(5) }

// For storing the currently selected piece's coordinates (if any)
private var selectedPieceRow: Int = -1
private var selectedPieceCol: Int = -1

// To store your ImageViews for easy access
private lateinit var cellImageViews: Array<Array<ImageView>>

// Show possible moves
private var combinedHighlightedMoves: List<HighlightedMoveWithCard> = emptyList()

class GameBoard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_board)

        initializeBoardImageViews()
        initializeBoardState() // Set initial piece positions in boardState array
        dealInitialCards()
        setupCellClickListeners()
        updateBoardUI() // Initial draw of the board based on boardState
        updateCardDisplay() // Initial display of cards
    }


    private fun dealInitialCards() {
        val shuffledDeck = ALL_GAME_CARDS.shuffled()

        redPlayerCard1 = shuffledDeck[0]
        redPlayerCard2 = shuffledDeck[1]
        bluePlayerCard1 = shuffledDeck[2]
        bluePlayerCard2 = shuffledDeck[3]
        neutralCard = shuffledDeck[4]

        currentPlayer = if (neutralCard.stampColor == CardStampColor.BLUE) {
            BLUE_PLAYER
        } else { RED_PLAYER }

        val currentPlayerString = if (currentPlayer == RED_PLAYER) "RED" else "BLUE"

        Toast.makeText(this, "" +
                "Cards Dealt:\n" +
                "Red Player: ${redPlayerCard1.name}, ${redPlayerCard2.name}\n" +
                "Blue Player: ${bluePlayerCard1.name}, ${bluePlayerCard2.name}\n" +
                "Neutral Card: ${neutralCard.name}\n" +
                "Starting Player: $currentPlayerString\n", Toast.LENGTH_LONG).show()
    }

    private fun initializeBoardImageViews() {
        cellImageViews = Array(5) { row ->
            Array(5) { col ->
                val cellId = resources.getIdentifier("cell_${row}_${col}", "id", packageName)
                findViewById<ImageView>(cellId)
            }
        }
    }

    private fun initializeBoardState() {
        for (col in 0..4) { boardState[4][col] = RED_PIECE }
        boardState[4][2] = RED_KING
        for (col in 0..4) { boardState[0][col] = BLUE_PIECE }
        boardState[0][2] = BLUE_KING
    }

    private fun setupCellClickListeners() {
        for (row in 0..4) {
            for (col in 0..4) {
                cellImageViews[row][col].setOnClickListener {
                    handleCellClick(row, col)
                }
            }
        }
    }

    private fun updateBoardUI() {
        // Log.d("UIUpdate", "Updating Board UI. SelectedPiece:($selectedPieceRow,$selectedPieceCol), Highlights:${combinedHighlightedMoves.size}")
        for (row in 0..4) {
            for (col in 0..4) {
                if (!::cellImageViews.isInitialized || cellImageViews.getOrNull(row)?.getOrNull(col) == null) {
                    Log.e("UIUpdate", "cellImageViews not fully initialized or accessed out of bounds at [$row][$col]. Skipping UI update for this cell.")
                    continue
                }

                val pieceType = boardState[row][col]
                val cellDrawableRes = when (pieceType) {
                    RED_PIECE -> R.drawable.ic_red_piece
                    BLUE_PIECE -> R.drawable.ic_blue_piece
                    RED_KING -> R.drawable.ic_red_king
                    BLUE_KING -> R.drawable.ic_blue_king
                    EMPTY_CELL -> R.drawable.ic_empty_square
                    else -> R.drawable.ic_empty_square
                }
                cellImageViews[row][col].setImageResource(cellDrawableRes)

                val isSelectedPieceCell = (row == selectedPieceRow && col == selectedPieceCol)
                val highlightedMoveTarget = combinedHighlightedMoves.firstOrNull { it.row == row && it.col == col }

                when {
                    isSelectedPieceCell -> {
                        cellImageViews[row][col].setBackgroundColor(android.graphics.Color.YELLOW)
                    }
                    highlightedMoveTarget != null -> {
                        if (highlightedMoveTarget.isCapture) {
                            cellImageViews[row][col].setBackgroundColor(android.graphics.Color.MAGENTA)
                        } else {
                            cellImageViews[row][col].setBackgroundColor(android.graphics.Color.GREEN)
                        }
                    }
                    else -> {
                        cellImageViews[row][col].setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                }
            }
        }
    }


    private fun isCurrentPlayerPiece(pieceType: Int): Boolean {
        return if (currentPlayer == RED_PLAYER) {
            pieceType == RED_PIECE || pieceType == RED_KING
        } else { // BLUE_PLAYER
            pieceType == BLUE_PIECE || pieceType == BLUE_KING
        }
    }

    private fun isOpponentPiece(pieceType: Int): Boolean {
        if (pieceType == EMPTY_CELL) return false // Not an opponent if the cell is empty
        return !isCurrentPlayerPiece(pieceType)
    }

    private fun calculateAndHighlightCombinedValidMoves() {
        Log.d("CombinedHighlight", "Calculating combined highlights. Piece:($selectedPieceRow,$selectedPieceCol)")
        combinedHighlightedMoves = emptyList() // Start fresh

        if (selectedPieceRow != -1 && selectedPieceCol != -1) { // Only need a piece selected
            val pieceType = boardState[selectedPieceRow][selectedPieceCol]
            if (!isCurrentPlayerPiece(pieceType)) {
                Log.w("CombinedHighlight", "Selected piece $pieceType does not belong to current player $currentPlayer.")
                updateBoardUI()
                return
            }

            val playerCards = if (currentPlayer == RED_PLAYER) {
                listOf(redPlayerCard1, redPlayerCard2)
            } else {
                listOf(bluePlayerCard1, bluePlayerCard2)
            }

            val potentialDestinations = mutableListOf<HighlightedMoveWithCard>()

            for (card in playerCards) {
                val adjustedMoves = getAdjustedMoves(card, currentPlayer, RED_PLAYER)

                for (move in adjustedMoves) {
                    val endRow = selectedPieceRow + move.deltaRow
                    val endCol = selectedPieceCol + move.deltaCol

                    if (endRow in 0..4 && endCol in 0..4) {
                        val targetCellContent = boardState[endRow][endCol]
                        if (!isCurrentPlayerPiece(targetCellContent)) {
                            // Add if not already present from another card for the same destination
                            if (!potentialDestinations.any { it.row == endRow && it.col == endCol }) {
                                potentialDestinations.add(
                                    HighlightedMoveWithCard(
                                        endRow,
                                        endCol,
                                        isCapture = isOpponentPiece(targetCellContent),
                                        enablingCard = card
                                    )
                                )
                            } else {
                                // If already present, you might want to store that multiple cards enable it,
                                // but for simplicity, the first card found wins for now.
                                // Or, if a move is possible with card A and card B, and card A is listed,
                                // and the player picks that square, you'd use card A.
                                // This gets complex if you need to let player choose *which* card to use
                                // if multiple enable the same highlighted square.
                                // For now, the first card that enables it is stored.
                            }
                        }
                    }
                }
            }
            combinedHighlightedMoves = potentialDestinations
            Log.d("CombinedHighlight", "Final combined highlighted moves ($combinedHighlightedMoves.size): $combinedHighlightedMoves")
        }
        updateBoardUI() // This will now use combinedHighlightedMoves
    }

    private fun handleCellClick(clickedRow: Int, clickedCol: Int) {
        val clickedPieceType = boardState[clickedRow][clickedCol]
        Log.d("GameInput", "Cell clicked: ($clickedRow, $clickedCol). PieceType: $clickedPieceType")

        // ACTION 1: Attempting to Execute a Highlighted Move
        // We now use combinedHighlightedMoves
        val targetMoveWithCard = combinedHighlightedMoves.firstOrNull { it.row == clickedRow && it.col == clickedCol }

        if (targetMoveWithCard != null) {
            Log.d("GameInput", "Clicked on a combined highlighted move target: $targetMoveWithCard")
            // A piece must have been selected for combinedHighlightedMoves to be populated
            if (selectedPieceRow != -1) {
                val pieceToMove = boardState[selectedPieceRow][selectedPieceCol]
                val cardUsed = targetMoveWithCard.enablingCard // Get the card that enabled this move

                Log.d("GameMove", "Executing move from ($selectedPieceRow,$selectedPieceCol) to ($clickedRow,$clickedCol) using card: ${cardUsed.name}")

                boardState[clickedRow][clickedCol] = pieceToMove
                boardState[selectedPieceRow][selectedPieceCol] = EMPTY_CELL

                 if (targetMoveWithCard.isCapture && (clickedPieceType == RED_KING || clickedPieceType == BLUE_KING)) {
                     handleWinByCapture(currentPlayer)
                 }

                performPostMoveActions(cardUsed) // Pass the specific card that enabled the move
                return
            } else {
                // Should not happen if highlights are present
                Log.e("GameError", "Highlighted cell clicked, but no piece was selected! Clearing.")
                clearSelectionsAndCombinedHighlights()
                return
            }
        }

        // ACTION 2: Attempting to Select/Deselect a Piece
        if (isCurrentPlayerPiece(clickedPieceType)) {
            Log.d("GameInput", "Clicked on current player's piece.")
            if (selectedPieceRow == clickedRow && selectedPieceCol == clickedCol) {
                Log.d("GameSelection", "Deselecting piece at ($clickedRow, $clickedCol).")
                selectedPieceRow = -1
                selectedPieceCol = -1
                // currentlySelectedPlayerCard is not used in this flow for selection
            } else {
                Log.d("GameSelection", "Selecting piece ($clickedPieceType) at ($clickedRow, $clickedCol).")
                selectedPieceRow = clickedRow
                selectedPieceCol = clickedCol
            }
            calculateAndHighlightCombinedValidMoves() // Calculate based on new piece selection
            return
        }

        // ACTION 3: Clicked an Empty Cell or Opponent's Piece (NOT a highlighted move)
        Log.d("GameInput", "Clicked non-highlighted, non-own-piece cell.")
        if (selectedPieceRow != -1) { // If a piece was selected, and they clicked "off"
            Log.d("GameSelection", "Clearing selection as click was off highlighted area.")
            clearSelectionsAndCombinedHighlights() // Deselect the piece and clear its highlights
        }
    }

// Remove or comment out onPlayerCardSelected as it's not part of this direct flow
// private fun onPlayerCardSelected(card: Card) { ... }

    // Modify clearSelectionsAndHighlights
    private fun clearSelectionsAndCombinedHighlights() {
        Log.d("GameSelection", "Clearing piece selection and combined highlights.")
        selectedPieceRow = -1
        selectedPieceCol = -1
        // currentlySelectedPlayerCard = null; // No longer needed for this flow's primary selection
        calculateAndHighlightCombinedValidMoves() // This will clear combinedHighlightedMoves and update UI
    }

    private fun handleWinByCapture(currentPlayer: Int) {
        Toast.makeText(this, "Player $currentPlayer Wins!", Toast.LENGTH_SHORT).show()
    }

    private fun performPostMoveActions(usedCard: Card) { // Takes usedCard: Card
        Log.d("GameMove", "Performing post-move actions. Card used: ${usedCard.name}")

        // 1. Swap the used card with the neutral card
        val tempCardHolding = neutralCard
        neutralCard = usedCard

        // Update the player's hand (ensure cards are initialized)
        if (currentPlayer == RED_PLAYER) {
            if (!::redPlayerCard1.isInitialized || !::redPlayerCard2.isInitialized) { Log.e("CardSwap", "Red cards not init for swap!"); return }
            if (redPlayerCard1 == usedCard) redPlayerCard1 = tempCardHolding
            else if (redPlayerCard2 == usedCard) redPlayerCard2 = tempCardHolding
            else Log.e("CardSwapError", "Red player used card ${usedCard.name} not found in hand! Hand: ${redPlayerCard1.name}, ${redPlayerCard2.name}")
        } else { // BLUE_PLAYER
            if (!::bluePlayerCard1.isInitialized || !::bluePlayerCard2.isInitialized) { Log.e("CardSwap", "Blue cards not init for swap!"); return }
            if (bluePlayerCard1 == usedCard) bluePlayerCard1 = tempCardHolding
            else if (bluePlayerCard2 == usedCard) bluePlayerCard2 = tempCardHolding
            else Log.e("CardSwapError", "Blue player used card ${usedCard.name} not found in hand! Hand: ${bluePlayerCard1.name}, ${bluePlayerCard2.name}")
        }
        Log.d("GameMove", "Card swap complete. New neutral: ${neutralCard.name}.") // Simplified log

        // 2. Deselect piece
        selectedPieceRow = -1
        selectedPieceCol = -1

        // 3. Switch turn
        switchTurn() // This also updates the currentPlayer variable

        // 4. Update UI
        updateCardDisplay() // Show new card arrangement
        calculateAndHighlightCombinedValidMoves() // Clear highlights for the new turn

        Log.d("GameMove", "Post-move actions complete. New turn for player: $currentPlayer")
    }


    private fun switchTurn() {
        currentPlayer = if (currentPlayer == RED_PLAYER) BLUE_PLAYER else RED_PLAYER
        Toast.makeText(this, "Current Player: ${if (currentPlayer == RED_PLAYER) "RED TURN" else "BLUE TURN"}", Toast.LENGTH_SHORT).show()
    }

    private fun updateCardDisplay() {
        // Example: imageViewRedCard1.setImageResource(getDrawableForCard(redPlayerCard1))
        // imageViewRedCard2.setImageResource(getDrawableForCard(redPlayerCard2))
        // ... and so on for blue and neutral cards
        // This is highly dependent on how your card UI is set up (ImageViews, TextViews, etc.)
    }

}
