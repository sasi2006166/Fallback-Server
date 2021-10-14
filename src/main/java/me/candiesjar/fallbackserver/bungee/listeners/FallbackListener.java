package me.candiesjar.fallbackserver.bungee.listeners;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.api.FallbackAPI;
import me.candiesjar.fallbackserver.bungee.enums.MessagesFields;
import me.candiesjar.fallbackserver.bungee.utils.RedirectServerWrapper;
import me.candiesjar.fallbackserver.bungee.utils.ServerGroup;
import me.candiesjar.fallbackserver.bungee.utils.TitleUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FallbackListener implements Listener {

    private final FallbackServerBungee plugin;
    private final TitleUtil titleUtil = new TitleUtil();

    public FallbackListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerKickEvent(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo kickedFrom = event.getKickedFrom();

        RedirectServerWrapper redirectServerWrapper = plugin.getServer(kickedFrom.getName());
        ServerGroup serverGroup;
        if (redirectServerWrapper != null) {
            serverGroup = redirectServerWrapper.getServerGroup();
        } else serverGroup = plugin.getUnknownServerGroup();
        if (serverGroup == null) {
            return;
        }

        RedirectServerWrapper targetServer = serverGroup.getRedirectServer(player, kickedFrom.getName(), true, serverGroup.getSpreadMode());
        if (targetServer == null) {
            return;
        }

        event.setCancelled(true);
        event.setCancelServer(targetServer.getServerInfo());
        plugin.getProxy().getPluginManager().callEvent(new FallbackAPI(player, kickedFrom));
        if (MessagesFields.USE_FALLBACK_TITLE.getBoolean())
            titleUtil.sendFallbackTitle(player);
        else
            player.sendMessage(new TextComponent(MessagesFields.MOVED_TO_FALLBACK.getFormattedString().replace("%server%", kickedFrom.getName())));
        targetServer.addProxiedPlayer();
        if (redirectServerWrapper != null)
            redirectServerWrapper.removeProxiedPlayer();
    }
}
