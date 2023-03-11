package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(ServerConnectEvent event) {

        ProxiedPlayer player = event.getPlayer();

        if (!player.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            return;
        }

        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            return;
        }

        if (FallbackServerBungee.getInstance().isAlpha()) {
            player.sendMessage(new TextComponent(" "));
            player.sendMessage(new TextComponent("§7You're running an §c§lALPHA VERSION §7of Fallback Server."));
            player.sendMessage(new TextComponent("§7If you find any bugs, please report them on discord."));
            player.sendMessage(new TextComponent(" "));
            return;
        }

        if (Utils.isUpdateAvailable()) {
            BungeeMessages.NEW_UPDATE.sendList(player,
                    new Placeholder("old_version", FallbackServerBungee.getInstance().getVersion()),
                    new Placeholder("new_version", Utils.getRemoteVersion()));
        }

    }
}
