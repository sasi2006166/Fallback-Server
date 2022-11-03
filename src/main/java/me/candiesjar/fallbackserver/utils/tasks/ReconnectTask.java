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
    private ScheduledTask task;

    public ReconnectTask(ProxiedPlayer player, ServerInfo target, UUID uuid) {
        this.player = player;
        this.target = target;
        this.uuid = uuid;
    }

    public void reconnect() {

        task = ProxyServer.getInstance().getScheduler().schedule(FallbackServerBungee.getInstance(), () -> {

            System.out.println("Connessione in corso.. per " + player.getName());

            AtomicInteger tries = new AtomicInteger(0);
            int dots = 0;

            dots++;

            if (dots == 4) {
                dots = 0;
            }

            if (!player.isConnected()) {
                instance.cancelReconnect(uuid);
                return;
            }

            if (player.getServer().getInfo().equals(target)) {
                instance.cancelReconnect(uuid);
                return;
            }

            TitleUtil.sendReconnectingTitle(0,
                    1 + 20,
                    dots,
                    BungeeMessages.RECONNECT_TITLE,
                    BungeeMessages.RECONNECT_SUB_TITLE,
                    player);

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

                if (error != null) {
                    tries.getAndIncrement();
                    return;
                }

                player.connect(target);
                instance.cancelReconnect(uuid);
                tries.set(0);

            });

        }, 1, 1, TimeUnit.SECONDS);

    }

    public void clear() {

        task.cancel();

    }

}
