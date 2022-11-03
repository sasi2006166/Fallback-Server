package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCache;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import me.candiesjar.fallbackserver.utils.tasks.ReconnectTask;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

public class ReconnectListener implements Listener {

    private int dots = 0;

    private ScheduledTask titleTask;

    private final FallbackServerBungee plugin;

    public ReconnectListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {

        ProxiedPlayer player = event.getPlayer();

        ServerInfo kickedFrom = event.getKickedFrom();

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

        event.setCancelled(true);

        ReconnectTask task = PlayerCache.getReconnectMap().get(player.getUniqueId());

        if (task == null) {
            PlayerCache.getReconnectMap().put(player.getUniqueId(), task = new ReconnectTask(player, kickedFrom, player.getUniqueId()));
        }

        task.reconnect();

    }

    private void refreshTitle(ProxiedPlayer player) {

        titleTask = ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {

            dots++;

            if (dots == 4) {
                dots = 0;
            }

            TitleUtil.sendReconnectingTitle(0,
                    1 + 20,
                    dots,
                    BungeeMessages.RECONNECT_TITLE,
                    BungeeMessages.RECONNECT_SUB_TITLE,
                    player);


        }, 0, 1, TimeUnit.SECONDS);

    }

}
