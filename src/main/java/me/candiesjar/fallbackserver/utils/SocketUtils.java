package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;

import java.net.Socket;

@UtilityClass
public class SocketUtils {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();

    public boolean checkPort() {

        String ip = fallbackServerVelocity.getServer().getBoundAddress().getAddress().getHostAddress();
        int port = VelocityConfig.RECONNECT_SOCKET_PORT.get(Integer.class);

        if (port < 0 || port > 65535) {
            return false;
        }

        try {
            Socket socket = new Socket(ip, port);
            socket.close();
            return true;
        } catch (Exception ignored) {
            return false;
        }

    }

}
