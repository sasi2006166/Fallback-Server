package me.candiesjar.fallbackserver.handler;

import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;

public class DebugLimboHandler implements LimboSessionHandler {

    @Override
    public void onSpawn(Limbo server, LimboPlayer player) {
        player.flushPackets();
        player.disableFalling();
    }
}
