package server.monitor;

import server.core.ServerContext;
import common.protocol.Message;
import common.protocol.MessageType;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatService {
    private final ServerContext context;
    private ScheduledExecutorService scheduler;
    private volatile boolean running;

    public HeartbeatService(ServerContext context) {
        this.context = context;
        this.running = false;
    }

    public void start() {
        if (running) {
            return;
        }

        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(this::sendPings,
                context.getConfig().getPingInterval(),
                context.getConfig().getPingInterval(),
                TimeUnit.MILLISECONDS);
    }

    private void sendPings() {
        if (!running) {
            return;
        }

        context.getHandlers().forEach((username, handler) -> {
            try {
                var session = handler.getSession();
                if (session != null) {
                    if (session.getMissedPings() >= context.getConfig().getMaxMissedPings()) {
                        System.out.println("[HEARTBEAT] Timeout for " + username);
                        handler.disconnect();
                        return;
                    }

                    Message ping = new Message(MessageType.PING, "SERVER", username, "");
                    handler.send(ping);
                    session.incrementMissedPings();
                }
            } catch (Exception e) {
                // Ignore
            }
        });
    }

    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    public boolean isRunning() {
        return running;
    }
}