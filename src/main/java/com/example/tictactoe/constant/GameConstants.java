package com.example.tictactoe.constant;

public final class GameConstants {
    private GameConstants() {
        // Private constructor to prevent instantiation
    }

    public static final String INIT_MESSAGE = "Initialized TicTacToeServiceImpl on port {} with assigned player {}";
    public static final String GAME_INIT_MESSAGE = "Game initialized with currentPlayer: {}";
    public static final String MOVE_OUT_OF_TURN_MESSAGE = "Attempted move out of turn. Current player: {}, Assigned player: {}";
    public static final String NOT_YOUR_TURN_MESSAGE = "It's not your turn.";
    public static final String MOVE_MADE_MESSAGE = "Move made at {} by {}. New current player: {}";
    public static final String SYNC_ERROR_MESSAGE = "Error during state synchronization";
    public static final String NEWER_STATE_RECEIVED_MESSAGE = "Received newer state from other instance. Updating local state.";
    public static final String LOCAL_STATE_NEWER_MESSAGE = "Local state is newer. Sending update to other instance.";
    public static final String INCONSISTENT_STATE_MESSAGE = "Inconsistent state detected.";
    public static final String OTHER_INSTANCE_URL_FORMAT = "http://localhost:%d/api/game/state";
}
