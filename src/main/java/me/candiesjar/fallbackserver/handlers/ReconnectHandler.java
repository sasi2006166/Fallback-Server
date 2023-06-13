package me.candiesjar.fallbackserver.handlers;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReconnectHandler {

    private final String LOST_CONNECTION = ProxyServer.getInstance().getTranslation("lost_connection");

    private final ProxiedPlayer player;
    private final ServerInfo target;
    private final UUID uuid;

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    @Getter
    private ScheduledTask reconnectTask;

    @Getter
    private ScheduledTask titleTask;

    public ReconnectHandler(ProxiedPlayer player, ServerInfo target, UUID uuid) {
        this.player = player;
        this.target = target;
        this.uuid = uuid;
    }

    public void reconnect() {

        sendTitle(player);

        AtomicInteger tries = new AtomicInteger(0);

        reconnectTask = fallbackServerBungee.getProxy().getScheduler().schedule(fallbackServerBungee, () -> {

            System.out.println("Reconnecting " + player.getName() + " to " + target.getName() + "...");

            if (tries.get() == BungeeConfig.RECONNECT_TRIES.getInt()) {
                BungeeMessages.CONNECTION_FAILED.send(player);
                boolean fallback = BungeeConfig.RECONNECT_SORT.getBoolean();

                if (fallback) {
                    List<FallingServer> lobbies = Lists.newArrayList(FallingServer.getServers().values());

                    if (lobbies.isEmpty()) {
                        player.disconnect(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.NO_SERVER)));
                        return;
                    }

                    lobbies.sort(Comparator.comparing(server -> server.getServerInfo().getPlayers().size()));

                    ServerInfo server = lobbies.get(0).getServerInfo();

                    player.connect(server);
                    fallbackServerBungee.cancelReconnect(uuid);

                    return;
                }

                fallbackServerBungee.cancelReconnect(player.getUniqueId());
                player.disconnect(new TextComponent(LOST_CONNECTION));
                return;
            }

            target.ping((result, error) -> {

                if (error != null || result == null) {
                    tries.getAndIncrement();
                    return;
                }

                player.connect(target);
                fallbackServerBungee.cancelReconnect(uuid);

            });

        }, 0, BungeeConfig.RECONNECT_DELAY.getInt(), TimeUnit.SECONDS);

    }

    private void sendTitle(ProxiedPlayer player) {
        AtomicInteger dots = new AtomicInteger(0);

        titleTask = fallbackServerBungee.getProxy().getScheduler().schedule(fallbackServerBungee, () -> {

            if (dots.getAndIncrement() == 4) {
                dots.set(0);
            }

            TitleUtil.sendReconnectingTitle(0, 1 + 20, dots.get(), BungeeMessages.RECONNECT_TITLE, BungeeMessages.RECONNECT_SUB_TITLE, player);
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void clear() {
        ProxyServer.getInstance().createTitle()
                .reset()
                .clear()
                .send(player);
    }
}
