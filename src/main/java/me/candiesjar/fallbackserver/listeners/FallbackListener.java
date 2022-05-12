package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.api.FallbackAPI;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.candiesjar.fallbackserver.enums.BungeeConfig.BLACKLISTED_WORDS;
import static me.candiesjar.fallbackserver.enums.BungeeMessages.*;

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

        for (String blacklist : BLACKLISTED_WORDS.getStringList()) {
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
                player.disconnect(new TextComponent(ChatUtil.getFormattedString(NO_SERVER, new PlaceHolder("prefix", instance.getPrefix()))));
            }
            return;
        }

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        event.setCancelServer(serverInfo);

        if (BungeeMessages.USE_FALLBACK_TITLE.getBoolean()) {
            ProxyServer.getInstance().getScheduler().schedule(instance, () -> titleUtil.sendTitle(FALLBACK_FADE_IN.getInt(),
                    FALLBACK_STAY.getInt(),
                    FALLBACK_FADE_OUT.getInt(),
                    FALLBACK_TITLE,
                    FALLBACK_SUB_TITLE,
                    player),
                    BungeeMessages.FALLBACK_DELAY.getInt(), 0, TimeUnit.SECONDS);
        } else {
            BungeeMessages.KICKED_TO_LOBBY.sendList(player,
                    new PlaceHolder("prefix", instance.getPrefix()),
                    new PlaceHolder("server", serverInfo.getName()),
                    new PlaceHolder("reason", BaseComponent.toLegacyText(event.getKickReasonComponent())));
        }

        ProxyServer.getInstance().getScheduler().runAsync(instance, () -> plugin.getProxy().getPluginManager().callEvent(new FallbackAPI(player, kickedFrom, serverInfo, Arrays.toString(event.getKickReasonComponent()))));
    }
}
