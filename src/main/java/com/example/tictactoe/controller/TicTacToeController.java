package com.example.tictactoe.controller;

import com.example.tictactoe.model.Cell;
import com.example.tictactoe.service.TicTacToeService;
import com.example.tictactoe.util.GameState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
@Validated
public class TicTacToeController {
    private static final Logger logger = LoggerFactory.getLogger(TicTacToeController.class);
    private final TicTacToeService ticTacToeService;

    @Autowired
    public TicTacToeController(TicTacToeService ticTacToeService) {
        this.ticTacToeService = ticTacToeService;
    }

    /**
     * Endpoint to make a move in the game.
     *
     * @param cell The cell where the move is to be made
     * @return ResponseEntity with the result of the move
     */
    @PostMapping("/move")
    @Operation(summary = "Make a move", description = "Make a move in the Tic-Tac-Toe game")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Move successful"),
            @ApiResponse(responseCode = "400", description = "Invalid move"),
            @ApiResponse(responseCode = "409", description = "Illegal move"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<String> makeMove(@RequestParam @NotNull Cell cell) {
        logger.info("Received move request for cell: {}", cell);

        try {
            ticTacToeService.makeMove(cell);
            GameState currentState = ticTacToeService.getGameState();

            if (currentState.isGameOver()) {
                if (currentState.getWinner() != null) {
                    logger.info("Game over. Player {} wins.", currentState.getWinner());
                    return ResponseEntity.ok("Move successful. Player " + currentState.getWinner() + " wins!");
                } else {
                    logger.info("Game over. It's a draw.");
                    return ResponseEntity.ok("Move successful. The game is a draw!");
                }
            }

            logger.info("Move successful for cell: {}", cell);
            return ResponseEntity.ok("Move successful");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid move attempt: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Illegal move attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during move", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    /**
     * Endpoint to reset the game.
     *
     * @return ResponseEntity confirming the game reset
     */
    @PostMapping("/reset")
    @Operation(summary = "Reset the game", description = "Reset the Tic-Tac-Toe game to its initial state")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Game reset successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to reset the game")
        }
    )
    public ResponseEntity<String> resetGame() {
        logger.info("Received request to reset the game");
        try {
            ticTacToeService.resetGame();
            logger.info("Game has been reset successfully");
            return ResponseEntity.ok("Game has been reset");
        } catch (Exception e) {
            logger.error("Error occurred while resetting the game", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset the game");
        }
    }

    /**
     * Endpoint to get the current game state.
     *
     * @return ResponseEntity with the current GameState
     */
    @GetMapping("/state")
    @Operation(summary = "Get game state", description = "Retrieve the current state of the Tic-Tac-Toe game")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200", description = "Successfully retrieved game state",
                content = @Content(schema = @Schema(implementation = GameState.class))
            ),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve game state")
        }
    )
    public ResponseEntity<GameState> getGameState() {
        logger.info("Received request to get game state");
        try {
            GameState state = ticTacToeService.getGameState();
            logger.info("Retrieved game state successfully");
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving game state", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint to update the game state.
     *
     * @param newState The new GameState to set
     * @return ResponseEntity confirming the state update
     */
    @PostMapping("/state")
    @Operation(summary = "Update game state", description = "Update the state of the Tic-Tac-Toe game")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Game state updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid game state"),
            @ApiResponse(responseCode = "500", description = "Failed to update game state")
        }
    )
    public ResponseEntity<String> updateGameState(@RequestBody @Valid @NotNull GameState newState) {
        logger.info("Received request to update game state");
        try {
            ticTacToeService.updateGameState(newState);
            logger.info("Game state updated successfully");
            return ResponseEntity.ok("Game state updated successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid game state update attempt: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error occurred while updating game state", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update game state");
        }
    }
}
