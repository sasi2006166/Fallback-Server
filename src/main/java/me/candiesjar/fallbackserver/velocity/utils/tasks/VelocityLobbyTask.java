package me.candiesjar.fallbackserver.velocity.utils.tasks;

import me.candiesjar.fallbackserver.velocity.objects.VelocityFallingServer;

public class VelocityLobbyTask implements Runnable {

    @Override
    public void run() {
        VelocityFallingServer.getServers().clear();


    }

}
