package com.example.tictactoe.service;

import com.example.tictactoe.model.Cell;
import com.example.tictactoe.util.GameState;

/**
 * Interface for the Tic-Tac-Toe game service.
 * This service manages the game state, player moves, and synchronization between instances.
 */
public interface TicTacToeService {

    /**
     * Resets the game to its initial state.
     */
    void resetGame();

    /**
     * Makes a move on the game board.
     *
     * @param cell The cell where the move is to be made.
     * @throws IllegalStateException if the game is over or it's not the player's turn.
     * @throws IllegalArgumentException if the cell is already occupied.
     */
    void makeMove(Cell cell);

    /**
     * Synchronizes the game state with another instance.
     * This method should be called periodically to ensure consistency between instances.
     */
    void syncState();


    /**
     * Updates the current game state with a new state.
     *
     * @param newState The new game state to update to.
     */
    void updateGameState(GameState newState);

    /**
     * Retrieves the current game state.
     *
     * @return A copy of the current game state.
     */
    GameState getGameState();
}
