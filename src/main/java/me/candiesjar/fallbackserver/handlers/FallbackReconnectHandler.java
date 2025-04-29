package me.candiesjar.fallbackserver.handlers;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.PlatformDependent;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.channel.BasicChannelInitializer;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.enums.TitleMode;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.packet.KeepAlive;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FallbackReconnectHandler {

    private final AtomicInteger maxTries = new AtomicInteger(BungeeConfig.RECONNECT_TRIES.getInt());
    private final AtomicInteger dots = new AtomicInteger(0);
    private final AtomicInteger tries = new AtomicInteger(0);

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();
    private final TaskScheduler taskScheduler = proxyServer.getScheduler();
    private final TextComponent lostConnection = new TextComponent(proxyServer.getTranslation("lost_connection"));
    private final Random random = new Random();

    private final ServerConnection serverConnection;
    private final UserConnection userConnection;
    private final BungeeServerInfo targetServerInfo;
    private final UUID uuid;

    @Getter
    private ScheduledTask reconnectTask, titleTask, connectTask;

    public FallbackReconnectHandler(UserConnection userConnection, ServerConnection serverConnection, UUID uuid) {
        this.serverConnection = serverConnection;
        this.userConnection = userConnection;
        this.targetServerInfo = serverConnection.getInfo();
        this.uuid = uuid;
    }

    public void onJoin() {
        TitleMode titleMode = TitleMode.fromString(BungeeConfig.RECONNECT_TITLE_MODE.getString());

        titleTask = scheduleTask(() -> sendTitles(BungeeMessages.RECONNECT_TITLE, BungeeMessages.RECONNECT_SUB_TITLE), 0, titleMode.getPeriod());
        reconnectTask = scheduleTask(this::startReconnect, 0, BungeeConfig.RECONNECT_DELAY.getInt());
    }

    private void startReconnect() {
        boolean maxTries = tries.incrementAndGet() == this.maxTries.get();

        if (maxTries) {
            boolean fallback = BungeeConfig.RECONNECT_SORT.getBoolean();

            if (fallback) {
                handleFallback();
                return;
            }

            ReconnectUtil.cancelReconnect(uuid);
            userConnection.disconnect(lostConnection);
            return;
        }

        targetServerInfo.ping(((result, error) -> {
            if (error != null || result == null) {
                return;
            }

            int maxPlayers = result.getPlayers().getMax();
            int connectedPlayers = result.getPlayers().getOnline();
            int check = BungeeConfig.RECONNECT_PLAYER_COUNT_CHECK.getInt();

            if (connectedPlayers == maxPlayers) {
                tries.set(this.maxTries.get());
                return;
            }

            if (maxPlayers != check) {
                return;
            }

            titleTask.cancel();
            reconnectTask.cancel();

            titleTask = scheduleTask(() -> sendTitles(BungeeMessages.CONNECTING_TITLE, BungeeMessages.CONNECTING_SUB_TITLE), 1, 1);
            connectTask = scheduleTask(this::handleConnection, BungeeConfig.RECONNECT_CONNECTION_DELAY.getInt(), 0);
        }));
    }

    private void handleConnection() {
        titleTask.cancel();
        TitleUtil.clearPlayerTitle(userConnection);

        if (userConnection.getServer() != null) {
            userConnection.getServer().disconnect("Reconnecting...");
        }

        ChannelInitializer<Channel> initializer = new BasicChannelInitializer(proxyServer, userConnection, targetServerInfo, false);
        Bootstrap bootstrap = new Bootstrap()
                .channel(PipelineUtils.getChannel(targetServerInfo.getAddress()))
                .group(serverConnection.getCh().getHandle().eventLoop())
                .handler(initializer)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1500)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(targetServerInfo.getAddress());

        if (userConnection.getPendingConnection().getListener().isSetLocalAddress() && userConnection.getPendingConnection().getListener().getSocketAddress() instanceof InetSocketAddress && !PlatformDependent.isWindows()) {
            bootstrap.localAddress(((InetSocketAddress) userConnection.getPendingConnection().getListener().getSocketAddress()).getHostString(), 0);
        }

        pingServer(targetServerInfo, (result, error) -> {
            if (error != null || result == null) {
                ErrorHandler.add(Severity.ERROR, "[RECONNECT] Failed to ping server during connect phase: " + targetServerInfo.getName());
                handleFallback();
            }
        });

        bootstrap.connect().addListener(channelFuture -> {
            userConnection.unsafe().sendPacket(new KeepAlive(random.nextLong()));
            ReconnectUtil.cancelReconnect(uuid);
        });

        if (BungeeConfig.CLEAR_CHAT_RECONNECT.getBoolean()) {
            ChatUtil.clearChat(userConnection);
        }

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("Reconnected to " + targetServerInfo.getName() + " with " + tries.get() + " tries", true);
        }

        sendConnectedTitle();
    }

    private void sendConnectedTitle() {
        int fadeIn = BungeeMessages.CONNECTED_FADE_IN.getInt();
        int stay = BungeeMessages.CONNECTED_STAY.getInt();
        int fadeOut = BungeeMessages.CONNECTED_FADE_OUT.getInt();
        int delay = BungeeMessages.CONNECTED_DELAY.getInt();

        taskScheduler.schedule(fallbackServerBungee,
                () -> TitleUtil.sendTitle(fadeIn, stay, fadeOut, BungeeMessages.CONNECTED_TITLE, BungeeMessages.CONNECTED_SUB_TITLE, targetServerInfo, userConnection),
                delay, TimeUnit.SECONDS);
    }

    private void pingServer(BungeeServerInfo target, Callback<Boolean> callback) {
        ChannelInitializer<Channel> initializer = new BasicChannelInitializer(proxyServer, userConnection, targetServerInfo, true);
        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(target.getAddress())).group(serverConnection.getCh().getHandle().eventLoop()).handler(initializer).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).remoteAddress(target.getAddress());
        bootstrap.connect().addListener(future -> callback.done(future.isSuccess(), future.cause()));
    }

    private void handleFallback() {
        TitleUtil.clearPlayerTitle(userConnection);
        ReconnectUtil.cancelReconnect(uuid);

        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, null, lostConnection, null, ServerKickEvent.State.CONNECTED));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connect(serverKickEvent.getCancelServer());
        }
    }

    private void sendTitles(BungeeMessages title, BungeeMessages subTitle) {
        int currentDots = dots.incrementAndGet() % 5;

        TitleMode titleMode = TitleMode.fromString(BungeeConfig.RECONNECT_TITLE_MODE.getString());

        switch (titleMode) {
            case NORMAL:
                TitleUtil.sendReconnectingTitle(0, 1 + 20, currentDots, title, subTitle, userConnection);
                break;
            case STATIC:
                TitleUtil.sendTitle(0, 1 + 20, 0, title, subTitle, targetServerInfo, userConnection);
                break;
            case PULSE:
                TitleUtil.sendTitle(1, 1 + 20, BungeeMessages.RECONNECT_TITLE_BEAT.getInt(), title, subTitle, targetServerInfo, userConnection);
                break;
        }
    }

    private ScheduledTask scheduleTask(Runnable task, int initialDelay, int period) {
        return taskScheduler.schedule(fallbackServerBungee, task, initialDelay, period, TimeUnit.SECONDS);
    }
}
