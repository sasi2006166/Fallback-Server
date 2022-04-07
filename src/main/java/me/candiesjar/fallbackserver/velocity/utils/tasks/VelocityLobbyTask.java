package me.candiesjar.fallbackserver.velocity.utils.tasks;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.velocity.objects.VelocityFallingServer;

public class VelocityLobbyTask implements Runnable {

    @Override
    public void run() {
        VelocityFallingServer.getServers().clear();

    }

}
