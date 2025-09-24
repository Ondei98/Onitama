
# Onitama - Android Edition

A digital implementation of the elegant and strategic board game Onitama, built for Android using Kotlin.

## Project Goal

The primary goal of this project is to create a fully functional and enjoyable mobile version of Onitama, allowing two players to compete locally on the same device. Future enhancements might include AI opponents or online play, but the initial focus is on a polished local multiplayer experience.

## Current Progress & Features

Here's a snapshot of what has been implemented so far and what's planned:

**Implemented Features:**

*   **Game Board & Pieces:**
    *   Visual representation of the 5x5 game board.
    *   Distinct pawn and king pieces for both Red and Blue players.
    *   Correct initial setup of pieces on the board.
*   **Card System:**
    *   Representation of Onitama's unique movement cards.
    *   Dealing of initial cards to players and the neutral "next" card.
    *   Visual display of player cards and the neutral card.
*   **Core Game Logic:**
    *   Turn-based gameplay for two local players (Red and Blue).
    *   Piece selection and deselection.
    *   Calculation of valid moves based on the player's selected piece and their available cards.
    *   Movement of pieces on the board according to card patterns.
    *   Perspective adjustment for card moves (moves are correctly interpreted for both Red and Blue players).
    *   Capturing opponent pieces.
*   **Move Execution & Card Cycling:**
    *   When a move is made, the used card is passed to the side (neutral pile).
    *   The player takes the previous neutral card into their hand.
*   **Basic Win Conditions:**
    *   Win by capturing the opponent's King.
    *   Win by moving your King to the opponent's starting Temple Arch square.
*   **User Interface (Basic):**
    *   Clickable game board cells.
    *   Highlighting of selected pieces.
    *   Highlighting of valid move destinations (green for move, magenta for capture).
    *   Basic display of whose turn it is.
   
**Work In Progress / To-Do:**

*   **Refined Ambiguous Move UI:**
    *   Currently uses a Toast and expects a card click. Plan to implement a more intuitive UI (e.g., AlertDialog or visual card choice directly on the board) for selecting which card to use when a move is possible with both.
*   **Game Engine / State Management:**
    *   While functional, the game state management could be more robust (e.g., dedicated game state class, clearer separation of concerns).
    *   No formal "Game Over" screen or "Play Again" functionality yet.
*   **Settings Menu:**
    *   Not yet implemented. Could include options for sound, themes, etc., in the future.
*   **Visual Polish & Animations:**
    *   UI is currently functional but basic.
    *   No animations for piece movements or card transitions.
*   **Sound Effects / Music:**
    *   Not yet implemented.
*   **AI Opponent:**
    *   A significant future and main goal, not yet started.
*   **Code Refinements & Testing:**
    *   Ongoing need for refactoring for clarity and efficiency.
    *   More comprehensive unit and UI testing.
*   **Persistence:**
    *   Game state is not saved if the app is closed.

## Technologies Used

*   **Language:** Kotlin
*   **Platform:** Android
*   **IDE:** Android Studio
*   **UI:** Android XML Layouts, `ImageViews` for board and pieces.

## How to Play (Current State)

1.  Launch the app.
2.  First player based on the card stamp starts.
3.  Click on one of your pieces (it will turn yellow). Valid moves based on your two cards will be highlighted (green for empty, magenta for capture).
4.  Click on a highlighted square to move your piece.
5.  The card you conceptually used will be swapped with the neutral card.
6.  Play passes to the second player.
7.  If a move can be made by either of your cards to the same square, the left card will be used (to fix off course!).
8.  The game ends when one King is captured or a King reaches the opponent's Temple Arch. (Win condition is logged, no special game over screen yet).

## Contribution

This is currently a personal learning project. Suggestions and feedback are welcome via GitHub Issues.

---

Cheers, Onda
