package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class ReconnectListener implements Listener {

    private int dots = 0;
    private int tries = 0;
    private ScheduledTask task;

    @EventHandler
    public void onReconnect(final ServerKickEvent event) {

        final ProxiedPlayer player = event.getPlayer();
        final ServerInfo kickedFrom = event.getKickedFrom();

        if (!player.isConnected()) {
            return;
        }

        for (String blacklist : BungeeConfig.IGNORED_REASONS.getStringList()) {
            if (BaseComponent.toLegacyText(event.getKickReasonComponent()).contains(blacklist)) {
                return;
            }
        }

        if (BungeeConfig.USE_BLACKLISTED_SERVERS.getBoolean()) {
            for (String blacklist : BungeeConfig.BLACKLISTED_SERVERS_LIST.getStringList()) {
                if (blacklist.contains(kickedFrom.getName())) {
                    return;
                }
            }
        }

        event.setCancelled(true);

        event.setState(ServerKickEvent.State.CONNECTED);

        task = ProxyServer.getInstance().getScheduler().schedule(FallbackServerBungee.getInstance(), () -> reConnect(kickedFrom, player), 3, 5, TimeUnit.SECONDS);

        Title title = ProxyServer.getInstance().createTitle();

        title.fadeIn(3 * 20);
        title.stay(9 * 20);
        title.fadeOut(2 * 20);

        title.title(new TextComponent("§a§lReconnecting§7%dots%".replace("%dots%", Utils.getDots(dots))));
        title.send(player);

    }

    private void reConnect(ServerInfo serverInfo, ProxiedPlayer player) {

        if (player.getServer().getInfo().equals(serverInfo)) {
            task.cancel();
            return;
        }

        if (tries >= 5) {
            task.cancel();
        }

        dots++;

        FallbackServerBungee.getInstance().getLogger().info("Method enter...");

        serverInfo.ping((result, error) -> {

            if (error != null) {
                tries++;
            }

            if (error == null) {

                player.connect(serverInfo);

            }
        });

    }

}
