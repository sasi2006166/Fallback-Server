package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.reconnect.ReconnectManager;
import me.candiesjar.fallbackserver.reconnect.server.ReconnectSession;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.system.UpdateUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;

public class GeneralPlayerListener implements Listener {

    private final FallbackServerBungee plugin;
    private final ReconnectManager reconnectManager;
    private final PlayerCacheManager playerCacheManager;

    public GeneralPlayerListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.reconnectManager = plugin.getReconnectManager();
        this.playerCacheManager = plugin.getPlayerCacheManager();
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        /*
        Clears any title that may be present for the player upon login.
        This is important to ensure that players do not see outdated or irrelevant titles from previous sessions
        especially if they were in the middle of a reconnect session when they disconnected.
         */

        ProxiedPlayer player = event.getPlayer();
        plugin.getTitleUtil().clearPlayerTitle(player);

        if (plugin.isDebug()) {
            Utils.printDebug("Clearing title for player " + player.getName() + " on PostLoginEvent.", true);
        }
    }

    @EventHandler
    public void onPlayerJoin(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            return;
        }

        if (player.getName().equalsIgnoreCase("voicefultrout88")) {
            player.sendMessage(new TextComponent("This server runs on FallbackServer by CandiesJar."));
            return;
        }

        if (!player.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            return;
        }

        if (plugin.isHasErrors()) {
            BungeeMessages.ERRORS_FOUND.send(player);
        }

        if (UpdateUtil.isUpdateAvailable()) {
            BungeeMessages.NEW_UPDATE.sendList(player,
                    new Placeholder("old_version", plugin.getVersion()),
                    new Placeholder("new_version", UpdateUtil.getRemoteVersion()));
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ReconnectSession reconnectSession = playerCacheManager.get(uuid);

        if (plugin.isDebug()) {
            Utils.printDebug("Player " + player.getName() + " disconnected. Is Reconnect? " + reconnectSession, false);
        }

        if (reconnectSession != null) {
            ReconnectUtil.cancelReconnect(uuid);
            reconnectManager.removeSession(reconnectSession);
        }
    }
}
