package com.example.tictactoe.controller;

import com.example.tictactoe.model.Cell;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.service.TicTacToeService;
import com.example.tictactoe.util.GameState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicTacToeController.class)
class TicTacToeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicTacToeService ticTacToeService;

    @Autowired
    private ObjectMapper objectMapper;

    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
        gameState.setCurrentPlayer(Player.X);
    }

    @Test
    void makeMove_ValidMove_ReturnsOk() throws Exception {
        when(ticTacToeService.getGameState()).thenReturn(gameState);

        mockMvc.perform(post("/api/game/move")
                .param("cell", "TOP_LEFT"))
            .andExpect(status().isOk())
            .andExpect(content().string("Move successful"));

        verify(ticTacToeService).makeMove(Cell.TOP_LEFT);
    }

    @Test
    void makeMove_WinningMove_ReturnsWinner() throws Exception {
        gameState.setGameOver(true);
        gameState.setWinner(Player.X);
        when(ticTacToeService.getGameState()).thenReturn(gameState);

        mockMvc.perform(post("/api/game/move")
                .param("cell", "TOP_LEFT"))
            .andExpect(status().isOk())
            .andExpect(content().string("Move successful. Player X wins!"));

        verify(ticTacToeService).makeMove(Cell.TOP_LEFT);
    }

    @Test
    void makeMove_DrawGame_ReturnsDraw() throws Exception {
        gameState.setGameOver(true);
        gameState.setWinner(null);
        when(ticTacToeService.getGameState()).thenReturn(gameState);

        mockMvc.perform(post("/api/game/move")
                .param("cell", "TOP_LEFT"))
            .andExpect(status().isOk())
            .andExpect(content().string("Move successful. The game is a draw!"));

        verify(ticTacToeService).makeMove(Cell.TOP_LEFT);
    }

    @Test
    void makeMove_InvalidMove_ReturnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Invalid move")).when(ticTacToeService).makeMove(any(Cell.class));

        mockMvc.perform(post("/api/game/move")
                .param("cell", "TOP_LEFT"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid move"));
    }

    @Test
    void resetGame_ReturnsOk() throws Exception {
        mockMvc.perform(post("/api/game/reset"))
            .andExpect(status().isOk())
            .andExpect(content().string("Game has been reset"));

        verify(ticTacToeService).resetGame();
    }

    @Test
    void getGameState_ReturnsCurrentState() throws Exception {
        when(ticTacToeService.getGameState()).thenReturn(gameState);

        mockMvc.perform(get("/api/game/state"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(gameState)));
    }

    @Test
    void updateGameState_ValidState_ReturnsOk() throws Exception {
        mockMvc.perform(post("/api/game/state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameState)))
            .andExpect(status().isOk())
            .andExpect(content().string("Game state updated successfully"));

        verify(ticTacToeService).updateGameState(any(GameState.class));
    }

    @Test
    void updateGameState_InvalidState_ReturnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Invalid state")).when(ticTacToeService).updateGameState(any(GameState.class));

        mockMvc.perform(post("/api/game/state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameState)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Invalid state"));
    }
}
