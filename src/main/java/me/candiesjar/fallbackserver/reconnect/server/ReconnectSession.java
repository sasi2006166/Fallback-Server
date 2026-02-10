package me.candiesjar.fallbackserver.reconnect.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.PlatformDependent;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.channel.BasicChannelInitializer;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.enums.TitleDisplayMode;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
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
import net.md_5.bungee.protocol.packet.Title;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReconnectSession {

    private final AtomicInteger dots = new AtomicInteger(0);

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = fallbackServerBungee.getProxy();
    private final ChatUtil chatUtil = fallbackServerBungee.getChatUtil();
    private final TitleUtil titleUtil = fallbackServerBungee.getTitleUtil();
    private final TaskScheduler taskScheduler = proxyServer.getScheduler();
    private final TextComponent lostConnection = new TextComponent(proxyServer.getTranslation("lost_connection"));
    private final boolean kick = BungeeConfig.RECONNECT_SORT.getBoolean();

    private final ServerConnection serverConnection;
    @Getter
    private final UserConnection userConnection;

    @Getter
    private final BungeeServerInfo targetServerInfo;
    private final UUID uuid;

    private final Random random = new Random();

    @Getter
    private ScheduledTask titleTask, connectTask, keepAliveTask;

    public ReconnectSession(UserConnection userConnection, ServerConnection serverConnection, UUID uuid) {
        this.serverConnection = serverConnection;
        this.userConnection = userConnection;
        this.targetServerInfo = serverConnection.getInfo();
        this.uuid = uuid;
    }

    public void onJoin() {
        TitleDisplayMode titleDisplayMode = TitleDisplayMode.fromString(BungeeConfig.RECONNECT_TITLE_MODE.getString());
        serverConnection.setObsolete(true);

        titleTask = scheduleTask(() -> sendTitles(BungeeMessages.RECONNECT_TITLE, BungeeMessages.RECONNECT_SUB_TITLE), 0, titleDisplayMode.getPeriod());
        keepAliveTask = scheduleTask(() -> userConnection.unsafe().sendPacket(new KeepAlive(random.nextLong())), 0, 10);
    }

    public void handleConnection() {
        titleTask.cancel();

        titleTask = scheduleTask(() -> sendTitles(BungeeMessages.CONNECTING_TITLE, BungeeMessages.CONNECTING_SUB_TITLE), 1, 1);
        connectTask = scheduleTask(this::runBootstrap, BungeeConfig.RECONNECT_CONNECTION_DELAY.getInt(), 0);
    }

    public void handleFallback(boolean kick) {
        if (kick) {
            userConnection.disconnect(lostConnection);
            return;
        }

        userConnection.getPendingConnects().remove(targetServerInfo);
        ReconnectUtil.cancelReconnect(uuid);

        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, null, lostConnection, null, ServerKickEvent.State.CONNECTED));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connect(serverKickEvent.getCancelServer());
        }

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[RECONNECT] Player " + userConnection.getName() + " failed to reconnect to " + targetServerInfo.getName(), true);
        }
    }

    public void resetTitle() {
        Title title = new Title(Title.Action.RESET);
        userConnection.unsafe().sendPacket(title);
    }

    private void runBootstrap() {
        ChannelInitializer<Channel> initializer = new BasicChannelInitializer(proxyServer, userConnection, targetServerInfo, false);
        Bootstrap bootstrap = new Bootstrap()
                .channel(PipelineUtils.getChannel(targetServerInfo.getAddress()))
                .group(serverConnection.getCh().getHandle().eventLoop())
                .handler(initializer)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, BungeeConfig.RECONNECT_PING_THRESHOLD.getInt())
                .remoteAddress(targetServerInfo.getAddress());

        if (userConnection.getPendingConnection().getListener().isSetLocalAddress() && userConnection.getPendingConnection().getListener().getSocketAddress() instanceof InetSocketAddress && !PlatformDependent.isWindows()) {
            bootstrap.localAddress(((InetSocketAddress) userConnection.getPendingConnection().getListener().getSocketAddress()).getHostString(), 0);
        }

        bootstrap.connect().addListener(channelFuture -> {
            if (!channelFuture.isSuccess()) {
                handleFallback(kick);
                return;
            }

            pingServer(targetServerInfo, (result, error) -> {
                if (error != null || result == null) {
                    Utils.printDebug("[RECONNECT] Failed to ping server during connect phase: " + targetServerInfo.getName(), true);
                    handleFallback(kick);
                }
            });

            if (fallbackServerBungee.isDebug()) {
                Utils.printDebug("Reconnected player " + userConnection.getName() + " to " + targetServerInfo.getName(), true);
            }

            ErrorHandler.add(Severity.INFO, "[RECONNECT] Successfully reconnected player " + userConnection.getName() + " to " + targetServerInfo.getName());

            userConnection.getPendingConnects().remove(targetServerInfo);
            ReconnectUtil.cancelReconnect(uuid);
            sendConnectedTitle();

            if (BungeeConfig.CLEAR_CHAT_RECONNECT.getBoolean()) {
                chatUtil.clearChat(userConnection);
            }
        });
    }

    private void sendConnectedTitle() {
        int fadeIn = BungeeMessages.CONNECTED_FADE_IN.getInt();
        int stay = BungeeMessages.CONNECTED_STAY.getInt();
        int fadeOut = BungeeMessages.CONNECTED_FADE_OUT.getInt();
        int delay = BungeeMessages.CONNECTED_DELAY.getInt();

        taskScheduler.schedule(fallbackServerBungee,
                () -> titleUtil.sendTitle(fadeIn, stay, fadeOut, BungeeMessages.CONNECTED_TITLE, BungeeMessages.CONNECTED_SUB_TITLE, targetServerInfo, userConnection),
                delay, TimeUnit.SECONDS);
    }

    private void pingServer(BungeeServerInfo target, Callback<Boolean> callback) {
        ChannelInitializer<Channel> initializer = new BasicChannelInitializer(proxyServer, userConnection, targetServerInfo, true);
        Bootstrap bootstrap = new Bootstrap().channel(PipelineUtils.getChannel(target.getAddress())).group(serverConnection.getCh().getHandle().eventLoop()).handler(initializer).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, BungeeConfig.RECONNECT_PING_THRESHOLD.getInt()).remoteAddress(target.getAddress());
        bootstrap.connect().addListener(future -> callback.done(future.isSuccess(), future.cause()));
    }

    private void sendTitles(BungeeMessages title, BungeeMessages subTitle) {
        int currentDots = dots.incrementAndGet() % 5;

        TitleDisplayMode titleDisplayMode = TitleDisplayMode.fromString(BungeeConfig.RECONNECT_TITLE_MODE.getString());

        switch (titleDisplayMode) {
            case NORMAL:
                titleUtil.sendReconnectingTitle(0, 1 + 20, currentDots, title, subTitle, userConnection);
                break;
            case STATIC:
                titleUtil.sendTitle(0, 1 + 20, 0, title, subTitle, targetServerInfo, userConnection);
                break;
            case PULSE:
                titleUtil.sendTitle(1, 1 + 20, BungeeMessages.RECONNECT_TITLE_PULSE.getInt(), title, subTitle, targetServerInfo, userConnection);
                break;
        }
    }

    private ScheduledTask scheduleTask(Runnable task, int initialDelay, int period) {
        return taskScheduler.schedule(fallbackServerBungee, task, initialDelay, period, TimeUnit.SECONDS);
    }
}
