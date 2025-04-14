package me.candiesjar.fallbackserveraddon.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public class ProtocolLibUtil {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();

    public void sendKeepAlive() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        CompletableFuture.runAsync(() -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                protocolManager.sendServerPacket(
                        player,
                        new PacketContainer(PacketType.Play.Server.KEEP_ALIVE)
                );
            }
        });
    }
}
