package me.candiesjar.fallbackserveraddon.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.listeners.addon.packetevents.PacketHandler;

@UtilityClass
public class PacketEventsUtil {

    private final PacketEventsAPI packetEventsAPI = PacketEvents.getAPI();

    public void registerHandler() {
        packetEventsAPI.getEventManager().registerListener(new PacketHandler());
    }

    public void terminate() {
        PacketEvents.getAPI().terminate();
    }
}
