package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.api.VelocityAPI;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.candiesjar.fallbackserver.enums.VelocityConfig.BLACKLISTED_WORDS;
import static me.candiesjar.fallbackserver.enums.VelocityMessages.*;

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

        for (String blacklist : BLACKLISTED_WORDS.getStringList()) {

            if (!event.getServerKickReason().isPresent()) {
                continue;
            }

            if (event.getServerKickReason().get().asComponent().contains(Component.text(blacklist))) {
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(event.getServerKickReason().get()));
                return;
            }

        }

        if (VelocityConfig.BLACKLISTED_SERVERS.get(Boolean.class)) {
            for (String blacklist : VelocityConfig.BLACKLISTED_SERVERS.getStringList()) {
                if (blacklist.contains(kickedFrom.getServerInfo().getName())) {
                    return;
                }
            }
        }

        final Map<RegisteredServer, FallingServer> clonedMap = new HashMap<>(FallingServer.getServers());

        clonedMap.remove(kickedFrom);

        final LinkedList<FallingServer> lobbies = new LinkedList<>(clonedMap.values());
        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        if (lobbies.size() == 0) {
            if (!event.getServerKickReason().isPresent()) {
                player.disconnect(color(NO_SERVER.get(String.class)
                        .replace("%prefix%", PREFIX.color())));
            }
            return;
        }

        final RegisteredServer registeredServer = lobbies.get(0).getRegisteredServer();

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer));

        if (USE_FALLBACK_TITLE.get(Boolean.class)) {
            instance.getServer().getScheduler().buildTask(instance, () -> TitleUtil.sendTitle(FALLBACK_FADE_IN.get(Integer.class),
                            FALLBACK_STAY.get(Integer.class),
                            FALLBACK_FADE_OUT.get(Integer.class),
                            ChatUtil.color(FALLBACK_TITLE.get(String.class)),
                            ChatUtil.color(FALLBACK_SUB_TITLE.get(String.class)),
                            player))
                    .delay(FALLBACK_TITLE_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        } else {
            KICKED_TO_LOBBY.sendList(player,
                    new PlaceHolder("prefix", PREFIX.color()),
                    new PlaceHolder("server", registeredServer.getServerInfo().getName()),
                    new PlaceHolder("reason", event.getServerKickReason().get().toString()));
        }

        instance.getServer().getScheduler().buildTask(instance, () -> instance.getServer().getEventManager().fire(new VelocityAPI(player, kickedFrom, registeredServer, Arrays.toString(new Optional[]{event.getServerKickReason()}))));

    }
}
