package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.handlers.ReconnectHandler;
import me.candiesjar.fallbackserver.utils.CheckUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
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

        boolean isEmpty = event.getKickReasonComponent() == null;
        String reason = isEmpty ? "" : BaseComponent.toLegacyText(event.getKickReasonComponent());

        boolean canContinue = CheckUtil.preChecks(player, state, reason, true);

        if (canContinue) {
            disconnect(player, reason);
            return;
        }

        if (checkIgnoredServers(kickedFrom.getName())) {
            disconnect(player, reason);
            return;
        }

        ReconnectHandler task = plugin.getPlayerCacheManager().get(player.getUniqueId());

        if (task == null) {
            plugin.getPlayerCacheManager().put(player.getUniqueId(), task = new ReconnectHandler(player, serverConnection, player.getUniqueId()));
        }

        boolean clearTab = BungeeConfig.RECONNECT_CLEAR_TABLIST.getBoolean();

        if (clearTab) {
            userConnection.resetTabHeader();
        }

        boolean clearChat = BungeeConfig.CLEAR_CHAT_RECONNECT_JOIN.getBoolean();

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        task.start();

        boolean usePhysicalServer = BungeeConfig.RECONNECT_USE_SERVER.getBoolean();

        if (usePhysicalServer) {
            event.setCancelled(true);
        }

    }

    private boolean checkIgnoredServers(String serverName) {
        return BungeeConfig.RECONNECT_IGNORED_SERVERS.getStringList().contains(serverName);
    }

    private void disconnect(ProxiedPlayer player, String reason) {
        player.disconnect(new TextComponent(reason));
    }

}
