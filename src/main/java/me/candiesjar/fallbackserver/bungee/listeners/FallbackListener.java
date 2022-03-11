package me.candiesjar.fallbackserver.bungee.listeners;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.api.FallbackAPI;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.objects.FallingServer;
import me.candiesjar.fallbackserver.bungee.utils.TitleUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class FallbackListener implements Listener {

    private final FallbackServerBungee plugin;
    private final TitleUtil titleUtil = new TitleUtil();

    public FallbackListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerKickEvent(ServerKickEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        final ServerInfo kickedFrom = event.getKickedFrom();

        event.setCancelled(true);

        Map<ServerInfo, FallingServer> clonedMap = new HashMap<>(FallingServer.getServers());

        clonedMap.remove(kickedFrom);

        LinkedList<FallingServer> lobbies = new LinkedList<>(clonedMap.values());
        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        try {
            ServerInfo serverInfo = lobbies.get(0).getServerInfo();
            event.setCancelServer(serverInfo);
            if (BungeeMessages.USE_FALLBACK_TITLE.getBoolean())
                titleUtil.sendFallbackTitle(player);
            else
                player.sendMessage(new TextComponent(BungeeMessages.CONNECTED.getFormattedString()
                        .replace("%server%", kickedFrom.getName())));
            plugin.getProxy().getPluginManager().callEvent(new FallbackAPI(player, kickedFrom));
        } catch (IndexOutOfBoundsException ignored) {
            player.disconnect(new TextComponent(BungeeMessages.NO_SERVER.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
        }
    }
}
