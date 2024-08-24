package com.example.tictactoe.service.impl;

import com.example.tictactoe.model.Cell;
import com.example.tictactoe.service.TicTacToeService;
import com.example.tictactoe.util.GameState;
import com.example.tictactoe.model.Player;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicReference;

import static com.example.tictactoe.constant.GameConstants.*;

/**
 * Implementation of the TicTacToeService interface.
 * This service manages the game state and synchronization for a Tic-Tac-Toe game.
 */
@Service
@Getter
public class TicTacToeServiceImpl implements TicTacToeService {
    private static final Logger logger = LoggerFactory.getLogger(TicTacToeServiceImpl.class);

    @Value("${other.instance.port}")
    private int otherInstancePort;

    @Value("${server.port}")
    private int currentPort;

    private Player assignedPlayer;
    private final AtomicReference<GameState> gameState = new AtomicReference<>();
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Initializes the service after construction.
     * Assigns the player based on the current port and resets the game.
     */
    @PostConstruct
    public void init() {
        assignedPlayer = (currentPort == 8082) ? Player.X : Player.O;
        resetGame();
        logger.info(INIT_MESSAGE, currentPort, assignedPlayer);
    }

    /**
     * Resets the game state to its initial condition.
     */
    public void resetGame() {
        gameState.set(new GameState(Player.X));
        logger.info(GAME_INIT_MESSAGE, gameState.get().getCurrentPlayer());
    }

    /**
     * Makes a move in the game.
     *
     * @param cell The cell where the move is to be made
     * @throws IllegalStateException if it's not the player's turn
     */
    public synchronized void makeMove(Cell cell) {
        GameState currentState = gameState.get();
        if (currentState.getCurrentPlayer() != assignedPlayer) {
            logger.warn(MOVE_OUT_OF_TURN_MESSAGE, currentState.getCurrentPlayer(), assignedPlayer);
            throw new IllegalStateException(NOT_YOUR_TURN_MESSAGE);
        }

        currentState.makeMove(cell);
        logger.info(MOVE_MADE_MESSAGE, cell, assignedPlayer, currentState.getCurrentPlayer());
        syncState();
    }


    /**
     * Synchronizes the game state with the other instance.
     * This method is scheduled to run at fixed intervals.
     */
    @Scheduled(fixedRateString = "${sync.interval.milliseconds:5000}")
    public void syncState() {
        final String otherInstanceUrl = String.format(OTHER_INSTANCE_URL_FORMAT, otherInstancePort);
        try {
            ResponseEntity<GameState> response = restTemplate.getForEntity(otherInstanceUrl, GameState.class);
            if (response.getBody() != null) {
                handleStateSynchronization(response.getBody(), otherInstanceUrl);
            }
        } catch (RestClientException e) {
            logger.error(SYNC_ERROR_MESSAGE, e);
        }
    }

    /**
     * Handles the synchronization of game states between instances.
     *
     * @param otherState       The game state from the other instance
     * @param otherInstanceUrl The URL of the other instance
     */
    private void handleStateSynchronization(GameState otherState, String otherInstanceUrl) {
        gameState.updateAndGet(currentState -> {
            if (otherState.getVersion() > currentState.getVersion()) {
                logger.info(NEWER_STATE_RECEIVED_MESSAGE);
                return new GameState(otherState);
            } else if (otherState.getVersion() < currentState.getVersion()) {
                logger.info(LOCAL_STATE_NEWER_MESSAGE);
                restTemplate.postForEntity(otherInstanceUrl, currentState, Void.class);
            } else if (!otherState.equals(currentState)) {
                logger.warn(INCONSISTENT_STATE_MESSAGE);
            }
            return currentState;
        });
    }

    /**
     * Updates the game state with a new state.
     *
     * @param newState The new game state to update to
     */
    public void updateGameState(GameState newState) {
        gameState.updateAndGet(currentState ->
            newState.getVersion() > currentState.getVersion() ? new GameState(newState) : currentState
        );
    }

    /**
     * Gets the current game state.
     *
     * @return A copy of the current game state
     */
    public GameState getGameState() {
        return new GameState(gameState.get());
    }
}
