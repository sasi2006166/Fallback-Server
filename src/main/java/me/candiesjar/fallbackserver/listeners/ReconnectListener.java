package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.handlers.ReconnectHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;

public class ReconnectListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {

        ProxiedPlayer player = event.getPlayer();
        ServerInfo kickedFrom = event.getKickedFrom();

        if (!player.isConnected()) {
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
                return;
            }
        }

        boolean useBlacklist = BungeeConfig.USE_BLACKLISTED_SERVERS.getBoolean();

        if (useBlacklist && BungeeConfig.BLACKLISTED_SERVERS_LIST.getStringList().contains(kickedFrom.getName())) {
            return;
        }

        event.setCancelled(false);
        event.setState(ServerKickEvent.State.CONNECTING);
        event.setCause(ServerKickEvent.Cause.UNKNOWN);

        ReconnectHandler task = PlayerCacheManager.getInstance().get(player.getUniqueId());

        if (task == null) {
            PlayerCacheManager.getInstance().put(player.getUniqueId(), task = new ReconnectHandler(player, kickedFrom, player.getUniqueId()));
        }

        task.reconnect();

    }

}
