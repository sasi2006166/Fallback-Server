package me.candiesjar.fallbackserver.handlers;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.PlatformDependent;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.channel.BasicChannelInitializer;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.events.FallbackEvent;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.netty.PipelineUtils;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReconnectHandler {

    private final String LOST_CONNECTION = ProxyServer.getInstance().getTranslation("lost_connection");
    private final AtomicInteger DOTS = new AtomicInteger(0);
    private final AtomicInteger TRIES = new AtomicInteger(0);

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();
    private final TaskScheduler taskScheduler = proxyServer.getScheduler();

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
        titleTask = taskScheduler.schedule(fallbackServerBungee, () -> sendTitle(BungeeMessages.RECONNECT_TITLE, BungeeMessages.RECONNECT_SUB_TITLE), 0, 1, TimeUnit.SECONDS);

        int reconnectDelay = BungeeConfig.RECONNECT_DELAY.getInt();
        int taskDelay = BungeeConfig.RECONNECT_TASK_DELAY.getInt();

        reconnectTask = taskScheduler.schedule(fallbackServerBungee, this::reconnect, taskDelay, reconnectDelay, TimeUnit.SECONDS);
    }

    private void reconnect() {

        boolean maxTries = TRIES.incrementAndGet() == BungeeConfig.RECONNECT_TRIES.getInt();
        Utils.printDebug("Reconnect tries: " + TRIES.get(), true);

        if (maxTries) {
            boolean fallback = BungeeConfig.RECONNECT_SORT.getBoolean();

            if (fallback) {
                handleFallback();
                return;
            }

            fallbackServerBungee.cancelReconnect(player.getUniqueId());
            player.disconnect(new TextComponent(LOST_CONNECTION));
            return;
        }

        targetServerInfo.ping(((result, error) -> {

            if (error != null || result == null) {
                return;
            }

            int maxPlayers = result.getPlayers().getMax();
            int check = BungeeConfig.RECONNECT_PLAYER_COUNT_CHECK.getInt();

            if (maxPlayers == check) {
                titleTask.cancel();
                resetDots();
                reconnectTask.cancel();
                clear();

                titleTask = taskScheduler.schedule(fallbackServerBungee, () -> sendTitle(BungeeMessages.CONNECTING_TITLE, BungeeMessages.CONNECTING_SUB_TITLE), 0, 1, TimeUnit.SECONDS);
                connectTask = taskScheduler.schedule(fallbackServerBungee, this::handleConnection, BungeeConfig.RECONNECT_CONNECTION_DELAY.getInt(), TimeUnit.SECONDS);
            }

        }));

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

        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(targetServerInfo.getAddress())).group(Utils.getUserChannelWrapper(userConnection).getHandle().eventLoop()).handler(initializer).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout).remoteAddress(targetServerInfo.getAddress());

        if (userConnection.getPendingConnection().getListener().isSetLocalAddress() && !PlatformDependent.isWindows()) {
            bootstrap.localAddress(((InetSocketAddress) userConnection.getPendingConnection().getListener().getSocketAddress()).getHostString(), 0);
        }

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

                handleFallback();
            }

        });

        fallbackServerBungee.cancelReconnect(uuid);
    }

    private void handleFallback() {
        fallbackServerBungee.cancelReconnect(uuid);
        clear();

        FallbackEvent fallbackEvent = new FallbackEvent(player, targetServerInfo, "ReconnectHandler");
        proxyServer.getPluginManager().callEvent(fallbackEvent);
    }

    private void pingServer(BungeeServerInfo target, Callback<Boolean> callback) {
        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(target.getAddress())).group(serverConnection.getCh().getHandle().eventLoop()).handler(PipelineUtils.BASE).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).remoteAddress(target.getAddress());
        bootstrap.connect().addListener(future -> callback.done(future.isSuccess(), future.cause()));
    }

    private void sendTitle(BungeeMessages title, BungeeMessages subTitle) {
        int maxDots = 4;

        if (DOTS.getAndIncrement() == maxDots) {
            DOTS.set(0);
        }

        TitleUtil.sendReconnectingTitle(0, 1 + 20, DOTS.get(), title, subTitle, player);
    }

    private void resetDots() {
        DOTS.set(0);
    }

    public void clear() {
        proxyServer.createTitle().reset().clear().send(player);
    }
}
