package com.project.onitama

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.project.onitama.MainActivity.Companion.isVsComputer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val EMPTY_CELL = 0
const val RED_PIECE = 1
const val BLUE_PIECE = 2
const val RED_KING = 3
const val BLUE_KING = 4

const val RED_PLAYER = 10
const val BLUE_PLAYER = 20

// Ambiguous move
private data class AmbiguousMovePendingChoice(
    val fromRow: Int, val fromCol: Int,
    val toRow: Int, val toCol: Int,
    val wasCapture: Boolean,
    val pieceMoved: Int, // The piece type that was moved
    val possibleCards: List<Card>
)

class GameBoard : AppCompatActivity() {

    // USED CARDS
    private lateinit var redPlayerCard1: Card
    private lateinit var redPlayerCard2: Card
    private lateinit var bluePlayerCard1: Card
    private lateinit var bluePlayerCard2: Card
    private lateinit var neutralCard: Card

    // For storing the board state
    private val boardState = Array(5) { IntArray(5) }

    // Card ImageViews
    private lateinit var redPlayerCard1ImageView: ImageView
    private lateinit var redPlayerCard2ImageView: ImageView
    private lateinit var bluePlayerCard1ImageView: ImageView
    private lateinit var bluePlayerCard2ImageView: ImageView
    //    private lateinit var neutralCardImageView: ImageView

    private var currentPlayer = RED_PLAYER

    // For storing the currently selected piece's coordinates (if any)
    private var selectedPieceRow: Int = -1
    private var selectedPieceCol: Int = -1

    // To store your ImageViews for easy access
    private lateinit var cellImageViews: Array<Array<ImageView>>

    // Show possible moves
    private var combinedHighlightedMoves: List<HighlightedMoveInfo> = emptyList()

    //Ambiguous Moves (Same move for 2 cards)
    private var lastMoveAmbiguous = false

    private var pendingMoveDetails: AmbiguousMovePendingChoice? = null

    // AI variables
    private val aiEngine = OnitamaAIEngine(
        redPlayer = RED_PLAYER,
        bluePlayer = BLUE_PLAYER,
        redKing = RED_KING,
        blueKing = BLUE_KING,
        redPiece = RED_PIECE,
        bluePiece = BLUE_PIECE
    )
    private val aiPlayer = BLUE_PLAYER
    private val humanPlayer = RED_PLAYER
    private var isHumanTurn = true
    private val aiDepth = 3

    // Total score
    private var redPlayerScore = 0
    private var bluePlayerScore = 0
    private var isGameOver = false

    // UI Elements for Ambiguous Move Overlay
    private lateinit var blueAmbiguousMoveOverlay: View
    private lateinit var redAmbiguousMoveOverlay: View

    // UI Elements for Game Over Overlay
    private lateinit var gameOverOverlay: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var winnerText: TextView
    private lateinit var scoreText: TextView
    private lateinit var playAgainButton: Button
    private lateinit var mainMenuButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_board)

        initializeBoardImageViews()
        initializeCardImageViews()

        blueAmbiguousMoveOverlay = findViewById(R.id.blueAmbiguousMoveOverlay)
        redAmbiguousMoveOverlay = findViewById(R.id.redAmbiguousMoveOverlay)

        initializeGameOverOverlayViews()

        initializeBoardState()
        dealInitialCards()

        setupCellClickListeners()
        setupCardClickListeners()
        setupGameOverButtonListeners()

        updateCardDisplay()
        updateBoardUI() // Initial draw of the board based on boardState

        // Choose first player
        if(isVsComputer){
            if (!isGameOver && currentPlayer == aiPlayer) { triggerAITurn() }
            else { enablePlayerInput() }
        }
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

        /*
        val currentPlayerString = if (currentPlayer == RED_PLAYER) "RED" else "BLUE"
        Toast.makeText(this, "" +
                "Cards Dealt:\n" +
                "Red Player: ${redPlayerCard1.name}, ${redPlayerCard2.name}\n" +
                "Blue Player: ${bluePlayerCard1.name}, ${bluePlayerCard2.name}\n" +
                "Neutral Card: ${neutralCard.name}\n" +
                "Starting Player: $currentPlayerString\n", Toast.LENGTH_LONG).show()

        */
    }

    private fun initializeBoardImageViews() {
        cellImageViews =
            Array(5) { Array(5) { ImageView(this) } } // Initialize with placeholder/dummy ImageViews

        // Row 0
        cellImageViews[0][0] = findViewById(R.id.cell_0_0)
        cellImageViews[0][1] = findViewById(R.id.cell_0_1)
        cellImageViews[0][2] = findViewById(R.id.cell_0_2)
        cellImageViews[0][3] = findViewById(R.id.cell_0_3)
        cellImageViews[0][4] = findViewById(R.id.cell_0_4)

        // Row 1
        cellImageViews[1][0] = findViewById(R.id.cell_1_0)
        cellImageViews[1][1] = findViewById(R.id.cell_1_1)
        cellImageViews[1][2] = findViewById(R.id.cell_1_2)
        cellImageViews[1][3] = findViewById(R.id.cell_1_3)
        cellImageViews[1][4] = findViewById(R.id.cell_1_4)

        // Row 2
        cellImageViews[2][0] = findViewById(R.id.cell_2_0)
        cellImageViews[2][1] = findViewById(R.id.cell_2_1)
        cellImageViews[2][2] = findViewById(R.id.cell_2_2)
        cellImageViews[2][3] = findViewById(R.id.cell_2_3)
        cellImageViews[2][4] = findViewById(R.id.cell_2_4)

        // Row 3
        cellImageViews[3][0] = findViewById(R.id.cell_3_0)
        cellImageViews[3][1] = findViewById(R.id.cell_3_1)
        cellImageViews[3][2] = findViewById(R.id.cell_3_2)
        cellImageViews[3][3] = findViewById(R.id.cell_3_3)
        cellImageViews[3][4] = findViewById(R.id.cell_3_4)

        // Row 4
        cellImageViews[4][0] = findViewById(R.id.cell_4_0)
        cellImageViews[4][1] = findViewById(R.id.cell_4_1)
        cellImageViews[4][2] = findViewById(R.id.cell_4_2)
        cellImageViews[4][3] = findViewById(R.id.cell_4_3)
        cellImageViews[4][4] = findViewById(R.id.cell_4_4)
    }

    private fun initializeCardImageViews() {
        redPlayerCard1ImageView = findViewById(R.id.redPlayerCard1ImageView)
        redPlayerCard2ImageView = findViewById(R.id.redPlayerCard2ImageView)
        bluePlayerCard1ImageView = findViewById(R.id.bluePlayerCard1ImageView)
        bluePlayerCard2ImageView = findViewById(R.id.bluePlayerCard2ImageView)
        // neutralCardImageView = findViewById(R.id.neutralCardImageView)
        // redPlayerNeutralCardRevealArea = findViewById(R.id.redPlayerNeutralCardRevealArea)
    }

    private fun initializeGameOverOverlayViews() {
        gameOverOverlay = findViewById(R.id.gameOverOverlay)
        winnerText = findViewById(R.id.winnerText)
        scoreText = findViewById(R.id.scoreText)
        playAgainButton = findViewById(R.id.playAgainButton)
        mainMenuButton = findViewById(R.id.mainMenuButton)
    }

    private fun initializeBoardState() {

        // RESET ALL the cells
        for (row in 0..4) {
            for (col in 0..4) {
                boardState[row][col] = EMPTY_CELL
            }
        }

        // Place the pieces
        for (col in 0..4) {
            boardState[4][col] = RED_PIECE
        }
        boardState[4][2] = RED_KING
        for (col in 0..4) {
            boardState[0][col] = BLUE_PIECE
        }
        boardState[0][2] = BLUE_KING
    }

    private fun setupCellClickListeners() {
        for (row in 0..4) {
            for (col in 0..4) {
                cellImageViews[row][col].setOnClickListener {
                    if (!isHumanTurn || isGameOver) return@setOnClickListener
                    handleCellClick(row, col)
                }
            }
        }
    }

    private fun setupCardClickListeners() {

        redPlayerCard1ImageView.setOnClickListener {
            if (::redPlayerCard1.isInitialized) {
                if (!isHumanTurn || isGameOver) return@setOnClickListener
                handlePlayerCardClicked(redPlayerCard1)
            }
        }
        redPlayerCard2ImageView.setOnClickListener {
            if (::redPlayerCard2.isInitialized) {
                if (!isHumanTurn || isGameOver) return@setOnClickListener
                handlePlayerCardClicked(redPlayerCard2)
            }
        }

        // Blue cards active only VS human
        if (!isVsComputer){
            bluePlayerCard1ImageView.setOnClickListener {
                if (::bluePlayerCard1.isInitialized) {
                    handlePlayerCardClicked(bluePlayerCard1)
                }
            }
            bluePlayerCard2ImageView.setOnClickListener {
                if (::bluePlayerCard2.isInitialized) {
                    handlePlayerCardClicked(bluePlayerCard2)
                }
            }
        }

    }

    private fun handlePlayerCardClicked(clickedCard: Card) {
        // Only proceed if an ambiguous move is actually pending
        if (lastMoveAmbiguous && pendingMoveDetails != null) {
            val details = pendingMoveDetails!!

            if (details.possibleCards.contains(clickedCard)) {

                blueAmbiguousMoveOverlay.visibility = View.GONE
                redAmbiguousMoveOverlay.visibility = View.GONE
                performPostMoveActions(clickedCard)

                lastMoveAmbiguous = false
                pendingMoveDetails = null
            }
        }
    }

    private fun updateBoardUI() {
        for (row in 0..4) {
            for (col in 0..4) {
                if (!::cellImageViews.isInitialized || cellImageViews.getOrNull(row)
                        ?.getOrNull(col) == null
                ) {
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
                val highlightedMoveTarget =
                    combinedHighlightedMoves.firstOrNull { it.row == row && it.col == col }

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
                        cellImageViews[row][col].foreground = null
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
        if (pieceType == EMPTY_CELL) return false
        return !isCurrentPlayerPiece(pieceType)
    }

    private fun calculateAndHighlightCombinedValidMoves() {
        Log.i(
            "HighlightCalc",
            "Attempting to calculate. Selected: ($selectedPieceRow, $selectedPieceCol), Player: $currentPlayer"
        )
        combinedHighlightedMoves = emptyList()

        if (selectedPieceRow == -1 || selectedPieceCol == -1) {
            updateBoardUI()
            return
        }
        val pieceTypeAtSelection = boardState[selectedPieceRow][selectedPieceCol]
        if (!isCurrentPlayerPiece(pieceTypeAtSelection)) {
            updateBoardUI()
            return
        }

        val playerCards: List<Card> =
            getPlayerCardsForHighlighting(currentPlayer) // Helper function
        if (playerCards.isEmpty()) {
            Log.w(
                "HighlightCalc",
                "No player cards available for highlighting. Player: $currentPlayer"
            )
            updateBoardUI(); return
        }

        val tempDestinations = mutableMapOf<Pair<Int, Int>, HighlightedMoveInfo>()

        for (card in playerCards) {
            val adjustedMoves = getAdjustedMoves(card, currentPlayer)
            for (move in adjustedMoves) {
                val endRow = selectedPieceRow + move.deltaRow
                val endCol = selectedPieceCol + move.deltaCol

                if (endRow in 0..4 && endCol in 0..4) {
                    val targetCellContent = boardState[endRow][endCol]
                    if (!isCurrentPlayerPiece(targetCellContent)) {
                        val destinationKey = Pair(endRow, endCol)
                        val existingMoveInfo = tempDestinations[destinationKey]
                        val isCaptureMove = isOpponentPiece(targetCellContent)

                        if (existingMoveInfo != null) {
                            // Destination already reachable. Add current 'card' to its enablingCards.
                            val updatedEnablingCards =
                                existingMoveInfo.enablingCards.toMutableList()
                            if (!updatedEnablingCards.contains(card)) {
                                updatedEnablingCards.add(card)
                            }
                            tempDestinations[destinationKey] = existingMoveInfo.copy(
                                enablingCards = updatedEnablingCards.toList()
                            )
                        } else {
                            // First path to this destination cell.
                            tempDestinations[destinationKey] = HighlightedMoveInfo(
                                row = endRow,
                                col = endCol,
                                isCapture = isCaptureMove,
                                enablingCards = listOf(card) // Store as a list with the one card
                            )
                        }
                    }
                }
            }
        }
        combinedHighlightedMoves = tempDestinations.values.toList()
        Log.i("HighlightCalc", "Finished. Combined highlights: ${combinedHighlightedMoves.size}")
        updateBoardUI()
    }

    // Helper function to get player cards
    private fun getPlayerCardsForHighlighting(player: Int): List<Card> {
        return when (player) {
            RED_PLAYER -> {
                if (::redPlayerCard1.isInitialized && ::redPlayerCard2.isInitialized) {
                    listOf(redPlayerCard1, redPlayerCard2)
                } else emptyList()
            }

            BLUE_PLAYER -> {
                if (::bluePlayerCard1.isInitialized && ::bluePlayerCard2.isInitialized) {
                    listOf(bluePlayerCard1, bluePlayerCard2)
                } else emptyList()
            }
            else -> emptyList()
        }
    }

    private fun handleCellClick(clickedRow: Int, clickedCol: Int) {

        val clickedPieceType = boardState[clickedRow][clickedCol]
        val targetMoveInfo =
            combinedHighlightedMoves.firstOrNull { it.row == clickedRow && it.col == clickedCol }

        // ACTION 1: Attempting to Execute a Highlighted Move
        if (targetMoveInfo != null) {
            if (selectedPieceRow == -1 || selectedPieceCol == -1) {
                Log.e("GameError", "Highlighted cell clicked, but no piece was selected! Clearing.")
                clearSelectionsAndCombinedHighlights()
                return
            }

            val pieceToMove = boardState[selectedPieceRow][selectedPieceCol]
            val enablingCardsForThisMove = targetMoveInfo.enablingCards

            //  Check for win by capture of King
            if (targetMoveInfo.isCapture && (clickedPieceType == RED_KING || clickedPieceType == BLUE_KING)) {
                handleWin(currentPlayer)
                return // Game over
            }
            // Check for win by Temple Arch
            if (checkWinByTempleArch(pieceToMove, clickedRow, clickedCol)) {
                handleWin(currentPlayer)
                return // Game over
            }

            // --- Core Logic for Handling Click on Highlighted Cell ---
            when (enablingCardsForThisMove.size) {
                1 -> { // UNAMBIGUOUS MOVE: Only one card can make this move
                    val cardActuallyUsed = enablingCardsForThisMove.first()

                    boardState[clickedRow][clickedCol] = pieceToMove
                    boardState[selectedPieceRow][selectedPieceCol] = EMPTY_CELL

                    // Perform post-move actions if game still going
                    performPostMoveActions(cardActuallyUsed)
                }

                2 -> { // AMBIGUOUS MOVE: Two cards can make this move

                    // Move the piece
                    boardState[clickedRow][clickedCol] = pieceToMove
                    boardState[selectedPieceRow][selectedPieceCol] = EMPTY_CELL

                    // Set state to wait for player to click a card
                    lastMoveAmbiguous = true
                    pendingMoveDetails = AmbiguousMovePendingChoice(
                        fromRow = selectedPieceRow,
                        fromCol = selectedPieceCol,

                        toRow = clickedRow,
                        toCol = clickedCol,
                        wasCapture = targetMoveInfo.isCapture,
                        pieceMoved = pieceToMove,
                        possibleCards = enablingCardsForThisMove
                    )

                    // Create an overlay to highlight the choice of the card
                    if (currentPlayer == BLUE_PLAYER) { blueAmbiguousMoveOverlay.visibility = View.VISIBLE }
                    else { redAmbiguousMoveOverlay.visibility = View.VISIBLE }

                    // Clear selection and current highlights (as piece has "moved" pending choice)
                    selectedPieceRow = -1
                    selectedPieceCol = -1
                    combinedHighlightedMoves = emptyList() // Clear move highlights

                    updateBoardUI()
                    updateCardDisplay()
                }
            }
            return // End of ACTION 1
        }

        // ACTION 2: Attempting to Select/Deselect a Piece (if not an ambiguous move pending)
        if (isCurrentPlayerPiece(clickedPieceType)) {
            Log.d("GameInput", "Clicked on current player's piece.")
            if (selectedPieceRow == clickedRow && selectedPieceCol == clickedCol) {
                Log.d("GameSelection", "Deselecting piece at ($clickedRow, $clickedCol).")
                clearSelectionsAndCombinedHighlights() // Clears selection & highlights, then updates UI
            } else {
                Log.d(
                    "GameSelection",
                    "Selecting piece ($clickedPieceType) at ($clickedRow, $clickedCol)."
                )
                selectedPieceRow = clickedRow
                selectedPieceCol = clickedCol
                calculateAndHighlightCombinedValidMoves() // Calculate based on new piece selection
            }
            return // End of ACTION 2 processing
        }

        // ACTION 3: Clicked an Empty Cell or Opponent's Piece (NOT a highlighted move, and not own piece)
        Log.d("GameInput", "Clicked non-highlighted, non-own-piece cell.")
        if (selectedPieceRow != -1) { // If a piece was selected, and they clicked "off"
            Log.d("GameSelection", "Clearing selection as click was off highlighted area.")
            clearSelectionsAndCombinedHighlights()
        }
        // No specific action if no piece was selected and they click an empty/opponent cell. UI just updates.
    }

    private fun clearSelectionsAndCombinedHighlights() {
        selectedPieceRow = -1
        selectedPieceCol = -1
        calculateAndHighlightCombinedValidMoves()
    }

    private fun checkWinByTempleArch(pieceMoved: Int, endRow: Int, endCol: Int): Boolean {
        // Check for RED KING at BLUE's Temple Arch
        if (currentPlayer == RED_PLAYER) {
            if (endRow == BLUE_TEMPLE_ARCH_ROW && endCol == BLUE_TEMPLE_ARCH_COL) {
                if (pieceMoved == RED_KING) {
                    return true
                }
            }
        }
        // Check for BLUE KING at RED's Temple Arch
        else if (currentPlayer == BLUE_PLAYER) {
            if (endRow == RED_TEMPLE_ARCH_ROW && endCol == RED_TEMPLE_ARCH_COL) {
                if (pieceMoved == BLUE_KING) {
                    return true
                }
            }
        }
        return false
    }

    private fun handleWin(currentPlayer: Int) {
        showGameOverScreen(currentPlayer)
    }

    private fun showGameOverScreen(winningPlayer: Int) {
        if (isGameOver) return // Prevent multiple triggers from different wind condition
        isGameOver = true

        // Update scores
        val winnerName: String
        if (winningPlayer == RED_PLAYER) {
            redPlayerScore++
            winnerName = "Red Player"
        } else {
            bluePlayerScore++
            winnerName = "Blue Player"
        }

        // Update UI Text
        winnerText.text = buildString {
            append(winnerName)
            append(" ")
            append(getString(R.string.win))
        }

        // --- Create SpannableString for Score ---
        val redScoreStr = redPlayerScore.toString()
        val blueScoreStr = bluePlayerScore.toString()
        val separator = " - "

        // Get your drawables
        val redPieceDrawable = ContextCompat.getDrawable(this, R.drawable.ic_red_piece)
        val bluePieceDrawable = ContextCompat.getDrawable(this, R.drawable.ic_blue_piece)

        // TODO considering put this somewhere else as
        // private const val SCORE_IMAGE_SIZE_DP = 20
        val imageSizePx = (20 * resources.displayMetrics.density).toInt()

        redPieceDrawable?.setBounds(0, 0, imageSizePx, imageSizePx)
        bluePieceDrawable?.setBounds(0, 0, imageSizePx, imageSizePx)

        val spannableScoreString = SpannableStringBuilder()

        if (redPieceDrawable != null) {
            spannableScoreString.append(" ") // Placeholder for image
            val redImageSpan = ImageSpan(redPieceDrawable, ImageSpan.ALIGN_BOTTOM)
            // Replace the last character (placeholder) with the image span
            spannableScoreString.setSpan(
                redImageSpan,
                spannableScoreString.length - 1,
                spannableScoreString.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        spannableScoreString.append(" ") // Space after red piece
        spannableScoreString.append(redScoreStr)
        spannableScoreString.append(separator)
        spannableScoreString.append(blueScoreStr)
        spannableScoreString.append(" ") // Space before blue piece

        if (bluePieceDrawable != null) {
            spannableScoreString.append(" ") // Placeholder for image
            val blueImageSpan = ImageSpan(bluePieceDrawable, ImageSpan.ALIGN_BOTTOM)
            // Replace the last character (placeholder) with the image span
            spannableScoreString.setSpan(
                blueImageSpan,
                spannableScoreString.length - 1,
                spannableScoreString.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        scoreText.text = spannableScoreString
        // --- End of SpannableString for Score ---


        // Make overlay visible
        gameOverOverlay.visibility = View.VISIBLE

        // Disable board interactions
        combinedHighlightedMoves = emptyList()
        selectedPieceRow = -1
        selectedPieceCol = -1
        updateBoardUI()
    }

    private fun setupGameOverButtonListeners() {
        playAgainButton.setOnClickListener {
            resetGameForPlayAgain()
        }

        mainMenuButton.setOnClickListener {
            Intent(this, MainActivity::class.java).also { startActivity(it) }
            finish()
        }
    }

    private fun resetGameForPlayAgain() {
        isGameOver = false
        gameOverOverlay.visibility = View.GONE // Hide the overlay

        // Reset game state (not score)
        selectedPieceRow = -1
        selectedPieceCol = -1
        combinedHighlightedMoves = emptyList()
        lastMoveAmbiguous = false
        pendingMoveDetails = null

        initializeBoardState()
        dealInitialCards()
        updateCardDisplay()
        updateBoardUI()
        // Choose first player
        if(isVsComputer){
            if (!isGameOver && currentPlayer == aiPlayer) { triggerAITurn() }
            else { enablePlayerInput() }
        }
    }

    private fun performPostMoveActions(usedCard: Card) {
        Log.d("GameMove", "Performing post-move actions. Card used: ${usedCard.name}")

        // 1. Swap used card with neutral card
        val tempCardHolding = neutralCard
        neutralCard = usedCard

        // Update player's hand
        if (currentPlayer == RED_PLAYER) {
            if (redPlayerCard1 == usedCard) redPlayerCard1 = tempCardHolding
            else if (redPlayerCard2 == usedCard) redPlayerCard2 = tempCardHolding
        } else { // BLUE_PLAYER
            if (bluePlayerCard1 == usedCard) bluePlayerCard1 = tempCardHolding
            else if (bluePlayerCard2 == usedCard) bluePlayerCard2 = tempCardHolding
        }
        Log.d("GameMove", "Card swap complete. New neutral: ${neutralCard.name}.")

        // 2. Deselect piece
        clearSelectionsAndCombinedHighlights()

        // 3. Switch turn
        switchTurn()

        // 4. Update UI
        updateBoardUI()
        updateCardDisplay()

        if(isVsComputer){
            if (!isGameOver && currentPlayer == aiPlayer) { triggerAITurn() }
            else if (!isGameOver) { enablePlayerInput() }
        }


        Log.d("GameMove", "Post-move actions complete. New turn for player: $currentPlayer")
    }

    private fun triggerAITurn() {
        disablePlayerInput()

        lifecycleScope.launch(Dispatchers.Default) {
            val aiCards = getPlayerCardsForHighlighting(aiPlayer)
            val humanCards = getPlayerCardsForHighlighting(humanPlayer)

            if (aiCards.isEmpty() || humanCards.isEmpty() || !::neutralCard.isInitialized) {
                Log.e("AI_ERROR", "Cannot run AI, cards are not ready.")
                withContext(Dispatchers.Main) { enablePlayerInput() } // Let human play if AI fails
                return@launch
            }

            Log.d("AI_THINKING", "AI is thinking...")
            val result = aiEngine.findBestMove(
                board = boardState.map { it.clone() }.toTypedArray(), // Pass a safe copy
                aiPlayer = aiPlayer,
                opponentPlayer = humanPlayer,
                aiCards = aiCards,
                opponentCards = humanCards,
                neutralCard = neutralCard,
                depth = aiDepth
            )

            // Switch back to the main thread to execute the AI's chosen move
            withContext(Dispatchers.Main) {
                // thinkingIndicator.visibility = View.GONE
                val bestMove = result.move
                if (bestMove != null) {
                    Log.d("AI_ACTION", "AI chose move with card ${bestMove.card.name}. Score: ${result.score}")
                    executeAIMove(bestMove)
                } else {
                    Log.e("AI_ERROR", "AI returned no valid move (stalemate or loss).")
                    enablePlayerInput() // AI can't move, give control back to human
                }
            }
        }
    }

    // Change which player is playing
    private fun disablePlayerInput() {
        isHumanTurn = false
        Log.d("GameInput", "Player input DISABLED.")
    }

    private fun enablePlayerInput() {
        isHumanTurn = true
        Log.d("GameInput", "Player input ENABLED.")
    }



    private fun executeAIMove(move: AIMove) {
        Log.d("AI_EXECUTE", "AI executing move: ${move.card.name} to (${move.targetRow}, ${move.targetCol})")

        // We will directly update the board state and then call performPostMoveActions.
        val pieceType = boardState[move.pieceRow][move.pieceCol]
        val capturedPieceType = boardState[move.targetRow][move.targetCol]

        // Update the board state model
        boardState[move.targetRow][move.targetCol] = pieceType
        boardState[move.pieceRow][move.pieceCol] = 0

        // Explicitly check for win conditions after AI move
        if (capturedPieceType == RED_KING) { // AI captured human king
            handleWin(aiPlayer)
            updateBoardUI() // Show the final board state
            return // Game over, stop here
        }
        if (checkWinByTempleArch(pieceType, move.targetRow, move.targetCol)) {
            handleWin(aiPlayer)
            updateBoardUI() // Show the final board state
            return // Game over, stop here
        }
        // This updates card and all the stuff
        performPostMoveActions(move.card)
    }

    private fun switchTurn() {
        currentPlayer = if (currentPlayer == RED_PLAYER) BLUE_PLAYER else RED_PLAYER
        // Toast.makeText(this, "Current Player: ${if (currentPlayer == RED_PLAYER) "RED TURN" else "BLUE TURN"}", Toast.LENGTH_SHORT).show()
    }

    private fun getDrawableResourceForCardSealed(card: Card): Int {
        return when (card.name) {
            TIGER_CARD.name -> R.drawable.tiger
            CRAB_CARD.name -> R.drawable.crab
            MONKEY_CARD.name -> R.drawable.monkey
            CRANE_CARD.name -> R.drawable.crane
            DRAGON_CARD.name -> R.drawable.dragon
            ELEPHANT_CARD.name -> R.drawable.elephant
            MANTIS_CARD.name -> R.drawable.mantis
            BOAR_CARD.name -> R.drawable.boar
            FROG_CARD.name -> R.drawable.frog
            GOOSE_CARD.name -> R.drawable.goose
            HORSE_CARD.name -> R.drawable.horse
            EEL_CARD.name -> R.drawable.eel
            COBRA_CARD.name -> R.drawable.cobra
            OX_CARD.name -> R.drawable.ox
            RABBIT_CARD.name -> R.drawable.rabbit
            ROOSTER_CARD.name -> R.drawable.rooster
            else -> R.drawable.ic_empty_square // A fallback image if a card name isn't mapped
        }
    }

    private fun updateCardDisplay() {
        val blueCardRotation = 180f // Rotate Blue's cards
        val redCardRotation = 0f

        if (::bluePlayerCard1.isInitialized) {
            val resId1 = getDrawableResourceForCardSealed(bluePlayerCard1)
            bluePlayerCard1ImageView.setImageResource(resId1)
            bluePlayerCard1ImageView.rotation = blueCardRotation // Apply rotation
            bluePlayerCard1ImageView.contentDescription =
                "Blue Player Card 1: ${bluePlayerCard1.name}"
        }

        if (::bluePlayerCard2.isInitialized) {
            val resId2 = getDrawableResourceForCardSealed(bluePlayerCard2)
            bluePlayerCard2ImageView.setImageResource(resId2)
            bluePlayerCard2ImageView.rotation = blueCardRotation // Apply rotation
            bluePlayerCard2ImageView.contentDescription =
                "Blue Player Card 2: ${bluePlayerCard2.name}"
        }

        if (::redPlayerCard1.isInitialized) {
            val resId1 = getDrawableResourceForCardSealed(redPlayerCard1)
            redPlayerCard1ImageView.setImageResource(resId1)
            redPlayerCard1ImageView.rotation = redCardRotation // Apply rotation (0 degrees)
            redPlayerCard1ImageView.contentDescription = "Red Player Card 1: ${redPlayerCard1.name}"
        }

        if (::redPlayerCard2.isInitialized) {
            val resId2 = getDrawableResourceForCardSealed(redPlayerCard2)
            redPlayerCard2ImageView.setImageResource(resId2)
            redPlayerCard2ImageView.rotation = redCardRotation // Apply rotation (0 degrees)
            redPlayerCard2ImageView.contentDescription = "Red Player Card 2: ${redPlayerCard2.name}"
        }
        /*
        if (::neutralCard.isInitialized) {
            val resIdNeutral = getDrawableResourceForCardSealed(neutralCard)
            neutralCardImageView.setImageResource(resIdNeutral)
            neutralCardImageView.contentDescription = "Neutral Card: ${neutralCard.name}"
            if (currentPlayer == RED_PLAYER) {
                neutralCardImageView.rotation = redCardRotation
            } else {
                neutralCardImageView.rotation = blueCardRotation
            }
        }*/

    }
}