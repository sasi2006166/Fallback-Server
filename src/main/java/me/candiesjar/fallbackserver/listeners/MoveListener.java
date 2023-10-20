package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.events.FallbackEvent;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import me.candiesjar.fallbackserver.utils.server.ServerUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MoveListener implements Listener {

    private final FallbackServerBungee plugin;

    public MoveListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFallback(FallbackEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo kickServer = event.getKickServer();
        String reason = event.getReason();

        if (!player.isConnected()) {
            disconnect(player, reason);
            return;
        }

        FallingServer.removeServer(kickServer);
        List<FallingServer> lobbies = Lists.newArrayList(FallingServer.getServers().values());

        boolean hasMaintenance = plugin.isMaintenance();

        if (hasMaintenance) {
            lobbies.removeIf(fallingServer -> ServerUtils.checkMaintenance(fallingServer.getServerInfo()));
        }

        if (lobbies.isEmpty()) {
            player.disconnect(new TextComponent(reason));
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getServerInfo().getPlayers().size()));

        ServerInfo lobby = lobbies.get(0).getServerInfo();

        player.connect(lobby);

        boolean clearChat = BungeeConfig.CLEAR_CHAT_FALLBACK.getBoolean();

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        BungeeMessages.CONNECTION_FAILED.send(player);

        boolean useTitle = BungeeMessages.USE_FALLBACK_TITLE.getBoolean();

        if (useTitle) {

            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> TitleUtil.sendTitle(BungeeMessages.FALLBACK_FADE_IN.getInt(),
                            BungeeMessages.FALLBACK_STAY.getInt(),
                            BungeeMessages.FALLBACK_FADE_OUT.getInt(),
                            BungeeMessages.FALLBACK_TITLE,
                            BungeeMessages.FALLBACK_SUB_TITLE,
                            kickServer,
                            player),
                    BungeeMessages.FALLBACK_DELAY.getInt(), 0, TimeUnit.SECONDS);
        }

    }

    private void disconnect(ProxiedPlayer player, String reason) {
        player.disconnect(new TextComponent(reason));
    }
}
