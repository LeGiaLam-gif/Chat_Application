package client.gui;

import client.core.ChatClient;
import client.core.NetworkListener;
import client.service.FileSender;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import java.io.File;

public class ClientController implements NetworkListener.MessageCallback {
    private ChatClient client;
    private ChatFrame frame;
    private NetworkListener listener;
    private Thread listenerThread;
    private FileSender fileSender;

    public void start() {
        // Show login dialog
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);

        if (!loginDialog.isConfirmed()) {
            System.exit(0);
        }

        try {
            // Connect to server
            client = new ChatClient(
                    loginDialog.getHost(),
                    loginDialog.getPort(),
                    loginDialog.getUsername());

            // Create GUI
            frame = new ChatFrame(loginDialog.getUsername());
            frame.setVisible(true);

            // Setup listeners
            frame.sendButton.addActionListener(e -> sendMessage());
            frame.inputField.addActionListener(e -> sendMessage());
            frame.fileButton.addActionListener(e -> sendFile());
            frame.userList.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        startPrivateChat();
                    }
                }
            });

            // Start network listener
            listener = new NetworkListener(client, this);
            listenerThread = new Thread(listener);
            listenerThread.start();

            // Initialize FileSender
            fileSender = new FileSender(client);

            frame.appendMessage("=== Connected to server ===");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void sendMessage() {
        String text = frame.inputField.getText().trim();
        if (text.isEmpty())
            return;

        frame.inputField.setText("");

        try {
            if (text.startsWith("/pm ")) {
                String[] parts = text.split(" ", 3);
                if (parts.length >= 3) {
                    Message msg = new Message(MessageType.PRIVATE,
                            client.getUsername(), parts[1], parts[2]);
                    client.send(msg);
                    frame.appendMessage("[PM to " + parts[1] + "] " + parts[2]);
                }
            } else if (text.startsWith("/")) {
                Message msg = new Message(MessageType.COMMAND,
                        client.getUsername(), text);
                client.send(msg);
            } else {
                Message msg = new Message(MessageType.CHAT,
                        client.getUsername(), text);
                client.send(msg);
            }
        } catch (Exception e) {
            frame.appendMessage("Error sending message: " + e.getMessage());
        }
    }

    private void sendFile() {
        String selectedUser = frame.userList.getSelectedValue();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame,
                    "Please select a user to send file to");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            new Thread(() -> {
                try {
                    fileSender.sendFile(file, selectedUser);
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        frame.appendMessage("File transfer failed: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void startPrivateChat() {
        String selectedUser = frame.userList.getSelectedValue();
        if (selectedUser != null) {
            frame.inputField.setText("/pm " + selectedUser + " ");
            frame.inputField.requestFocus();
        }
    }

    @Override
    public void onMessageReceived(Message msg) {
        // Handle all MessageType enum values - NO WARNINGS!
        switch (msg.getType()) {
            case CHAT:
                frame.appendMessage("[" + msg.getSender() + "] " + msg.getContent());
                break;

            case PRIVATE:
                frame.appendMessage("[PM from " + msg.getSender() + "] " + msg.getContent());
                break;

            case SERVER:
                frame.appendMessage("*** " + msg.getContent() + " ***");
                updateUserList(msg);
                break;

            case FILE_META:
                handleFileOffer(msg);
                break;

            case FILE_CHUNK:
                // TODO: Implement file receiving
                break;

            case FILE_ACK:
                // Handled by FileSender
                break;

            // Connection management - handled elsewhere but need cases for no warnings
            case CONNECT:
                // Already handled during connection
                break;

            case ACCEPT:
                // Already handled during connection
                break;

            case REJECT:
                // Already handled during connection
                frame.appendMessage("*** Connection rejected: " + msg.getContent() + " ***");
                break;

            case DISCONNECT:
                // Server disconnecting us
                frame.appendMessage("*** Server disconnected ***");
                break;

            // Heartbeat
            case PING:
                // Send PONG response
                try {
                    Message pong = new Message(MessageType.PONG,
                            client.getUsername(), "");
                    client.send(pong);
                } catch (Exception e) {
                    // Ignore
                }
                break;

            case PONG:
                // We don't send PING, so this shouldn't happen
                break;

            // Command responses are sent as SERVER messages
            case COMMAND:
                // We send commands, don't receive them
                break;

            default:
                frame.appendMessage("*** Unknown message type: " + msg.getType() + " ***");
                break;
        }
    }

    private void updateUserList(Message msg) {
        if (msg.getContent().contains("joined") || msg.getContent().contains("left")) {
            // Request user list
            try {
                client.send(new Message(MessageType.COMMAND,
                        client.getUsername(), "/who"));
            } catch (Exception e) {
                // Ignore
            }
        } else if (msg.getContent().startsWith("Online users:")) {
            String[] users = msg.getContent().substring(14).split(", ");
            frame.userListModel.clear();
            for (String user : users) {
                String trimmed = user.trim();
                if (!trimmed.isEmpty() && !trimmed.equals(client.getUsername())) {
                    frame.userListModel.addElement(trimmed);
                }
            }
        }
    }

    private void handleFileOffer(Message msg) {
        String filename = msg.getFilename();
        String sender = msg.getSender();

        int choice = JOptionPane.showConfirmDialog(frame,
                sender + " wants to send you: " + filename + "\nAccept?",
                "File Transfer", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            // TODO: Implement file receiving
            frame.appendMessage("*** File receiving not yet implemented ***");
        }
    }

    @Override
    public void onConnectionLost() {
        frame.appendMessage("*** Connection lost ***");
        frame.updateStatus("Disconnected");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientController().start();
        });
    }
}