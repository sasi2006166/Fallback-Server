package me.candiesjar.fallbackserver.handler;

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
import java.util.concurrent.TimeUnit;

@UtilityClass
public class SocketHandler {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final ServerCacheManager serverCacheManager = fallbackServerVelocity.getServerCacheManager();
    private ServerSocket serverSocket;
    private ScheduledTask socketTask;
    private boolean socketClosed = true;

    public void start() {
        int serverPort = VelocityConfig.RECONNECT_SOCKET_PORT.get(Integer.class);
        if (!socketClosed) {
            closeSocket();
        }

        serverSocket = createServerSocket(serverPort);
        if (serverSocket == null) {
            return;
        }

        socketClosed = false;

        socketTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, SocketHandler::acceptConnections)
                .repeat(VelocityConfig.RECONNECT_SOCKET_TASK.get(Integer.class), TimeUnit.SECONDS).schedule();
    }

    public void stop() {
        socketTask.cancel();
        closeSocket();
        socketClosed = true;
    }

    private void acceptConnections() {
        try {
            if (serverSocket == null || serverSocket.isClosed()) {
                int serverPort = VelocityConfig.RECONNECT_SOCKET_PORT.get(Integer.class);
                serverSocket = new ServerSocket(serverPort);
            }

            Socket clientSocket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = reader.readLine();

            String[] messageSplit = message.split(":");
            String ipAddress = messageSplit[0];
            int clientPort = Integer.parseInt(messageSplit[1]);

            serverCacheManager.put(ipAddress, clientPort);

            reader.close();
            clientSocket.close();
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

    private void closeSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
