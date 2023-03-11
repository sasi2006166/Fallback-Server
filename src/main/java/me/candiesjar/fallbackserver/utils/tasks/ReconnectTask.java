package me.candiesjar.fallbackserver.utils.tasks;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReconnectTask {

    private final ProxiedPlayer player;
    private final ServerInfo target;
    private final UUID uuid;

    private final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    @Getter
    private ScheduledTask reconnectTask;

    @Getter
    private ScheduledTask titleTask;

    public ReconnectTask(ProxiedPlayer player, ServerInfo target, UUID uuid) {
        this.player = player;
        this.target = target;
        this.uuid = uuid;
    }

    public void reconnect() {

        sendTitle(player);

        AtomicInteger tries = new AtomicInteger(0);

        reconnectTask = instance.getProxy().getScheduler().schedule(instance, () -> {

            System.out.println("Connessione in corso.. per " + player.getName());

            if (!player.isConnected()) {
                instance.cancelReconnect(uuid);
                return;
            }

            if (tries.get() == BungeeConfig.RECONNECT_TRIES.getInt()) {
                BungeeMessages.CONNECTION_FAILED.send(player,
                        new Placeholder("prefix", instance.getPrefix()));

                boolean fallback = BungeeConfig.RECONNECT_SORT.getBoolean();

                if (fallback) {

                    LinkedList<FallingServer> lobbies = Lists.newLinkedList(FallingServer.getServers().values());

                    if (lobbies.size() == 0) {
                        player.disconnect(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.NO_SERVER, new Placeholder("prefix", instance.getPrefix()))));
                        return;
                    }

                    lobbies.sort(FallingServer::compareTo);
                    lobbies.sort(Comparator.reverseOrder());

                    ServerInfo server = lobbies.get(0).getServerInfo();

                    player.connect(server);
                    instance.cancelReconnect(uuid);

                    return;

                }

                instance.cancelReconnect(player.getUniqueId());

                player.disconnect(new TextComponent(ProxyServer.getInstance().getTranslation("lost_connection")));

                return;
            }

            target.ping((result, error) -> {

                if (error != null || result == null) {
                    tries.getAndIncrement();
                    return;
                }

                player.connect(target);
                instance.cancelReconnect(uuid);

            });

        }, 0, BungeeConfig.RECONNECT_DELAY.getInt(), TimeUnit.SECONDS);

    }

    private void sendTitle(ProxiedPlayer player) {
        AtomicInteger dots = new AtomicInteger(0);

        titleTask = instance.getProxy().getScheduler().schedule(instance, () -> {

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
