package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.utils.WorldUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReconnectListener {

    private final FallbackServerVelocity plugin;

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerKick(LoginLimboRegisterEvent event) {

        event.setOnKickCallback(kickEvent -> {

            RegisteredServer kickedFrom = kickEvent.getServer();
            String serverName = kickedFrom.getServerInfo().getName();
            Player player = event.getPlayer();

            if (kickEvent.kickedDuringServerConnect()) {
                return false;
            }

            Optional<Component> componentOptional = kickEvent.getServerKickReason();
            boolean isEmpty = componentOptional.isEmpty();
            String kickReasonString = isEmpty ? "" : ChatUtil.componentToString(componentOptional.get());
            List<String> ignoredReasons = VelocityConfig.RECONNECT_IGNORED_REASONS.getStringList();

            for (String blacklist : ignoredReasons) {

                if (isEmpty) {
                    break;
                }

                if (PlainTextComponentSerializer.plainText().serialize(componentOptional.get()).contains(blacklist)) {
                    player.disconnect(Component.text(kickReasonString));
                    return false;
                }

            }

            if (checkIgnoredServer(serverName)) {
                player.disconnect(Component.text(kickReasonString));
                return false;
            }

            boolean clearTab = VelocityConfig.RECONNECT_CLEAR_TABLIST.get(Boolean.class);

            if (clearTab) {
                player.getTabList().clearAll();
            }

            boolean clearChat = VelocityConfig.CLEAR_CHAT_RECONNECT_JOIN.get(Boolean.class);

            if (clearTab) {
                ChatUtil.clearChat(player);
            }

            FallbackLimboHandler fallbackLimboHandler = new FallbackLimboHandler(kickedFrom, player.getUniqueId(), player);
            plugin.getPlayerCacheManager().put(player.getUniqueId(), fallbackLimboHandler);
            WorldUtil.getFallbackWorld().spawnPlayer(player, fallbackLimboHandler);

            return true;
        });

    }

    private boolean checkIgnoredServer(String serverName) {
        return VelocityConfig.RECONNECT_IGNORED_SERVERS.getStringList().contains(serverName);
    }

}
