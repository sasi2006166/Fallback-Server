package me.candiesjar.fallbackserver.velocity.api;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Objects;

public class VelocityAPI implements ResultedEvent<ResultedEvent.GenericResult> {

    private final Player player;
    private final RegisteredServer kickedFrom;
    private final RegisteredServer kickedTo;
    private final String kickedReason;

    private GenericResult result = GenericResult.allowed();

    public VelocityAPI(Player player, RegisteredServer kickedFrom, RegisteredServer kickedTo, String kickedReason) {
        this.player = player;
        this.kickedFrom = kickedFrom;
        this.kickedTo = kickedTo;
        this.kickedReason = kickedReason;
    }

    @Override
    public GenericResult getResult() {
        return result;
    }

    @Override
    public void setResult(GenericResult genericResult) {
        this.result = Objects.requireNonNull(genericResult);
    }

    public Player getPlayer() {
        return player;
    }

    public RegisteredServer getKickedFrom() {
        return kickedFrom;
    }

    public RegisteredServer getKickedTo() {
        return kickedTo;
    }

    public String getKickedReason() {
        return kickedReason;
    }
}
