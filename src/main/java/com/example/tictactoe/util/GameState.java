package com.example.tictactoe.util;

import com.example.tictactoe.model.Cell;
import com.example.tictactoe.model.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the state of a Tic-Tac-Toe game.
 * This class encapsulates all the information about the current game state,
 * including the board, current player, game status, and version for synchronization.
 * It also includes the game logic for making moves and checking game status.
 */
@Getter
@Setter
public class GameState {

    private Map<Cell, Player> board;
    private Player currentPlayer;
    private Player lastPlayer;
    private boolean gameOver;
    private Player winner;
    private long version;

    /*
     * Creates a new game state with the specified starting player.¬
     */
    public GameState() {
        this(Player.X);
    }

    public GameState(Player startingPlayer) {
        this.board = new EnumMap<>(Cell.class);
        this.currentPlayer = startingPlayer;
        this.gameOver = false;
        this.winner = Player.EMPTY;
        initializeBoard();
    }

    public GameState(GameState other) {
        this.board = new EnumMap<>(Cell.class);
        this.board.putAll(other.board);
        this.currentPlayer = other.currentPlayer;
        this.lastPlayer = other.lastPlayer;
        this.gameOver = other.gameOver;
        this.winner = other.winner;
        this.version = other.version;
    }

    private void initializeBoard() {
        for (Cell cell : Cell.values()) {
            board.put(cell, Player.EMPTY);
        }
    }

    public void incrementVersion() {
        this.version++;
    }

    /**
     * Makes a move on the specified cell for the current player.
     *
     * @param cell The cell where the move is to be made
     * @throws IllegalStateException    if the game is over
     * @throws IllegalArgumentException if the cell is already occupied
     */
    public void makeMove(Cell cell) {
        if (gameOver) {
            throw new IllegalStateException("Game is already over.");
        }
        if (board.get(cell) != Player.EMPTY) {
            throw new IllegalArgumentException("Cell is already occupied.");
        }

        board.put(cell, currentPlayer);
        lastPlayer = currentPlayer;
        currentPlayer = (currentPlayer == Player.X) ? Player.O : Player.X;
        checkGameStatus();
        incrementVersion();
    }

    /**
     * Checks the game status after a move to determine if the game has ended.
     */
    private void checkGameStatus() {
        if (checkLine(Cell.TOP_LEFT, Cell.TOP_CENTER, Cell.TOP_RIGHT) ||
            checkLine(Cell.MIDDLE_LEFT, Cell.MIDDLE_CENTER, Cell.MIDDLE_RIGHT) ||
            checkLine(Cell.BOTTOM_LEFT, Cell.BOTTOM_CENTER, Cell.BOTTOM_RIGHT) ||
            checkLine(Cell.TOP_LEFT, Cell.MIDDLE_LEFT, Cell.BOTTOM_LEFT) ||
            checkLine(Cell.TOP_CENTER, Cell.MIDDLE_CENTER, Cell.BOTTOM_CENTER) ||
            checkLine(Cell.TOP_RIGHT, Cell.MIDDLE_RIGHT, Cell.BOTTOM_RIGHT) ||
            checkLine(Cell.TOP_LEFT, Cell.MIDDLE_CENTER, Cell.BOTTOM_RIGHT) ||
            checkLine(Cell.TOP_RIGHT, Cell.MIDDLE_CENTER, Cell.BOTTOM_LEFT)) {
            gameOver = true;
            winner = lastPlayer;
        } else if (isBoardFull()) {
            gameOver = true;
            winner = Player.EMPTY;
        }
    }

    private boolean checkLine(Cell cell1, Cell cell2, Cell cell3) {
        return board.get(cell1) != Player.EMPTY &&
            board.get(cell1) == board.get(cell2) &&
            board.get(cell2) == board.get(cell3);
    }

    private boolean isBoardFull() {
        return !board.containsValue(Player.EMPTY);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Player: ").append(currentPlayer).append("\n");
        sb.append("Game Over: ").append(gameOver).append("\n");
        sb.append("Game Board:\n");
        sb.append(formatRow(Cell.TOP_LEFT, Cell.TOP_CENTER, Cell.TOP_RIGHT)).append("\n");
        sb.append(formatRow(Cell.MIDDLE_LEFT, Cell.MIDDLE_CENTER, Cell.MIDDLE_RIGHT)).append("\n");
        sb.append(formatRow(Cell.BOTTOM_LEFT, Cell.BOTTOM_CENTER, Cell.BOTTOM_RIGHT)).append("\n");
        if (gameOver) {
            sb.append("Winner: ").append(winner).append("\n");
        }
        return sb.toString();
    }

    private String formatRow(Cell cell1, Cell cell2, Cell cell3) {
        return String.format("%s | %s | %s",
            playerToString(board.get(cell1)),
            playerToString(board.get(cell2)),
            playerToString(board.get(cell3)));
    }

    private String playerToString(Player player) {
        return player == Player.EMPTY ? " " : player.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameState gameState = (GameState) o;
        return gameOver == gameState.gameOver &&
            version == gameState.version &&
            Objects.equals(board, gameState.board) &&
            currentPlayer == gameState.currentPlayer &&
            lastPlayer == gameState.lastPlayer &&
            winner == gameState.winner;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentPlayer, lastPlayer, gameOver, winner, version);
    }
}
