package me.candiesjar.fallbackserver.utils.player;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.utils.Utils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.time.Duration;

public class TitleUtil {

    private final FallbackServerBungee fallbackServerBungee;

    public TitleUtil(FallbackServerBungee fallbackServerBungee) {
        this.fallbackServerBungee = fallbackServerBungee;
    }

    private Title createNewTitle(String formattedTitle, String formattedSubTitle, int fadeIn, int stay, int fadeOut) {
        Component mainTitle = ChatUtil.asComponent(formattedTitle);
        Component subTitle = ChatUtil.asComponent(formattedSubTitle);

        Title.Times times = Title.Times.times(Duration.ofSeconds(fadeIn), Duration.ofSeconds(stay), Duration.ofSeconds(fadeOut));
        return Title.title(mainTitle, subTitle, times);
    }

    public void sendTitle(int fadeIn, int stay, int fadeOut, BungeeMessages title, BungeeMessages subTitle, ServerInfo serverInfo, ProxiedPlayer proxiedPlayer) {
        Title createdTitle = createNewTitle(formatMessage(title, serverInfo, 0), formatMessage(subTitle, serverInfo, 0), fadeIn, stay, fadeOut);
        Audience audience = fallbackServerBungee.adventure().sender(proxiedPlayer);
        audience.showTitle(createdTitle);
    }

    public void sendReconnectingTitle(int fadeIn, int stay, int dots, BungeeMessages title, BungeeMessages subTitle, ProxiedPlayer proxiedPlayer) {
        Title createdTitle = createNewTitle(formatMessage(title, null, dots), formatMessage(subTitle, null, dots), fadeIn, stay, 0);
        Audience audience = fallbackServerBungee.adventure().sender(proxiedPlayer);
        audience.showTitle(createdTitle);
    }

    private String formatMessage(BungeeMessages message, ServerInfo serverInfo, int dots) {
        String formatted = ChatUtil.getFormattedString(message)
                .replace("%dots%", Utils.getDots(dots));
        if (serverInfo != null) {
            formatted = formatted.replace("%server%", serverInfo.getName());
        }
        return formatted;
    }

    public void clearPlayerTitle(ProxiedPlayer proxiedPlayer) {
        Audience audience = fallbackServerBungee.adventure().sender(proxiedPlayer);
        audience.clearTitle();
    }
}
