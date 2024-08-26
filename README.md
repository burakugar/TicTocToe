# Distributed Tic-Tac-Toe Game Implementation

## 1. Implementation Overview

This application implements a distributed Tic-Tac-Toe game as a RESTful web service using Spring Boot. It allows two
instances of the application to play against each other, running on different ports.

### Key Components:

- `TicTacToeController`: Handles HTTP requests for game actions.
- `TicTacToeService`: Manages game logic and state.
- `GameState`: Represents the current state of the game.
- `Player` and `Cell` enums: Represent players and board positions.

## 2. Game Flow

### a. Game Initialization:

- Two instances of the application are started on different ports.
- Each instance is assigned a player (X or O) based on its port.

### b. Making Moves:

- Players take turns making moves by sending POST requests to `/api/game/move`.
- The service validates the move and updates the game state.

### c. Synchronization:

- After each move, the instances synchronize their game states.
- A scheduled task (`syncState`) runs periodically to ensure consistency.

### d. Game End:

- The game ends when a player wins or it's a draw.
- The final state is synchronized between instances.

## 3. Features

- RESTful API for game actions (move, reset, get state)
- Automatic player assignment (X or O)
- Move validation and turn management
- Win/draw detection
- State synchronization between instances
- Conflict resolution for inconsistent states

## 4. Technologies Used

- Java 21
- Spring Boot 3.2.4
- Spring Web (for RESTful API)
- Spring Validation
- Lombok (for reducing boilerplate code)
- JUnit 5 (for testing)
- Gradle (for build management)

## 5. How to Run the Application

### a. Build the application:

```bash
./gradlew build
```

### b. Run two instances of the application:

```bash
./gradlew bootRun --args='--server.port=8081 --other.instance.port=8080 --player.assignment=X'
./gradlew bootRun --args='--server.port=8080 --other.instance.port=8081 --player.assignment=O'

```

Documentation(Swagger) can be accessed at http://localhost:8080/swagger-ui/index.html and
http://localhost:8081/swagger-ui/index.html

## 6. How to Run Tests

Run the tests using Gradle:

```bash
./gradlew test
```

## 7. Game Implementation

- The game board is represented by an `EnumMap<Cell, Player>` in the `GameState` class.
- Moves are validated to ensure they're legal and in turn.
- After each move, the game checks for a win or draw condition.
- The game state is versioned to help with synchronization.

## 8. Models

- `Player`: Enum representing X, O, or EMPTY.
- `Cell`: Enum representing the 9 positions on the board.
- `GameState`: Class representing the current game state, including board, current player, game status, and version.

## 9. Additional Details

- The application uses a `RestTemplate` for communication between instances.
- Error handling is implemented using `@ControllerAdvice`.
- The application is configured to allow CORS (Cross-Origin Resource Sharing).
- Logging is implemented throughout the application for debugging and monitoring.
-

## 10. Controller Usage and Response Statuses

The `TicTacToeController` provides several endpoints for interacting with the Tic-Tac-Toe game. Here's a breakdown of
each endpoint, its usage, and expected responses:

### 1. Make a Move

`POST /api/game/move`

**Usage:**

```bash
POST /api/game/move?cell=TOP_LEFT
```

**Response Statuses:**

- 200 OK: Move successful
    - Body: "Move successful" (if game continues)
    - Body: "Move successful. Player X/O wins!" (if there's a winner)
    - Body: "Move successful. The game is a draw!" (if it's a draw)
- 400 Bad Request: Invalid move (e.g., cell already occupied)
    - Body: Error message explaining the invalid move
- 409 Conflict: Illegal move (e.g., not player's turn, game already over)
    - Body: Error message explaining the illegal move
- 500 Internal Server Error: Unexpected error
    - Body: "An unexpected error occurred"

### 2. Reset Game

**Endpoint:** `POST /api/game/reset`

**Usage:**

```bash
`POST /api/game/reset`
```

**Response Statuses:**

- 200 OK: Game reset successful
    - Body: "Game has been reset"
- 500 Internal Server Error: Failed to reset the game
    - Body: "Failed to reset the game"

### 3. Get Game State

**Endpoint:** `GET /api/game/state`

**Usage:**

```bash
GET /api/game/state
```

**Response Statuses:**

- 200 OK: Successfully retrieved game state
    - Body: JSON representation of the current GameState
- 500 Internal Server Error: Failed to retrieve game state
    - Body: None

### 4. Update Game State

**Endpoint:** `POST /api/game/state`

**Usage:**

```bash
POST /api/game/state
Content-Type: application/json

{
"board": {...},
"currentPlayer": "X",
"gameOver": false,
"winner": null,
"version": 5
}
```

**Response Statuses:**

- 200 OK: Game state updated successfully
    - Body: "Game state updated successfully"
- 400 Bad Request: Invalid game state provided
    - Body: Error message explaining the invalid state
- 500 Internal Server Error: Failed to update game state
    - Body: "Failed to update game state"

### Notes:

- All endpoints log their actions and any errors that occur.
- The `/move` endpoint handles various game scenarios, including win and draw conditions.
- Input validation is performed using Spring's validation annotations (`@Valid`, `@NotNull`).
- Exception handling is implemented to provide appropriate HTTP status codes and error messages.


