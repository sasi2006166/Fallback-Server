package me.candiesjar.fallbackserver.handler;

import com.google.common.collect.Lists;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.ServerCacheManager;
import me.candiesjar.fallbackserver.enums.VelocityConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
public class SocketHandler {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final ServerCacheManager serverCacheManager = fallbackServerVelocity.getServerCacheManager();
    private final AtomicReference<ServerSocket> serverSocketRef = new AtomicReference<>(null);
    private final List<Socket> activeConnections = Lists.newArrayList();
    private ScheduledTask socketTask;

    public void start() {
        int serverPort = VelocityConfig.RECONNECT_SOCKET_PORT.get(Integer.class);
        ServerSocket serverSocket = serverSocketRef.get();
        if (serverSocket != null && !serverSocket.isClosed()) {
            stop();
        }

        serverSocket = createServerSocket(serverPort);
        if (serverSocket == null) {
            return;
        }

        serverSocketRef.set(serverSocket);

        socketTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, SocketHandler::acceptConnections)
                .repeat(VelocityConfig.RECONNECT_SOCKET_TASK.get(Integer.class), TimeUnit.SECONDS).schedule();
    }

    public void stop() {
        if (socketTask != null) {
            socketTask.cancel();
            socketTask = null;
        }

        ServerSocket serverSocket = serverSocketRef.getAndSet(null);
        if (serverSocket != null && !serverSocket.isClosed()) {
            for (Socket socket : activeConnections) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            activeConnections.clear();

            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        stop();
        start();
    }

    private void acceptConnections() {
        ServerSocket serverSocket = serverSocketRef.get();
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }

        try (Socket clientSocket = serverSocket.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String message = reader.readLine();

            String[] messageSplit = message.split(":");
            String ipAddress = messageSplit[0];
            int clientPort = Integer.parseInt(messageSplit[1]);

            serverCacheManager.put(ipAddress, clientPort);
            activeConnections.add(clientSocket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServerSocket createServerSocket(int serverPort) {
        try {
            return new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
