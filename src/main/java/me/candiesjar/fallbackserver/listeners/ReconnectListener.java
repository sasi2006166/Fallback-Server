package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.ServerType;
import me.candiesjar.fallbackserver.utils.ConditionUtil;
import me.candiesjar.fallbackserver.utils.WorldUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReconnectListener {

    private final FallbackServerVelocity plugin;

    @Subscribe(priority = Short.MAX_VALUE)
    public void onPlayerKick(LoginLimboRegisterEvent event) {
        event.setOnKickCallback(kickEvent -> {
            Player player = event.getPlayer();
            RegisteredServer kickedFrom = kickEvent.getServer();
            String kickedFromName = kickedFrom.getServerInfo().getName();

            if (kickEvent.kickedDuringServerConnect()) {
                return false;
            }

            String group = ServerManager.getGroupByServer(kickedFromName) == null ? "default" : ServerManager.getGroupByServer(kickedFromName);

            if (group == null) {
                return false;
            }

            ServerType serverType = plugin.getServerTypeManager().get(group);

            if (!serverType.isReconnect()) {
                return false;
            }

            Optional<Component> componentOptional = kickEvent.getServerKickReason();
            boolean isEmpty = componentOptional.isEmpty();
            String kickReasonString = isEmpty ? "" : ChatUtil.componentToString(componentOptional.get());
            List<String> ignoredReasons = VelocityConfig.RECONNECT_IGNORED_REASONS.getStringList();

            if (shouldIgnore(kickReasonString, ignoredReasons)) {
                return false;
            }

            if (checkIgnoredServer(kickedFromName)) {
                player.disconnect(Component.text(kickReasonString));
                return false;
            }

            boolean clearTab = VelocityConfig.RECONNECT_CLEAR_TABLIST.get(Boolean.class);

            if (clearTab) {
                player.getTabList().clearAll();
            }

            boolean clearChat = VelocityConfig.CLEAR_CHAT_RECONNECT_JOIN.get(Boolean.class);

            if (clearChat) {
                ChatUtil.clearChat(player);
            }

            FallbackLimboHandler fallbackLimboHandler = new FallbackLimboHandler(kickedFrom, player.getUniqueId(), player);
            plugin.getPlayerCacheManager().put(player.getUniqueId(), fallbackLimboHandler);
            WorldUtil.getFallbackLimbo().spawnPlayer(player, fallbackLimboHandler);

            return true;
        });

    }

    private boolean checkIgnoredServer(String serverName) {
        return VelocityConfig.RECONNECT_IGNORED_SERVERS.getStringList().contains(serverName);
    }

    private boolean shouldIgnore(String reason, List<String> ignoredReasons) {
        return ConditionUtil.checkReason(ignoredReasons, reason);
    }

}
