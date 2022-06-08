package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.api.VelocityAPI;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FallbackListener {

    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    @Subscribe
    public void onPlayerKick(final KickedFromServerEvent event) {

        final Player player = event.getPlayer();
        final RegisteredServer kickedFrom = event.getServer();

        if (!player.isActive()) {
            return;
        }

        if (event.kickedDuringServerConnect()) {
            return;
        }

        for (String blacklist : VelocityConfig.BLACKLISTED_WORDS.getStringList()) {

            if (!event.getServerKickReason().isPresent()) {
                continue;
            }

            if (PlainTextComponentSerializer.plainText().serialize(event.getServerKickReason().get()).contains(blacklist)) {
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(event.getServerKickReason().get()));
                return;
            }

        }

        if (VelocityConfig.USE_BLACKLISTED_SERVERS.get(Boolean.class)) {
            for (String blacklist : VelocityConfig.BLACKLISTED_SERVERS_LIST.getStringList()) {
                if (blacklist.contains(kickedFrom.getServerInfo().getName())) {
                    return;
                }
            }
        }

        final Map<RegisteredServer, FallingServer> clonedMap = new HashMap<>(FallingServer.getServers());

        clonedMap.remove(kickedFrom);

        final LinkedList<FallingServer> lobbies = new LinkedList<>(clonedMap.values());

        if (lobbies.size() == 0) {
            if (!event.getServerKickReason().isPresent()) {
                player.disconnect(VelocityMessages.color(VelocityMessages.NO_SERVER.get(String.class)
                        .replace("%prefix%", VelocityMessages.PREFIX.color())));
            }
            return;
        }

        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        final RegisteredServer registeredServer = lobbies.get(0).getRegisteredServer();

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer));

        VelocityMessages.KICKED_TO_LOBBY.sendList(player,
                new PlaceHolder("server", registeredServer.getServerInfo().getName()),
                new PlaceHolder("reason", ChatUtil.color(PlainTextComponentSerializer.plainText().serialize(event.getServerKickReason().get()))));

        if (VelocityMessages.USE_FALLBACK_TITLE.get(Boolean.class)) {
            instance.getServer().getScheduler().buildTask(instance, () -> TitleUtil.sendTitle(VelocityMessages.FALLBACK_FADE_IN.get(Integer.class),
                            VelocityMessages.FALLBACK_STAY.get(Integer.class),
                            VelocityMessages.FALLBACK_FADE_OUT.get(Integer.class),
                            ChatUtil.color(VelocityMessages.FALLBACK_TITLE.get(String.class)),
                            ChatUtil.color(VelocityMessages.FALLBACK_SUB_TITLE.get(String.class)),
                            player))
                    .delay(VelocityMessages.FALLBACK_TITLE_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }

        instance.getServer().getScheduler().buildTask(instance, () -> instance.getServer().getEventManager().fire(new VelocityAPI(player, kickedFrom, registeredServer, PlainTextComponentSerializer.plainText().serialize(event.getServerKickReason().get()))));

    }
}
