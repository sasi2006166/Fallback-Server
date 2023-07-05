package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.handlers.ReconnectHandler;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;

public class ReconnectListener implements Listener {

    private final FallbackServerBungee plugin;

    public ReconnectListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {

        ProxiedPlayer player = event.getPlayer();
        ServerInfo kickedFrom = event.getKickedFrom();
        UserConnection userConnection = (UserConnection) player;
        ServerConnection serverConnection = userConnection.getServer();
        ServerKickEvent.State state = event.getState();

        if (!player.isConnected()) {
            return;
        }

        if (state != ServerKickEvent.State.CONNECTED) {
            return;
        }

        boolean isEmpty = event.getKickReasonComponent() == null;
        String reason = isEmpty ? "" : BaseComponent.toLegacyText(event.getKickReasonComponent());
        List<String> ignoredReasons = BungeeConfig.RECONNECT_IGNORED_REASONS.getStringList();

        for (String word : ignoredReasons) {

            if (isEmpty) {
                break;
            }

            if (reason.contains(word)) {
                disconnect(player, reason);
                return;
            }
        }

        if (BungeeConfig.RECONNECT_IGNORED_SERVERS.getStringList().contains(kickedFrom.getName())) {
            disconnect(player, reason);
            return;
        }

        ReconnectHandler task = plugin.getPlayerCacheManager().get(player.getUniqueId());

        if (task == null) {
            plugin.getPlayerCacheManager().put(player.getUniqueId(), task = new ReconnectHandler(player, serverConnection, player.getUniqueId()));
        }

        userConnection.getServerSentScoreboard().clear();
        userConnection.resetTabHeader();

        task.start();

    }

    private void disconnect(ProxiedPlayer player, String reason) {
        player.disconnect(new TextComponent(reason));
    }

}
