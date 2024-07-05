package Server;

// Rules I based my game off:
// https://www.scholastic.com/content/dam/teachers/blogs/alycia-zimmerman/migrated-files/mancala_rules.pdf

import java.io.*;
import java.net.*;

public class GameThread extends Thread {

    private Socket player1Socket;
    private Socket player2Socket;
    private int gameNumber;
    private int[] board; // Mancala board representation

    public GameThread(Socket player1Socket, Socket player2Socket, int gameNumber) {
        this.player1Socket = player1Socket;
        this.player2Socket = player2Socket;
        this.gameNumber = gameNumber;
        this.board = new int[]{4, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4, 4, 4, 0}; // Initial board state
    }
    
    /**
     * Run the game after initializing values
     */
    public void run() {
        try {
            // Get input/output streams for both players
            BufferedReader player1In = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
            PrintWriter player1Out = new PrintWriter(player1Socket.getOutputStream(), true);
            BufferedReader player2In = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
            PrintWriter player2Out = new PrintWriter(player2Socket.getOutputStream(), true);

            player1Out.println("Game Started.");
            player2Out.println("Game Started.");

            boolean player1Turn = true;

            // Main game loop
            while (true) {
                // Handle Player Turns
                if (player1Turn) {
                    player2Out.println("\n" + boardToString(2, false) + "\nOpponents turn!  Waiting for Player 1.");
                    while (true) {
                        player1Out.println("\n" + boardToString(1, true) + "\nYour turn! Type a number.");
                        player1Out.println("Possible Moves: " + getPossibleMoves(1) + ".");
                        String moveStr = player1In.readLine().trim();
                        int move = 0;
                        // Wrap the parseInt in a try-catch to ensure the user input is actually a number
                        try {
                            move = Integer.parseInt(moveStr);
                        } catch (Exception e) {
                            player1Out.println("\nError: Invalid move!  Please try again.");
                            continue;
                        }

                        if (isValidMove(move, 1)) {
                            player1Turn = makeMove(move, 1); // Make move & see if player gets another turn
                            break;
                        } else {
                            player1Out.println("\nError: Invalid move!  Please try again.");
                        }
                    }
                } else {
                    player1Out.println("\n" + boardToString(1, false) + "\nOpponents turn!  Waiting for Player 2.");
                    while (true) {
                        player2Out.println("\n" + boardToString(2, true) + "\nYour turn! Type a number.");
                        player2Out.println("Possible Moves: " + getPossibleMoves(2) + ".");
                        String moveStr = player2In.readLine().trim();
                        int move = 0;
                        // Wrap the parseInt in a try-catch to ensure the user input is actually a number
                        try {
                            move = Integer.parseInt(moveStr) + 7;
                        } catch (Exception e) {
                            player2Out.println("\nError: Invalid move!  Please try again.");
                            continue;
                        }
                        if (isValidMove(move, 2)) {
                            player1Turn = makeMove(move, 2); // Make move & see if player gets another turn
                            break;
                        } else {
                            player2Out.println("\nError: Invalid move!  Please try again.");
                        }
                    }
                }

                // Check for win conditions
                // Player 1
                boolean gameOver = true;
                for (int i = 0; i < 6; i++) {
                    if (board[i] > 0) {
                        gameOver = false;
                        break;
                    }
                }
                if (gameOver) {
                    handleGameOver(2);
                    break;
                }
                // Player 2
                gameOver = true;
                for (int i = 7; i < 13; i++) {
                    if (board[i] > 0) {
                        gameOver = false;
                        break;
                    }
                }
                if (gameOver) {
                    handleGameOver(1);
                    break;
                }
            }

            // Show both players the final outcome before announcing the winner
            player1Out.println(boardToString(1, true));
            player2Out.println(boardToString(2, false));

            // Determine the winner, then alert both players who won.
            int winner = determineWinner();
            if (winner == 0) {
                player1Out.println("\nTie!\nPlayer 1: " + board[6] + "\nPlayer 2: " + board[13]);
                player2Out.println("\nTie!\nPlayer 1: " + board[6] + "\nPlayer 2: " + board[13]);
            } else if (winner == 1) {
                player1Out.println("\nPlayer 1 Wins!\nPlayer 1: " + board[6] + "\nPlayer 2: " + board[13]);
                player2Out.println("\nPlayer 1 Wins!\nPlayer 1: " + board[6] + "\nPlayer 2: " + board[13]);
            } else {
                player1Out.println("\nPlayer 2 Wins!\nPlayer 1: " + board[6] + "\nPlayer 2: " + board[13]);
                player2Out.println("\nPlayer 2 Wins!\nPlayer 1: " + board[6] + "\nPlayer 2: " + board[13]);
            }
            
            // Announce that the thread is stopping, wait 10 seconds, then stop.
            player1Out.println("Stopping thread in 10 seconds.");
            player2Out.println("Stopping thread in 10 seconds.");
            Thread.sleep(10000);
            player1Out.println("Stopping thread now.");
            player2Out.println("Stopping thread now.");

        } catch (IOException | InterruptedException e) {
            System.err.println("Error in game " + gameNumber + ": " + e);
        } finally {
            // Close player1Socket
            try {
                if (player1Socket != null && !player1Socket.isClosed()) {
                    player1Socket.shutdownInput();
                    player1Socket.shutdownOutput();
                    player1Socket.close();
                    System.out.println("Game " + gameNumber + ": Player 1 disconnected.");
                }
            } catch (IOException e) {
                System.err.println("Game " + gameNumber + ": Error closing player 1 socket: " + e);
            }

            // Close player2Socket
            try {
                if (player2Socket != null && !player2Socket.isClosed()) {
                    player2Socket.shutdownInput();
                    player2Socket.shutdownOutput();
                    player2Socket.close();
                    System.out.println("Game " + gameNumber + ": Player 2 disconnected.");
                }
            } catch (IOException e) {
                System.err.println("Game " + gameNumber + ": Error closing player 2 socket: " + e);
            }

            System.out.println("Game " + gameNumber + ": closed.");
        }
    }
    
    /**
     * Function that converts the `board` array to a visual version
     *
     * @param player
     * @param showInputs
     * @return String containing a nice visual of the board (eg.
     * ╔══╦══╦══╦══╦══╦══╦══╦══╗
     * ║ 0║ 4║ 4║ 4║ 4║ 4║ 4║←┐║
     * ║  ╠══╬══╬══╬══╬══╬══╣  ║
     * ║└→║ 4║ 4║ 4║ 4║ 4║ 4║ 0║
     * ╚══╩══╩══╩══╩══╩══╩══╩══╝
     * )
     */
    private String boardToString(int player, boolean showInputs) {
        // Convert board state to a string for sending
        String result = "";
        if (player == 1) {
            result = result + "╔══╦══╦══╦══╦══╦══╦══╦══╗";
            result = result + String.format("\n║%2d║%2d║%2d║%2d║%2d║%2d║%2d║←┐║", board[13], board[12], board[11], board[10], board[9], board[8], board[7]);
            result = result + "\n║  ╠══╬══╬══╬══╬══╬══╣  ║";
            result = result + String.format("\n║└→║%2d║%2d║%2d║%2d║%2d║%2d║%2d║", board[0], board[1], board[2], board[3], board[4], board[5], board[6]);
            result = result + "\n╚══╩══╩══╩══╩══╩══╩══╩══╝";
            if (showInputs) {
                result = result + "\n   [ 0| 1| 2| 3| 4| 5]";
            }
        } else if (player == 2) { // Flip the board so that it's from the perspective of player 2
            result = result + "╔══╦══╦══╦══╦══╦══╦══╦══╗";
            result = result + String.format("\n║%2d║%2d║%2d║%2d║%2d║%2d║%2d║←┐║", board[6], board[5], board[4], board[3], board[2], board[1], board[0]);
            result = result + "\n║  ╠══╬══╬══╬══╬══╬══╣  ║";
            result = result + String.format("\n║└→║%2d║%2d║%2d║%2d║%2d║%2d║%2d║", board[7], board[8], board[9], board[10], board[11], board[12], board[13]);
            result = result + "\n╚══╩══╩══╩══╩══╩══╩══╩══╝";
            if (showInputs) {
                result = result + "\n   [ 0| 1| 2| 3| 4| 5]";
            }
        }
        return result;
    }
    
    /**
     * Function that returns a string containing all the possible inputs
     *
     * @param player
     * @return String containing all possible options (eg. "0, 2, 3, 5")
     */
    private String getPossibleMoves(int player) {
        String result = "";

        if (player == 1) {
            for (int i = 0; i < 6; i++) {
                if (board[i] > 0) {
                    result = result + i + ", ";
                }
            }
            result = result.substring(0, result.length() - 2);
        } else if (player == 2) {
            for (int i = 7; i < 13; i++) {
                if (board[i] > 0) {
                    result = result + (i - 7) + ", ";
                }
            }
            result = result.substring(0, result.length() - 2);
        }

        return result;
    }
    
    /**
     * Function that returns if a function is a valid move
     *
     * @param pitIndex
     * @param player
     * @return true/false
     */
    private boolean isValidMove(int pitIndex, int player) {
        // Check if the move is valid according to Mancala rules
        // Basic
        System.out.println(pitIndex);
        System.out.println("before oob");
        if (pitIndex < 0 || pitIndex > 13) return false; // Out of bounds
        System.out.println("before empty");
        if (board[pitIndex] == 0) return false; // Empty pit
        // Player-Specific Validation
        System.out.println("before 1 on 2");
        if (player == 1 && (pitIndex >= 7 && pitIndex <= 12)) return false; // Player 1 cannot play on Player 2's side
        System.out.println("before 2 on 1");
        return player != 2 || (pitIndex < 1 || pitIndex > 5); // Player 2 cannot play on Player 1's side
    }
    
    /**
     * Function that will make the given move and return if the player gets another turn (after that move was made).
     *
     * @param pitIndex
     * @param player
     * @return true/false
     */
    private boolean makeMove(int pitIndex, int player) {
        // Update the board state based on the move
        int index = pitIndex;

        for (int i = 0; i < board[pitIndex]; i++) {
            index++;
            // Handle wrap around (add to stores)
            if (index == 6 && player == 2) {
                index++; // Skip store
            } else if (index == 13 && player == 1) {
                index = 0; // Skip store
            }
            if (index == 14) {
                index = 0;
            }
            board[index] = board[index] + 1;
        }
        board[pitIndex] = 0;

        // Logic to handle landing on an empty space (capture opposing seeds)
        int[] corresponding = new int[]{12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}; // Corresponding opposite index
        if (player == 1 && index <= 5 && board[index] == 1 && board[corresponding[index]] > 0) {
            board[6] = board[6] + board[index] + board[corresponding[index]];
            board[index] = 0;
            board[corresponding[index]] = 0;
        } else if (player == 2 && index >= 7 && index <= 12 && board[index] == 1 && board[corresponding[index]] > 0) {
            board[13] = board[13] + board[index] + board[corresponding[index]];
            board[index] = 0;
            board[corresponding[index]] = 0;
        }

        // Logic to get another turn
        if (player == 1 && index == 6) {
            return true;
        } else if (player == 2 && index == 13) {
            return false;
        } else {
            return player != 1;
        }
    }
    
    /**
     * Function that will add all remaining seeds to a given seed.
     * Called when there are no remaining seeds on 1 side.
     *
     * @param player
     */
    private void handleGameOver(int player) {
        if (player == 1) {
            for (int i = 0; i < 6; i++) {
                board[6] = board[6] + board[i];
                board[i] = 0;
            }
        } else if (player == 2) {
            for (int i = 7; i < 13; i++) {
                board[13] = board[13] + board[i];
                board[i] = 0;
            }
        }
    }
    
    /**
     * Function that determines who wins the game.
     *
     * @return 0(tie)/1/2
     */
    private int determineWinner() {
        int playerOneCount = board[6];
        int playerTwoCount = board[13];
        if (playerOneCount > playerTwoCount) {
            return 1;
        } else if (playerOneCount < playerTwoCount) {
            return 2;
        } else {
            return 0;
        }
    }
}
