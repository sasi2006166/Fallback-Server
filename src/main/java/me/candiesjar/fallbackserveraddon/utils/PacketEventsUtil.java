package me.candiesjar.fallbackserveraddon.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.listeners.addon.packetevents.PacketHandler;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class PacketEventsUtil {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();
    private final PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();

    public void registerHandler() {
        packetEventsAPI.getEventManager().registerListener(new PacketHandler());
    }

    public void terminate() {
        packetEventsAPI.terminate();
    }

    public void sendKeepAlive() {
        CompletableFuture.runAsync(() -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                if (user != null) user.sendPacket(new WrapperPlayServerKeepAlive(System.nanoTime() ^ ThreadLocalRandom.current().nextLong()));
            }
        });
    }
}
