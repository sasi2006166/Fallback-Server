package me.candiesjar.fallbackserveraddon.listeners.addon.packetevents;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.Utils;

public class PacketHandler extends PacketListenerAbstract {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();
    private final TaskScheduler scheduler = UniversalScheduler.getScheduler(plugin);

    private boolean received = false;
    private boolean finished = false;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.JOIN_GAME) {
            return;
        }

        WrapperPlayServerJoinGame response = new WrapperPlayServerJoinGame(event);
        response.setMaxPlayers(-1);
        if (finished) {
            return;
        }

        if (!plugin.isLocked()) {
            return;
        }

        response.setMaxPlayers(plugin.getConfig().getInt("settings.addon.override_player_count_number", -1));

        if (received) {
            return;
        }

        received = true;

        if (plugin.getConfig().getBoolean("settings.addon.auto_remove", false)) {
            scheduler.runTaskLater(() -> plugin.getServer().getPluginManager().disablePlugin(plugin), plugin.getConfig().getInt("settings.disable_after", 30) * 20L);
            return;
        }

        scheduler.runTaskLater(() -> {
            finished = true;
            Utils.unregisterEvent(PacketEvents.getAPI());
        }, plugin.getConfig().getInt("settings.addon.disable_after", 30) * 20L);
    }
}
