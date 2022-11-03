package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.api.VelocityAPI;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.PlaceHolder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class FallbackListener {
    private final FallbackServerVelocity fallbackServerVelocity;
    private final FallingServerManager fallingServerManager;

    public FallbackListener(FallbackServerVelocity fallbackServerVelocity) {
        this.fallbackServerVelocity = fallbackServerVelocity;
        this.fallingServerManager = fallbackServerVelocity.getFallingServerManager();
    }

    @Subscribe
    public void onPlayerKick(KickedFromServerEvent event) {

        final Player player = event.getPlayer();
        final RegisteredServer kickedFrom = event.getServer();
        final String serverName = kickedFrom.getServerInfo().getName();

        if (!player.isActive() || event.kickedDuringServerConnect()) {
            return;
        }

        final Optional<Component> componentOptional = event.getServerKickReason();
        final boolean isEmpty = !componentOptional.isPresent();

        final String kickReasonString = isEmpty ? "" : ChatUtil.componentToString(componentOptional.get());

        for (String blacklist : VelocityConfig.IGNORED_REASONS.getStringList()) {

            if (isEmpty) {
                break;
            }

            if (PlainTextComponentSerializer.plainText().serialize(componentOptional.get()).contains(blacklist)) {
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(componentOptional.get()));
                return;
            }

        }

        if (shouldUseBlacklistedServer(serverName)) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(kickReasonString)));
            return;
        }

        fallingServerManager.remove(serverName);

        final LinkedList<RegisteredServer> lobbies = Lists.newLinkedList(fallingServerManager.getAll());

        if (lobbies.size() == 0) {
            if (!kickReasonString.isEmpty()) {
                String disconnectMessage = VelocityMessages.NO_SERVER.get(String.class).replace("%prefix%", VelocityMessages.PREFIX.color());
                player.disconnect(Component.text(ChatUtil.color(disconnectMessage)));
            }
            return;
        }

        lobbies.sort(Comparator.comparingInt(o -> o.getPlayersConnected().size()));

        final RegisteredServer registeredServer = lobbies.get(0);

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer));

        VelocityMessages.KICKED_TO_LOBBY.sendList(player,
                new PlaceHolder("server", registeredServer.getServerInfo().getName()),
                new PlaceHolder("reason", ChatUtil.color(kickReasonString))
        );

        if (VelocityMessages.USE_FALLBACK_TITLE.get(Boolean.class)) {
            fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> TitleUtil.sendTitle(VelocityMessages.FALLBACK_FADE_IN.get(Integer.class),
                            VelocityMessages.FALLBACK_STAY.get(Integer.class),
                            VelocityMessages.FALLBACK_FADE_OUT.get(Integer.class),
                            ChatUtil.color(VelocityMessages.FALLBACK_TITLE.get(String.class)),
                            ChatUtil.color(VelocityMessages.FALLBACK_SUB_TITLE.get(String.class)),
                            player)).delay(VelocityMessages.FALLBACK_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }

        fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> fallbackServerVelocity.getServer().getEventManager().fire(new VelocityAPI(player, kickedFrom, registeredServer, kickReasonString)));

    }

    private boolean shouldUseBlacklistedServer(String serverName) {
        return VelocityConfig.USE_BLACKLISTED_SERVERS.get(Boolean.class) && VelocityConfig.BLACKLISTED_SERVERS_LIST.getStringList().contains(serverName);
    }
}
