package me.candiesjar.fallbackserver.bungee.listeners;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.api.FallbackAPI;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.objects.FallingServer;
import me.candiesjar.fallbackserver.bungee.objects.PlaceHolder;
import me.candiesjar.fallbackserver.bungee.utils.TitleUtil;
import me.candiesjar.fallbackserver.bungee.utils.chat.ChatUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
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
    private final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    public FallbackListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerKickEvent(final ServerKickEvent event) {

        final ProxiedPlayer player = event.getPlayer();
        final ServerInfo kickedFrom = event.getKickedFrom();

        if (!player.isConnected()) {
            return;
        }

        for (String blacklist : BungeeConfig.BLACKLISTED_WORDS.getStringList()) {
            if (BaseComponent.toLegacyText(event.getKickReasonComponent()).contains(blacklist)) {
                return;
            }
        }

        event.setCancelled(true);

        Map<ServerInfo, FallingServer> clonedMap = new HashMap<>(FallingServer.getServers());

        clonedMap.remove(kickedFrom);

        LinkedList<FallingServer> lobbies = new LinkedList<>(clonedMap.values());
        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        if (lobbies.size() == 0) {
            if (event.getKickReasonComponent() == null) {
                player.disconnect(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.NO_SERVER, new PlaceHolder("prefix", instance.getPrefix()))));
            }
            return;
        }

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        event.setCancelServer(serverInfo);

        if (BungeeMessages.USE_FALLBACK_TITLE.getBoolean()) {
            titleUtil.sendFallbackTitle(player);
        } else {
            BungeeMessages.CONNECTED.send(player, new PlaceHolder("prefix", instance.getPrefix()));
        }

        ProxyServer.getInstance().getScheduler().runAsync(instance, () -> plugin.getProxy().getPluginManager().callEvent(new FallbackAPI(player, kickedFrom)));
    }
}
