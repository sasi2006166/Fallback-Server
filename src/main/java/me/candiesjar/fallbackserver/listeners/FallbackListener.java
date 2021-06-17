package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServer;
import me.candiesjar.fallbackserver.managers.TitleManager;
import me.candiesjar.fallbackserver.utils.Fields;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FallbackListener implements Listener {

    private final FallbackServer plugin;
    private final TitleManager titleManager = new TitleManager();

    public FallbackListener(FallbackServer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerKickEvent(ServerKickEvent event) {
        ServerInfo kickedFrom;
        ProxiedPlayer player = event.getPlayer();
        if (player.getServer() != null) {
            kickedFrom = player.getServer().getInfo();
        } else if (plugin.getProxy().getReconnectHandler() != null) {
            kickedFrom = plugin.getProxy().getReconnectHandler().getServer(player);
        } else {
            kickedFrom = AbstractReconnectHandler.getForcedHost(player.getPendingConnection());
            if (kickedFrom == null) {
                kickedFrom = ProxyServer.getInstance().getServerInfo(player.getPendingConnection().getListener().getDefaultServer());
            }
        }
        if (kickedFrom != null && kickedFrom.equals(ProxyServer.getInstance().getServerInfo(Fields.LOBBYSERVER.getString()))) {
            return;
        }
        event.setCancelled(true);
        event.setCancelServer(ProxyServer.getInstance().getServerInfo(Fields.LOBBYSERVER.getString()));
        if (Fields.USETITLE.getBoolean()) {
            titleManager.sendTitle(player);
        } else if (kickedFrom != null)
            player.sendMessage(new TextComponent(Fields.CONNECTEDTOFALLBACK.getFormattedString().replace("%server%", kickedFrom.getName())));
        else player.sendMessage(new TextComponent(Fields.CONNECTEDTOFALLBACK.getFormattedString()));
    }
}
