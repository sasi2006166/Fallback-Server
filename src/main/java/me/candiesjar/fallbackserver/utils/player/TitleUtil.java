package me.candiesjar.fallbackserver.utils.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;

@UtilityClass
public class TitleUtil {

    private Title createTitle(String title, String subTitle, String serverName, Duration fadeIn, Duration stay, Duration fadeOut) {
        return Title.title(
                Component.text(ChatUtil.formatColor(title)
                        .replace("%server%", serverName)
                        .replace("%dots%", Utils.getDots(0))),
                Component.text(ChatUtil.formatColor(subTitle)
                        .replace("%server%", serverName)
                        .replace("%dots%", Utils.getDots(0))),
                Title.Times.times(fadeIn, stay, fadeOut)
        );
    }

    public void sendTitle(int fadeIn, int stay, int fadeOut, String title, String subTitle, RegisteredServer registeredServer, Player player) {
        String serverName = registeredServer.getServerInfo().getName();
        Title createdTitle = createTitle(title, subTitle, serverName, Duration.ofSeconds(fadeIn), Duration.ofSeconds(stay), Duration.ofSeconds(fadeOut));
        player.showTitle(createdTitle);
    }

    public void sendReconnectingTitle(int fadeIn, int stay, int dots, VelocityMessages title, VelocityMessages subTitle, Player player) {
        Title createdTitle = createTitle(
                ChatUtil.getFormattedString(title).replace("%dots%", Utils.getDots(dots)),
                ChatUtil.getFormattedString(subTitle),
                "",
                Duration.ofSeconds(fadeIn),
                Duration.ofSeconds(stay),
                Duration.ofSeconds(1)
        );
        player.showTitle(createdTitle);
    }
}
