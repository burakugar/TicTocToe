package com.example.tictactoe.service;

import com.example.tictactoe.model.Player;
import com.example.tictactoe.service.impl.TicTacToeServiceImpl;
import com.example.tictactoe.util.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


class TicTacToeServiceTest {

    @InjectMocks
    private TicTacToeServiceImpl ticTacToeService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(ticTacToeService, "currentPort", 8082);
        ReflectionTestUtils.setField(ticTacToeService, "otherInstancePort", 8083);
        ticTacToeService.init();
    }

    @Test
    void init_ShouldAssignPlayerAndResetGame() {
        assertEquals(Player.X, ticTacToeService.getAssignedPlayer());
        assertNotNull(ticTacToeService.getGameState());
        assertEquals(Player.X, ticTacToeService.getGameState().getCurrentPlayer());
    }

    @Test
    void syncState_NewerStateReceived_ShouldUpdateLocalState() {
        GameState newerState = new GameState(Player.O);
        newerState.setVersion(2);
        when(restTemplate.getForEntity(anyString(), eq(GameState.class)))
            .thenReturn(ResponseEntity.ok(newerState));

        ticTacToeService.syncState();

        assertEquals(newerState.getVersion(), ticTacToeService.getGameState().getVersion());
        assertEquals(newerState.getCurrentPlayer(), ticTacToeService.getGameState().getCurrentPlayer());
    }


    @Test
    void updateGameState_NewerState_ShouldUpdateLocalState() {
        GameState newerState = new GameState(Player.O);
        newerState.setVersion(2);

        ticTacToeService.updateGameState(newerState);

        assertEquals(newerState.getVersion(), ticTacToeService.getGameState().getVersion());
        assertEquals(newerState.getCurrentPlayer(), ticTacToeService.getGameState().getCurrentPlayer());
    }

    @Test
    void getGameState_ShouldReturnCopyOfState() {
        GameState originalState = ticTacToeService.getGameState();
        GameState returnedState = ticTacToeService.getGameState();

        assertNotSame(originalState, returnedState);
        assertEquals(originalState, returnedState);
    }
}
