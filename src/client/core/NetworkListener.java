package client.core;

import common.protocol.Message;
import javax.swing.SwingUtilities;

public class NetworkListener implements Runnable {
    private final ChatClient client;
    private final MessageCallback callback;
    private volatile boolean running;
    
    public NetworkListener(ChatClient client, MessageCallback callback) {
        this.client = client;
        this.callback = callback;
    }
    
    @Override
    public void run() {
        running = true;
        
        while (running && client.isConnected()) {
            try {
                Message msg = client.receive();
                
                // CRITICAL: Update GUI on Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    callback.onMessageReceived(msg);
                });
                
            } catch (Exception e) {
                if (running) {
                    SwingUtilities.invokeLater(() -> {
                        callback.onConnectionLost();
                    });
                }
                running = false;
            }
        }
    }
    
    public void stop() {
        running = false;
    }
    
    public interface MessageCallback {
        void onMessageReceived(Message msg);
        void onConnectionLost();
    }
}
