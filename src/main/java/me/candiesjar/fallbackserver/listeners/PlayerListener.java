package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.VelocityUtils;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public class PlayerListener {

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event) {

        Player player = event.getPlayer();
        String adminPermission = VelocityConfig.ADMIN_PERMISSION.get(String.class);

        if (!player.hasPermission(adminPermission)) {
            return;
        }

        if (FallbackServerVelocity.getInstance().isAlpha()) {
            player.sendMessage(Component.text(" "));
            player.sendMessage(Component.text("§7You're running an §c§lALPHA VERSION §7of Fallback Server."));
            player.sendMessage(Component.text("§7If you find any bugs, please report them on discord."));
            player.sendMessage(Component.text(" "));
            return;
        }

        VelocityUtils.getUpdates().whenComplete((newUpdate, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }

            if (newUpdate != null && newUpdate) {
                VelocityMessages.NEW_UPDATE.sendList(player,
                        new Placeholder("old_version", FallbackServerVelocity.VERSION),
                        new Placeholder("new_version", VelocityUtils.getRemoteVersion()));
            }
        });
    }

}
