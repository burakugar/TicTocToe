package com.example.tictactoe;

import com.example.tictactoe.model.Cell;
import com.example.tictactoe.util.GameState;
import com.example.tictactoe.model.Player;
import com.example.tictactoe.constant.GameConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class TicTacToeIntegrationTest {

    private static final String LOCALHOST = "http://localhost:";
    private static final String MOVE_ENDPOINT = "/api/game/move?cell=";
    private static final String RESET_ENDPOINT = "/api/game/reset";
    private static final String STATE_ENDPOINT = "/api/game/state";
    private static final String GAME_OVER_MESSAGE = "Game is already over";
    private static final String CELL_OCCUPIED_MESSAGE = "Cell is already occupied";

    private ConfigurableApplicationContext contextX;
    private ConfigurableApplicationContext contextO;
    private final TestRestTemplate playerXTemplate = new TestRestTemplate();
    private final TestRestTemplate playerOTemplate = new TestRestTemplate();
    private int portX;
    private int portO;

    @BeforeEach
    public void setUp() throws InterruptedException {
        contextX = new SpringApplicationBuilder(TikTakToeApplication.class)
            .properties("server.port=8082", "other.instance.port=8080", "player.assignment=X")
            .run();
        contextO = new SpringApplicationBuilder(TikTakToeApplication.class)
            .properties("server.port=8080", "other.instance.port=8082", "player.assignment=O")
            .run();

        portX = contextX.getEnvironment().getProperty("local.server.port", Integer.class, 0);
        portO = contextO.getEnvironment().getProperty("local.server.port", Integer.class, 0);

        System.out.println("Player X port: " + portX);
        System.out.println("Player O port: " + portO);
        Thread.sleep(100);
    }

    @AfterEach
    public void tearDown() {
        if (contextX != null) {
            contextX.close();
        }
        if (contextO != null) {
            contextO.close();
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testGameScenario() throws InterruptedException {
        resetGame();
        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_CENTER);
        makeMove(playerXTemplate, portX, Cell.TOP_CENTER);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_LEFT);
        makeMove(playerXTemplate, portX, Cell.TOP_RIGHT);

        final GameState gameState = getGameState(portX);
        assertTrue(gameState.isGameOver());
        assertEquals(Player.X, gameState.getWinner());

        assertEquals(Player.X, gameState.getBoard().get(Cell.TOP_LEFT));
        assertEquals(Player.X, gameState.getBoard().get(Cell.TOP_CENTER));
        assertEquals(Player.X, gameState.getBoard().get(Cell.TOP_RIGHT));
        assertEquals(Player.O, gameState.getBoard().get(Cell.MIDDLE_LEFT));
        assertEquals(Player.O, gameState.getBoard().get(Cell.MIDDLE_CENTER));
    }

    @Test
    void testGameOverScenario() throws InterruptedException {
        resetGame();
        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_CENTER);
        makeMove(playerXTemplate, portX, Cell.TOP_CENTER);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_LEFT);
        makeMove(playerXTemplate, portX, Cell.TOP_RIGHT);

        final ResponseEntity<String> gameOverResponse = playerOTemplate.postForEntity(
            createURLWithPort(portO, MOVE_ENDPOINT + Cell.BOTTOM_LEFT),
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, gameOverResponse.getStatusCode());
        assertTrue(gameOverResponse.getBody().contains(GAME_OVER_MESSAGE));
    }

    @Test
    void testIllegalMoves() throws InterruptedException {
        resetGame();

        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);

        final ResponseEntity<String> illegalMoveResponse = playerOTemplate.postForEntity(
            createURLWithPort(portO, MOVE_ENDPOINT + Cell.TOP_LEFT),
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, illegalMoveResponse.getStatusCode());
        assertTrue(illegalMoveResponse.getBody().contains(CELL_OCCUPIED_MESSAGE));

        final ResponseEntity<String> outOfTurnMove = playerXTemplate.postForEntity(
            createURLWithPort(portX, MOVE_ENDPOINT + Cell.MIDDLE_CENTER),
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, outOfTurnMove.getStatusCode());
        assertTrue(outOfTurnMove.getBody().contains(GameConstants.NOT_YOUR_TURN_MESSAGE));
    }

    @Test
    void testResetGame() throws InterruptedException {
        System.out.println("Starting testResetGame");

        resetGame();
        Thread.sleep(500);

        GameState initialStateX = getGameState(portX);
        GameState initialStateO = getGameState(portO);
        System.out.println("Initial state X: " + initialStateX);
        System.out.println("Initial state O: " + initialStateO);

        ResponseEntity<String> moveX = makeMove(playerXTemplate, portX, Cell.TOP_LEFT);
        System.out.println("Player X move response: " + moveX.getStatusCode() + " - " + moveX.getBody());

        Thread.sleep(500);

        GameState stateAfterX_X = getGameState(portX);
        GameState stateAfterX_O = getGameState(portO);
        System.out.println("State after X's move (X instance): " + stateAfterX_X);
        System.out.println("State after X's move (O instance): " + stateAfterX_O);

        ResponseEntity<String> moveO = makeMove(playerOTemplate, portO, Cell.MIDDLE_CENTER);
        System.out.println("Player O move response: " + moveO.getStatusCode() + " - " + moveO.getBody());

        Thread.sleep(500);

        GameState stateAfterO_X = getGameState(portX);
        GameState stateAfterO_O = getGameState(portO);
        System.out.println("State after O's move (X instance): " + stateAfterO_X);
        System.out.println("State after O's move (O instance): " + stateAfterO_O);

        assertEquals(HttpStatus.OK, moveX.getStatusCode(), "X's move should be successful");
        assertEquals(HttpStatus.OK, moveO.getStatusCode(), "O's move should be successful");
        assertEquals(stateAfterO_X, stateAfterO_O, "Game states should be equal after O's move");
        assertEquals(Player.X, stateAfterO_X.getCurrentPlayer(), "It should be X's turn after O's move");
    }

    @Test
    void testDrawGame() throws InterruptedException {
        resetGame();
        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);
        makeMove(playerOTemplate, portO, Cell.TOP_CENTER);
        makeMove(playerXTemplate, portX, Cell.TOP_RIGHT);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_LEFT);
        makeMove(playerXTemplate, portX, Cell.MIDDLE_RIGHT);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_CENTER);
        makeMove(playerXTemplate, portX, Cell.BOTTOM_LEFT);
        makeMove(playerOTemplate, portO, Cell.BOTTOM_RIGHT);
        makeMove(playerXTemplate, portX, Cell.BOTTOM_CENTER);

        GameState gameState = getGameState(portX);
        assertTrue(gameState.isGameOver());
    }

    @Test
    void testDiagonalWin() throws InterruptedException {
        resetGame();
        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);
        makeMove(playerOTemplate, portO, Cell.TOP_CENTER);
        makeMove(playerXTemplate, portX, Cell.MIDDLE_CENTER);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_LEFT);
        makeMove(playerXTemplate, portX, Cell.BOTTOM_RIGHT);

        GameState gameState = getGameState(portX);
        assertTrue(gameState.isGameOver());
        assertEquals(Player.X, gameState.getWinner());
    }

    @Test
    void testMoveAfterGameOver() throws InterruptedException {
        resetGame();
        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_CENTER);
        makeMove(playerXTemplate, portX, Cell.TOP_CENTER);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_LEFT);
        makeMove(playerXTemplate, portX, Cell.TOP_RIGHT);

        ResponseEntity<String> response = makeMove(playerOTemplate, portO, Cell.BOTTOM_LEFT);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(GAME_OVER_MESSAGE));
    }

    @Test
    void testOutOfTurnMove() throws InterruptedException {
        resetGame();
        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);

        ResponseEntity<String> response = makeMove(playerXTemplate, portX, Cell.TOP_CENTER);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(GameConstants.NOT_YOUR_TURN_MESSAGE));
    }

    @Test
    void testResetGameDuringPlay() throws InterruptedException {
        resetGame();
        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_CENTER);

        resetGame();
        GameState gameState = getGameState(portX);
        assertFalse(gameState.isGameOver());
        assertEquals(Player.X, gameState.getCurrentPlayer());
    }

    @Test
    void testFullGamePlaythrough() throws InterruptedException {
        resetGame();
        makeMove(playerXTemplate, portX, Cell.TOP_LEFT);
        makeMove(playerOTemplate, portO, Cell.TOP_CENTER);
        makeMove(playerXTemplate, portX, Cell.MIDDLE_LEFT);
        makeMove(playerOTemplate, portO, Cell.MIDDLE_CENTER);
        makeMove(playerXTemplate, portX, Cell.BOTTOM_LEFT);

        GameState finalState = getGameState(portX);
        assertTrue(finalState.isGameOver());
        assertEquals(Player.X, finalState.getWinner());
        assertEquals(Player.X, finalState.getBoard().get(Cell.TOP_LEFT));
        assertEquals(Player.X, finalState.getBoard().get(Cell.MIDDLE_LEFT));
        assertEquals(Player.X, finalState.getBoard().get(Cell.BOTTOM_LEFT));
        assertEquals(Player.O, finalState.getBoard().get(Cell.TOP_CENTER));
        assertEquals(Player.O, finalState.getBoard().get(Cell.MIDDLE_CENTER));
    }

    private ResponseEntity<String> makeMove(final TestRestTemplate template, final int port, final Cell cell) {
        return template.postForEntity(
            createURLWithPort(port, MOVE_ENDPOINT + cell),
            null,
            String.class
        );
    }

    private void resetGame() throws InterruptedException {
        playerXTemplate.postForEntity(
            createURLWithPort(portX, RESET_ENDPOINT),
            null,
            String.class
        );
        Thread.sleep(500);
    }

    private GameState getGameState(final int port) {
        final ResponseEntity<GameState> response = playerXTemplate.getForEntity(
            createURLWithPort(port, STATE_ENDPOINT),
            GameState.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private String createURLWithPort(final int port, final String uri) {
        return LOCALHOST + port + uri;
    }
}
