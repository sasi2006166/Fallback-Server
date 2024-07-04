package me.candiesjar.fallbackserveraddon.listeners.addon.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.ProtocolLibUtil;
import me.candiesjar.fallbackserveraddon.utils.Utils;

public class PacketHandler extends PacketAdapter {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();
    private final TaskScheduler scheduler = UniversalScheduler.getScheduler(plugin);

    private boolean received = false;
    private boolean finished = false;

    public PacketHandler(FallbackServerAddon plugin) {
        super(plugin, PacketType.Status.Server.SERVER_INFO);
    }

    @Override
    public void onPacketSending(PacketEvent event) {

        if (finished) {
            return;
        }

        if (!plugin.isLocked()) {
            return;
        }

        WrappedServerPing ping = event.getPacket().getServerPings().read(0);
        ping.setPlayersMaximum(plugin.getConfig().getInt("settings.addon.override_player_count_number", -1));

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
            Utils.unregisterEvent(ProtocolLibUtil.getProtocolManager(), ProtocolLibUtil.getPacketListener());
        }, plugin.getConfig().getInt("settings.addon.disable_after", 30) * 20L);
    }
}
