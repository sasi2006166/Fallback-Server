package me.candiesjar.fallbackserver.handlers;

import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.channel.BasicChannelInitializer;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.utils.ServerUtils;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.netty.PipelineUtils;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReconnectHandler {

    private final String LOST_CONNECTION = ProxyServer.getInstance().getTranslation("lost_connection");

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();
    private final AtomicInteger dots = new AtomicInteger(0);

    private final ProxiedPlayer player;
    private final ServerConnection serverConnection;
    private final UserConnection userConnection;
    private final BungeeServerInfo targetServerInfo;
    private final UUID uuid;

    @Getter
    private ScheduledTask reconnectTask;
    @Getter
    private ScheduledTask titleTask;
    @Getter
    private ScheduledTask connectTask;

    public ReconnectHandler(ProxiedPlayer player, ServerConnection serverConnection, UUID uuid) {
        this.player = player;
        this.serverConnection = serverConnection;
        this.userConnection = (UserConnection) player;
        this.targetServerInfo = serverConnection.getInfo();
        this.uuid = uuid;
    }

    public void start() {
        titleTask = proxyServer.getScheduler().schedule(fallbackServerBungee, () -> sendTitle(player, BungeeMessages.RECONNECT_TITLE, BungeeMessages.RECONNECT_SUB_TITLE, dots), 0, 1, TimeUnit.SECONDS);
        AtomicInteger tries = new AtomicInteger(0);
        reconnectTask = proxyServer.getScheduler().schedule(fallbackServerBungee, () -> reconnect(tries), 0, BungeeConfig.RECONNECT_DELAY.getInt(), TimeUnit.SECONDS);
    }

    private void reconnect(AtomicInteger tries) {

        boolean maxTries = tries.incrementAndGet() == BungeeConfig.RECONNECT_TRIES.getInt();

        if (maxTries) {
            BungeeMessages.CONNECTION_FAILED.send(player);
            boolean fallback = BungeeConfig.RECONNECT_SORT.getBoolean();

            if (fallback) {
                handleFallback();
                return;
            }

            fallbackServerBungee.cancelReconnect(player.getUniqueId());
            player.disconnect(new TextComponent(LOST_CONNECTION));
            return;
        }

        pingServer(targetServerInfo, (result, error) -> {
            if (error != null || result == null) {
                return;
            }

            titleTask.cancel();
            resetDots();
            reconnectTask.cancel();
            clear();

            titleTask = proxyServer.getScheduler().schedule(fallbackServerBungee, () -> sendTitle(player, BungeeMessages.CONNECTING_TITLE, BungeeMessages.CONNECTING_SUB_TITLE, dots), 0, 1, TimeUnit.SECONDS);
            connectTask = proxyServer.getScheduler().schedule(fallbackServerBungee, this::handleConnection, BungeeConfig.RECONNECT_CONNECTION_DELAY.getInt(), TimeUnit.SECONDS);
        });

    }

    private void resetDots() {
        dots.set(0);
    }

    private void handleConnection() {
        titleTask.cancel();
        clear();

        int fadeIn = BungeeMessages.CONNECTED_FADE_IN.getInt();
        int stay = BungeeMessages.CONNECTED_STAY.getInt();
        int fadeOut = BungeeMessages.CONNECTED_FADE_OUT.getInt();
        int delay = BungeeMessages.CONNECTED_DELAY.getInt();
        int timeout = BungeeConfig.RECONNECT_PING_THRESHOLD.getInt();

        ChannelInitializer<Channel> initializer = new BasicChannelInitializer(proxyServer, userConnection, targetServerInfo);
        ChannelFutureListener listener = channelFuture -> proxyServer.getScheduler().schedule(fallbackServerBungee, () -> TitleUtil.sendTitle(fadeIn, stay, fadeOut, BungeeMessages.CONNECTED_TITLE, BungeeMessages.CONNECTED_SUB_TITLE, targetServerInfo, player), delay, TimeUnit.SECONDS);

        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(targetServerInfo.getAddress())).group(serverConnection.getCh().getHandle().eventLoop()).handler(initializer).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout).remoteAddress(targetServerInfo.getAddress());
        bootstrap.connect().addListener(listener);

        boolean clearChat = BungeeConfig.CLEAR_CHAT_RECONNECT.getBoolean();

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        pingServer(targetServerInfo, (result, error) -> {
            if (error != null || result == null) {

                if (!fallbackServerBungee.isReconnectError()) {
                    Utils.printDebug("Target reconnect server went offline.", true);
                    Utils.printDebug("Preventing player timeout enabling fallback mode", true);
                    Utils.printDebug("Moving player to fallback server..", true);
                    Utils.printDebug("Your spigot instances are likely unstable, please fix them.", true);
                }

                fallbackServerBungee.setReconnectError(true);
                if (!player.isConnected()) {
                    fallbackServerBungee.cancelReconnect(uuid);
                    return;
                }

                handleFallback();
            }
        });

        fallbackServerBungee.cancelReconnect(uuid);
    }

    private void handleFallback() {
        clear();
        FallingServer.removeServer(targetServerInfo);
        List<FallingServer> lobbies = Lists.newArrayList(FallingServer.getServers().values());

        boolean hasMaintenance = fallbackServerBungee.isMaintenance();

        if (hasMaintenance) {
            lobbies.removeIf(fallingServer -> ServerUtils.checkMaintenance(fallingServer.getServerInfo()));
        }

        if (lobbies.isEmpty()) {
            player.disconnect(new TextComponent(LOST_CONNECTION));
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getServerInfo().getPlayers().size()));

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        player.connect(serverInfo);

        proxyServer.getScheduler().schedule(fallbackServerBungee, () -> TitleUtil.sendTitle(BungeeMessages.FALLBACK_FADE_IN.getInt(),
                        BungeeMessages.FALLBACK_STAY.getInt(),
                        BungeeMessages.FALLBACK_FADE_OUT.getInt(),
                        BungeeMessages.FALLBACK_TITLE,
                        BungeeMessages.FALLBACK_SUB_TITLE,
                        serverInfo,
                        player),
                BungeeMessages.FALLBACK_DELAY.getInt(), 0, TimeUnit.SECONDS);

        fallbackServerBungee.cancelReconnect(uuid);
    }

    private void pingServer(BungeeServerInfo target, Callback<Boolean> callback) {
        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(target.getAddress())).group(serverConnection.getCh().getHandle().eventLoop()).handler(PipelineUtils.BASE).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).remoteAddress(target.getAddress());
        bootstrap.connect().addListener(future -> callback.done(future.isSuccess(), future.cause()));
    }

    private void sendTitle(ProxiedPlayer player, BungeeMessages title, BungeeMessages subTitle, AtomicInteger dots) {
        int maxDots = 4;

        if (dots.getAndIncrement() == maxDots) {
            dots.set(0);
        }

        TitleUtil.sendReconnectingTitle(0, 1 + 20, dots.get(), title, subTitle, player);
    }

    public void clear() {
        proxyServer.createTitle().reset().clear().send(player);
    }
}
