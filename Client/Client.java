package Client;

// IMPORTANT: You must run GameServer.java before you run this program.  This program connects to the server started by
//            GameServer.java, and will not run without it.

import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class Client extends JFrame {
    private JLabel messageLabel; //The JLabel that displays information to the player about the game.
    private JButton[] board = new JButton[14];

    JFrame frame = new JFrame("Mancala");
    private JPanel left = new JPanel(new GridLayout(1,1));
    private JPanel right = new JPanel(new GridLayout(1,1));
    int playerNum = 0;

    public Client() {
        try (
                Socket socket = new Socket("localhost", 1024);
                PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ) {
            createGUI(socketOut);
            while (true) {
                String line = socketIn.readLine();
                // Determine if the user is player 1 or player 2
                // Used to know what inputs to send back
                if (playerNum == 0) {
                    if (line.endsWith("1.")) { // "Welcome Player 1."
                        playerNum = 1;
                    } else if (line.endsWith("2.")) { // "Welcome Player 2."
                        playerNum = 2;
                    }
                }
                if (line.startsWith("╔")) { // Board display
                    if (updateBoard(socketIn)) {
                        break;
                    }
                }
                if (line.contains("Wins") || line.contains("Tie")) { // "Player x Wins!" or "Tie!"
                    messageLabel.setText(line);
                    break;
                }
            }
            // Wait 10 seconds then close
            Thread.sleep(10000);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        } catch (ConnectException e) {
            System.out.println("ERROR: ../Server/GameServer.java must be running for this program to start.");
        } catch (Exception e) {
            System.out.println("something broke: " + e);
        }
    }
    
    /**
     * Initialize the GUI elements.
     *
     * @param socketOut used for button click handler
     */
    private void createGUI(PrintWriter socketOut) {
        // Create window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800,300));
        BorderLayout borderLayout = new BorderLayout(5,5);
        JPanel panel = new JPanel(borderLayout);
        panel.setBorder(BorderFactory.createTitledBorder("Mancala")); // Make fancy border
        frame.add(panel);

        // Initialize messageLabel (created as a class variable)
        messageLabel = new JLabel("Waiting for other player...");
        messageLabel.setHorizontalAlignment(0);
        messageLabel.setFont(new Font("", Font.BOLD, 30));
        panel.add(messageLabel, BorderLayout.NORTH);

        // Create the center grid of buttons
        JPanel center = new JPanel(new GridLayout(2,6,5,5));
        // Create 12 buttons
        for (int i = 0; i < 12; i++) {
            JButton btn = new JButton("4");
            // Opponents buttons
            if (i <= 5) {
                btn.setBackground(new Color(252, 217, 217));
                btn.setRolloverEnabled(false);
                btn.setBorder(new LineBorder(Color.GRAY));
                btn.setFont(new Font("Arial", Font.BOLD, 30));
            } else { // Our buttons
                btn.setBackground(new Color(225, 255, 224));
                btn.setBorder(new LineBorder(Color.GRAY));
                btn.setFont(new Font("Arial", Font.BOLD, 30));
                // Change button color when hovered over
                btn.getModel().addChangeListener(e -> {
                    final ButtonModel model = (ButtonModel) e.getSource();
                    if (model.isRollover() && Integer.parseInt(btn.getText()) != 0) { // Hovered over
                        btn.setBackground(new Color(188,253,138));
                    } else if (Integer.parseInt(btn.getText()) == 0 ) { // Button disabled
                        btn.setBackground(new Color(196, 204, 196));
                    } else { // Default
                        btn.setBackground(new Color(225, 255, 224));
                    }
                });
                
            }
            int finalI = i;
            // Click handler
            btn.addActionListener(e -> {
                if (messageLabel.getText().contains("Your")) { // Ignore input if it is not the users turn
                    // Offset output based on player, because index 0 in the GUI is top left, whereas index 0 in the
                    // terminal version is bottom left.
                    if (playerNum == 1) {
                        socketOut.println((finalI - 6) + "");
                    } else if (playerNum == 2) {
                        socketOut.println((finalI - 6) + "");
                    }
                }
            });
            
            board[i] = btn; // Save button to array
            center.add(board[i]); // Add button to center panel
        }
        panel.add(center, BorderLayout.CENTER); // Add center panel to main panel

        // Create left button (opponents store)
        JButton leftButton = new JButton("0");
        leftButton.setBackground(new Color(252, 217, 217));
        leftButton.setRolloverEnabled(false);
        leftButton.setBorder(new LineBorder(Color.GRAY));
        leftButton.setFont(new Font("Arial", Font.BOLD, 72));
        board[12] = leftButton;
        left.add(board[12]);
        left.setPreferredSize(new Dimension(95,100));
        panel.add(left, BorderLayout.WEST);

        // Create right button (our store)
        JButton rightButton = new JButton("0");
        rightButton.setBackground(new Color(225, 255, 224));
        rightButton.setRolloverEnabled(false);
        rightButton.setBorder(new LineBorder(Color.GRAY));
        rightButton.setFont(new Font("Arial", Font.BOLD, 72));
        board[13] = rightButton;
        right.add(board[13]);
        right.setPreferredSize(new Dimension(95,100));
        panel.add(right, BorderLayout.EAST);

        // Resize handler (change left and right sizes based on window size)
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                JFrame frame = (JFrame) evt.getSource(); // Get the JFrame
                Dimension size = frame.getSize(); // Get dimensions
                int width = size.width;
                int correctedWidth = width - (5*9); // Remove padding
                int newButtonWidth = correctedWidth / 8; // Get size of each button
                left.setPreferredSize(new Dimension(newButtonWidth, 100)); // Change size of left panel
                right.setPreferredSize(new Dimension(newButtonWidth, 100)); // Change size of right panel
            }
        });

        frame.setSize(800, 300);
        frame.setVisible(true);
    }
    
    /**
     * Function that updates the number in each button, and returns if the game is completed or not.
     *
     * @param socketIn used to get each incoming line
     * @return true/false
     * @throws IOException required for socketIn
     */
    private boolean updateBoard(BufferedReader socketIn) throws IOException {
        int currentLine = 0;
        while (true) {
            String line = socketIn.readLine();
            currentLine++;

            if (currentLine == 1) {
                board[12].setText(line.substring(1,3).trim());
                board[0].setText(line.substring(4,6).trim());
                board[1].setText(line.substring(7,9).trim());
                board[2].setText(line.substring(10,12).trim());
                board[3].setText(line.substring(13,15).trim());
                board[4].setText(line.substring(16,18).trim());
                board[5].setText(line.substring(19,21).trim());
            } else if (currentLine == 3) {
                board[6].setText(line.substring(4,6).trim());
                board[7].setText(line.substring(7,9).trim());
                board[8].setText(line.substring(10,12).trim());
                board[9].setText(line.substring(13,15).trim());
                board[10].setText(line.substring(16,18).trim());
                board[11].setText(line.substring(19,21).trim());
                board[13].setText(line.substring(22,24).trim());
            } else if (currentLine == 5 && !line.contains("[ 0")) {
                if (line.startsWith("Opponents")) {
                    messageLabel.setText(line);
                }
                break;
            } else if (currentLine == 6 && line.startsWith("Your")) {
                messageLabel.setText("Your turn! Click a pit.");
                break;
            }

            if (line.contains("Wins") || line.contains("Tie")) {
                if ((line.contains("1") && playerNum == 1) || (line.contains("2") && playerNum == 2)) {
                    messageLabel.setText("You win!");
                } else if ((line.contains("1") && playerNum == 2) || (line.contains("2") && playerNum == 1)) {
                    messageLabel.setText("You lost.");
                } else {
                    messageLabel.setText("It's a tie.");
                }
                return true;
            }
        }

        for (int i = 6; i < 12; i++) {
            if (Integer.parseInt(board[i].getText()) == 0) {
                board[i].setBackground(new Color(196, 204, 196));
            } else {
                board[i].setBackground(new Color(225, 255, 224));
            }
        }

        return false;
    }

    public static void main(String[] args) {
        new Client();
    }
}
