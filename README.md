# Mancala Game - COSC 2P13 Assignment 2
This project is a Java implementation of a networked two-player Mancala game, designed to fulfill the requirements of the COSC 2P13 Assignment 2. The game supports both terminal-based and GUI-based clients, allowing for flexible gameplay options.

## Key Features:

- **Networked Gameplay:** Players can connect to a central server and engage in Mancala matches over a network.
- **Multiple Concurrent Games:** The server is capable of hosting multiple games simultaneously, allowing for a scalable gaming experience.
- **Terminal and GUI Clients:** Players have the choice to play the game either through a simple terminal interface or a more visually appealing graphical user interface (GUI).
- **Thread Management:** The server utilizes threads to manage individual game instances, ensuring smooth and responsive gameplay.
- **Error Handling:** The server includes error-checking mechanisms to handle invalid moves and maintain game integrity.
  
## Project Structure:

- **Server:** The core game logic, network communication, and thread management are implemented on the server-side.
- **Client (Terminal):** A text-based client that interacts with the server via a terminal interface.
- **Client (GUI):** A graphical client that provides a visual representation of the game board and allows for interactive gameplay.

## How to Play:

1. **Run the Server:** Start `Server/GameServer.java` to initiate the game environment.
2. **Connect Clients:** Either run `nc localhost 1024` or `Client/Client.java` and connect to the server.
3. **Matchmaking:** The server will automatically pair up players as they connect.
4.  **Gameplay:** Follow the standard Mancala rules to play the game. The interface will guide you through your turns.
