package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

// Basically taken from "Socket Programming" in content.

public class GameServer {

    private static final int PORT = 1024;
    private List<GameThread> activeGames;
    private int gameCounter = 1;

    public GameServer() {
        activeGames = new ArrayList<>();
    }
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Game server started on port " + PORT);

            while (true) {
                Socket player1Socket = serverSocket.accept();
                PrintWriter player1Out = new PrintWriter(player1Socket.getOutputStream(), true);
                System.out.println("Game " + gameCounter + ": Player 1 connected.");
                player1Out.println("Welcome Player 1.");
                player1Out.println("Waiting for Player 2...");

                Socket player2Socket = serverSocket.accept();
                PrintWriter player2Out = new PrintWriter(player2Socket.getOutputStream(), true);
                System.out.println("Game " + gameCounter + ": Player 2 connected.");
                player1Out.println("Player 2 joined."); // Alert player 1 when player 2 joins
                player2Out.println("Welcome Player 2.");

                GameThread gameThread = new GameThread(player1Socket, player2Socket, gameCounter);
                activeGames.add(gameThread);
                gameThread.start();

                System.out.println("Game " + gameCounter + ": started.");
                gameCounter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}
