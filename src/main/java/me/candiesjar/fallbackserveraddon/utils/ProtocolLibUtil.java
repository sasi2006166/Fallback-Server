package me.candiesjar.fallbackserveraddon.utils;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketListener;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.listeners.addon.protocollib.PacketHandler;

@UtilityClass
public class ProtocolLibUtil {

    public void start(FallbackServerAddon plugin) {
        protocolManager = getDefaultProtocolManager();
        plugin.getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] ProtocolLib support enabled.");
        packetListener = new PacketHandler(plugin);
        protocolManager.addPacketListener(packetListener);
    }

    public ProtocolManager getDefaultProtocolManager() {
        return ProtocolLibrary.getProtocolManager();
    }

    @Getter
    private ProtocolManager protocolManager;

    @Getter
    private PacketListener packetListener;
}
