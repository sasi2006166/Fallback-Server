package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.ServerCacheManager;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.ServerUtils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class FallbackListener {
    private final FallbackServerVelocity fallbackServerVelocity;
    private final FallingServerManager fallingServerManager;
    private final ServerCacheManager serverCacheManager;
    private final Map<String, LongAdder> pendingConnections = new ConcurrentHashMap<>();

    public FallbackListener(FallbackServerVelocity fallbackServerVelocity) {
        this.fallbackServerVelocity = fallbackServerVelocity;
        this.fallingServerManager = fallbackServerVelocity.getFallingServerManager();
        this.serverCacheManager = fallbackServerVelocity.getServerCacheManager();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerKick(KickedFromServerEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        RegisteredServer kickedFrom = event.getServer();
        String serverName = kickedFrom.getServerInfo().getName();

        if (event.kickedDuringServerConnect()) {
            return;
        }

        Optional<Component> componentOptional = event.getServerKickReason();
        boolean isEmpty = componentOptional.isEmpty();
        String kickReasonString = isEmpty ? "" : ChatUtil.componentToString(componentOptional.get());
        List<String> ignoredReasons = VelocityConfig.IGNORED_REASONS.getStringList();

        for (String blacklist : ignoredReasons) {
            if (isEmpty) {
                break;
            }

            if (PlainTextComponentSerializer.plainText().serialize(componentOptional.get()).contains(blacklist)) {
                continuation.resume();
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(ChatUtil.color(kickReasonString))));
                return;
            }
        }

        if (shouldUseBlacklistedServer(serverName)) {
            continuation.resume();
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(ChatUtil.color(kickReasonString))));
            return;
        }

        fallingServerManager.remove(serverName);
        serverCacheManager.removeIfContains(serverName);

        List<RegisteredServer> lobbies = Lists.newArrayList(fallingServerManager.getAll());

        boolean useMaintenance = fallbackServerVelocity.isUseMaintenance();

        if (useMaintenance) {
            lobbies.removeIf(ServerUtils::isMaintenance);
        }

        if (lobbies.isEmpty()) {
            if (kickReasonString.isEmpty()) {
                continuation.resume();
                String disconnectMessage = VelocityMessages.NO_SERVER.get(String.class).replace("%prefix%", ChatUtil.getFormattedString(VelocityMessages.PREFIX));
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(ChatUtil.color(disconnectMessage))));
                return;
            }
            continuation.resume();
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(ChatUtil.color(kickReasonString))));
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getPlayersConnected().size() + getPendingConnections(server.getServerInfo().getName())));

        RegisteredServer selectedServer = lobbies.get(0);

        player.createConnectionRequest(selectedServer).fireAndForget();

        incrementPendingConnections(selectedServer.getServerInfo().getName());
        fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> decrementPendingConnections(selectedServer.getServerInfo().getName()))
                .delay(1, TimeUnit.SECONDS)
                .schedule();

        VelocityMessages.KICKED_TO_LOBBY.sendList(player,
                new Placeholder("server", selectedServer.getServerInfo().getName()),
                new Placeholder("reason", ChatUtil.color(kickReasonString))
        );

        boolean useTitle = VelocityMessages.USE_FALLBACK_TITLE.get(Boolean.class);

        if (useTitle) {
            fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () ->
                            TitleUtil.sendTitle(
                                    VelocityMessages.FALLBACK_FADE_IN.get(Integer.class),
                                    VelocityMessages.FALLBACK_STAY.get(Integer.class),
                                    VelocityMessages.FALLBACK_FADE_OUT.get(Integer.class),
                                    ChatUtil.color(VelocityMessages.FALLBACK_TITLE.get(String.class)),
                                    ChatUtil.color(VelocityMessages.FALLBACK_SUB_TITLE.get(String.class)),
                                    player
                            )).delay(VelocityMessages.FALLBACK_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }

        boolean clearChat = VelocityConfig.CLEAR_CHAT_FALLBACK.get(Boolean.class);

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

    }

    private boolean shouldUseBlacklistedServer(String serverName) {
        return VelocityConfig.USE_BLACKLISTED_SERVERS.get(Boolean.class) && VelocityConfig.BLACKLISTED_SERVERS_LIST.getStringList().contains(serverName);
    }

    private int getPendingConnections(String serverName) {
        return pendingConnections.getOrDefault(serverName, new LongAdder()).intValue();
    }

    private void incrementPendingConnections(String serverName) {
        pendingConnections.computeIfAbsent(serverName, key -> new LongAdder()).increment();
    }

    private void decrementPendingConnections(String serverName) {
        LongAdder adder = pendingConnections.get(serverName);
        if (adder != null) {
            adder.decrement();
        }
    }
}
