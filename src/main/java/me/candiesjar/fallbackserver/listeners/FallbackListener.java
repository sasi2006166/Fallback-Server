package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.api.FallbackAPI;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.ServerUtils;
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
import net.md_5.bungee.event.EventPriority;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FallbackListener implements Listener {

    private final FallbackServerBungee plugin;

    public FallbackListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

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
        event.setCancelled(true);

        Map<ServerInfo, FallingServer> clonedMap = Maps.newHashMap(FallingServer.getServers());

        clonedMap.remove(kickedFrom);

        List<FallingServer> lobbies = Lists.newArrayList(clonedMap.values());

        if (lobbies.size() == 0) {
            if (event.getKickReasonComponent() == null) {
                player.disconnect(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.NO_SERVER)));
            }
            player.disconnect(new TextComponent(BaseComponent.toLegacyText(event.getKickReasonComponent())));
            return;
        }

        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        boolean isMaintenance = ServerUtils.checkMaintenance(serverInfo);

        if (isMaintenance) {
            player.disconnect(new TextComponent(BaseComponent.toLegacyText(event.getKickReasonComponent())));
            return;
        }

        event.setCancelServer(serverInfo);

        BungeeMessages.KICKED_TO_LOBBY.sendList(player,
                new Placeholder("server", serverInfo.getName()),
                new Placeholder("reason", ChatUtil.color(BaseComponent.toLegacyText(event.getKickReasonComponent()))));

        boolean useTitle = BungeeMessages.USE_FALLBACK_TITLE.getBoolean();

        if (useTitle) {

            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> TitleUtil.sendTitle(BungeeMessages.FALLBACK_FADE_IN.getInt(),
                            BungeeMessages.FALLBACK_STAY.getInt(),
                            BungeeMessages.FALLBACK_FADE_OUT.getInt(),
                            BungeeMessages.FALLBACK_TITLE,
                            BungeeMessages.FALLBACK_SUB_TITLE,
                            serverInfo,
                            player),
                    BungeeMessages.FALLBACK_DELAY.getInt(), 0, TimeUnit.SECONDS);
        }

        boolean notification = BungeeConfig.ADMIN_NOTIFICATION.getBoolean();

        if (notification) {

            ProxyServer.getInstance().getPlayers().stream().filter(all -> all != player);

        }

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> plugin.getProxy().getPluginManager().callEvent(new FallbackAPI(player, kickedFrom, serverInfo, BaseComponent.toLegacyText(event.getKickReasonComponent()))));
    }
}
