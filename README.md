
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
*   **Card System and UI:**
    *   Full integration of all 16 Onitama movement cards using custom PNG assets.
    *   Dealing of initial cards to players and the neutral "next" card.
    *   Visual display of player cards and the neutral card.
    *   Perspective-Correct Rotation: Blue player's cards (at the top) are rotated 180° to be upright from the Blue player's point of view.   
*   **Core Game Logic:**
    *   Turn-based gameplay for two local players (Red and Blue).
    *   Piece selection and deselection.
    *   Calculation of valid moves based on the player's selected piece and their available cards.
    *   Movement of pieces on the board according to card patterns, with correct move interpretation for both Red and Blue player perspectives.
    *   Capturing opponent pieces.
*   **Move Execution & Card Cycling:**
    *   When a move is made, the used card is passed to the side (neutral pile).
    *   If a move can be made by either of the player's two cards, the board highlights the cards in question, and the player must click on the card they wish to use to resolve the ambiguity.
    *   The player takes the previous neutral card into their hand.
    *   
*   **Win Conditions & Game Flow:**
    *   Win by Capture (Way of the Stone): The game correctly detects a win when the opponent's King is captured.
    *   Win by Temple Arch (Way of the Stream): The game correctly detects a win when a player's King reaches the opponent's starting Temple Arch square.
    *   Game Over Screen: A dedicated game over overlay appears upon winning, displaying the winner and the final score.
    *   "Play Again" / "Main Menu" functionality from the game over screen.
*   **User Interface:**
    *   Clickable game board cells and cards.
    *   Highlighting of selected pieces.
    *   Highlighting of valid move destinations combining possibilities from both cards (green for move, magenta for capture).
    *   Dedicated UI overlay for resolving ambiguous moves.
   
**Work In Progress / To-Do:**

*   **Card System and UI:**
    *   Still to visualize the neutral card.
*   **First player Move UI:**
    *   Currently uses a Toast. Plan to implement a more intuitive UI  to show first player to move.
*   **Game Engine / State Management:**
    *   While functional, the game state management could be more robust (e.g., dedicated game state class, clearer separation of concerns).
*   **Settings Menu:**
    *   Not yet implemented. Could include options for sound, themes, etc., in the future.
*   **Visual Polish & Animations:**
    *   No animations for piece movements or card transitions.
*   **Sound Effects / Music:**
    *   Not yet implemented (but I'm a musician so let me cook).
*   **AI Opponent:**
    *   A significant future and main goal, not yet started.
*   **Code Refinements & Testing:**
    *   Ongoing need for refactoring for clarity and efficiency.
    *   More comprehensive unit and UI testing.
*   **Persistence:**
    *   Game state is not saved if the app is closed (or the game view is closed going back to main menu).

## Technologies Used

*   **Language:** Kotlin
*   **Platform:** Android
*   **IDE:** Android Studio
*   **UI:** Android XML Layouts, `ImageViews` for board and pieces.

## How to Play (Current State)

1.  Launch the app and start a new game.
2.  The player whose stamp is on the initial neutral card starts.
3.  Click on one of your pieces (it will turn yellow). Valid moves based on your two cards will be highlighted (green for empty, magenta for capture).
4.  Click on a highlighted square to move your piece.
5.  If a move can be made by either of your cards, an overlay will appear. Click the card you wish to use.
6.  The card you used is swapped with the neutral card, and play passes to the next player.
7.  The game ends when one King is captured or a King reaches the opponent's Temple Arch.
8.  From the Game Over screen, choose to play again or return to the main menu.

## Contribution

This is currently a personal learning project. Suggestions and feedback are welcome via GitHub Issues.

---

Cheers, Onda
