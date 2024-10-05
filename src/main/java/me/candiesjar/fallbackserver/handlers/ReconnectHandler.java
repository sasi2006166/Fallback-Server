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

public class ReconnectHandler {

    private final String LOST_CONNECTION = ProxyServer.getInstance().getTranslation("lost_connection");
    private final AtomicInteger MAX_TRIES = new AtomicInteger(BungeeConfig.RECONNECT_TRIES.getInt());
    private final AtomicInteger DOTS = new AtomicInteger(0);
    private final AtomicInteger TRIES = new AtomicInteger(0);

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();
    private final TaskScheduler taskScheduler = proxyServer.getScheduler();

    private final ServerConnection serverConnection;
    private final UserConnection userConnection;
    private final BungeeServerInfo targetServerInfo;
    private final UUID uuid;

    @Getter
    private ScheduledTask reconnectTask, titleTask, connectTask;

    public ReconnectHandler(UserConnection userConnection, ServerConnection serverConnection, UUID uuid) {
        this.serverConnection = serverConnection;
        this.userConnection = userConnection;
        this.targetServerInfo = serverConnection.getInfo();
        this.uuid = uuid;
    }

    public void start() {
        String titleMode = BungeeConfig.RECONNECT_TITLE_MODE.getString();

        switch (titleMode) {
            case "NORMAL":
            case "STATIC":
            default:
                titleTask = scheduleTask(() -> sendTitles(BungeeMessages.RECONNECT_TITLE, BungeeMessages.RECONNECT_SUB_TITLE), 0, 1);
                break;
            case "PULSE":
                titleTask = scheduleTask(() -> sendTitles(BungeeMessages.RECONNECT_TITLE, BungeeMessages.RECONNECT_SUB_TITLE), 0, 3);
                break;
        }

        reconnectTask = scheduleTask(this::reconnect, BungeeConfig.RECONNECT_TASK_DELAY.getInt(), BungeeConfig.RECONNECT_DELAY.getInt());
    }

    private void reconnect() {
        boolean maxTries = TRIES.incrementAndGet() == MAX_TRIES.get();

        if (maxTries) {
            boolean fallback = BungeeConfig.RECONNECT_SORT.getBoolean();

            if (fallback) {
                handleFallback();
                return;
            }

            ReconnectUtil.cancelReconnect(uuid);
            userConnection.disconnect(new TextComponent(LOST_CONNECTION));
            return;
        }

        targetServerInfo.ping(((result, error) -> {
            if (error != null || result == null) {
                return;
            }

            int maxPlayers = result.getPlayers().getMax();
            int connectedPlayers = result.getPlayers().getOnline();

            if (connectedPlayers == maxPlayers) {
                TRIES.set(MAX_TRIES.get());
                return;
            }

            int check = BungeeConfig.RECONNECT_PLAYER_COUNT_CHECK.getInt();

            if (maxPlayers != check) {
                return;
            }

            titleTask.cancel();
            reconnectTask.cancel();

            clear();

            titleTask = scheduleTask(() -> sendTitles(BungeeMessages.CONNECTING_TITLE, BungeeMessages.CONNECTING_SUB_TITLE), 0, 1);
            connectTask = taskScheduler.schedule(fallbackServerBungee, this::handleConnection, BungeeConfig.RECONNECT_CONNECTION_DELAY.getInt(), TimeUnit.SECONDS);

        }));
    }

    private void handleConnection() {
        titleTask.cancel();
        clear();

        ChannelInitializer<Channel> initializer = new BasicChannelInitializer(proxyServer, userConnection, targetServerInfo);
        Bootstrap bootstrap = new Bootstrap()
                .channel(PipelineUtils.getChannel(targetServerInfo.getAddress()))
                .group(Utils.getUserChannelWrapper(userConnection).getHandle().eventLoop())
                .handler(initializer)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .remoteAddress(targetServerInfo.getAddress());

        if (userConnection.getPendingConnection().getListener().isSetLocalAddress() && !PlatformDependent.isWindows()) {
            bootstrap.localAddress(((InetSocketAddress) userConnection.getPendingConnection().getListener().getSocketAddress()).getHostString(), 0);
        }

        userConnection.unsafe().sendPacket(new KeepAlive(new Random().nextInt()));

        bootstrap.connect().addListener(channelFuture -> ReconnectUtil.cancelReconnect(uuid));

        pingServer(targetServerInfo, (result, error) -> {
            if (error != null || result == null) {
                handleFallback();
            }
        });

        if (BungeeConfig.CLEAR_CHAT_RECONNECT.getBoolean()) {
            ChatUtil.clearChat(userConnection);
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
        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(target.getAddress())).group(serverConnection.getCh().getHandle().eventLoop()).handler(PipelineUtils.BASE).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).remoteAddress(target.getAddress());
        bootstrap.connect().addListener(future -> callback.done(future.isSuccess(), future.cause()));
    }

    private void handleFallback() {
        ReconnectUtil.cancelReconnect(uuid);

        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, null, TextComponent.fromLegacy(LOST_CONNECTION), null, ServerKickEvent.State.CONNECTED));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connect(serverKickEvent.getCancelServer());
        }
    }

    private void sendTitles(BungeeMessages title, BungeeMessages subTitle) {
        int maxDots = 4;

        if (DOTS.getAndIncrement() == maxDots) {
            DOTS.set(0);
        }

        String titleMode = BungeeConfig.RECONNECT_TITLE_MODE.getString();

        switch (titleMode) {
            default:
            case "NORMAL":
                TitleUtil.sendReconnectingTitle(0, 1 + 20, DOTS.get(), title, subTitle, userConnection);
                break;
            case "STATIC":
                TitleUtil.sendTitle(0, 1 + 20, 0, title, subTitle, targetServerInfo, userConnection);
                break;
            case "PULSE":
                TitleUtil.sendTitle(1, 1 + 20, BungeeMessages.RECONNECT_TITLE_BEAT.getInt(), title, subTitle, targetServerInfo, userConnection);
                break;
        }
    }

    private ScheduledTask scheduleTask(Runnable task, int initialDelay, int delay) {
        return taskScheduler.schedule(fallbackServerBungee, task, initialDelay, delay, TimeUnit.SECONDS);
    }

    public void clear() {
        proxyServer.createTitle()
                .reset()
                .clear()
                .send(userConnection);
    }
}
