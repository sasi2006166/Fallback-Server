package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.utils.ServerUtils;
import me.candiesjar.fallbackserver.utils.tasks.ReconnectTask;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ReconnectListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {

        ProxiedPlayer player = event.getPlayer();

        ServerInfo kickedFrom = event.getKickedFrom();

        if (!player.isConnected()) {
            return;
        }

        for (String word : BungeeConfig.IGNORED_REASONS.getStringList()) {

            if (event.getKickReasonComponent() == null) {
                break;
            }

            if (BaseComponent.toLegacyText(event.getKickReasonComponent()).contains(word)) {
                return;
            }

        }

        boolean useBlacklist = BungeeConfig.USE_BLACKLISTED_SERVERS.getBoolean();

        if (useBlacklist && BungeeConfig.BLACKLISTED_SERVERS_LIST.getStringList().contains(kickedFrom.getName())) {
            return;
        }

        boolean isMaintenance = ServerUtils.checkMaintenance(kickedFrom);

        if (isMaintenance) {
            return;
        }

        event.setCancelled(true);

        event.setCancelServer(kickedFrom);

        ReconnectTask task = PlayerCacheManager.getInstance().get(player.getUniqueId());

        if (task == null) {
            PlayerCacheManager.getInstance().put(player.getUniqueId(), task = new ReconnectTask(player, kickedFrom, player.getUniqueId()));
        }

        task.reconnect();

    }

}
