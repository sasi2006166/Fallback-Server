package me.candiesjar.fallbackserver.utils.player;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@UtilityClass
public class TitleUtil {

    private final Title createdTitle = ProxyServer.getInstance().createTitle();

    public void sendTitle(int fadeIn, int stay, int fadeOut, BungeeMessages title, BungeeMessages subTitle, ServerInfo serverInfo, ProxiedPlayer proxiedPlayer) {

        createdTitle.fadeIn(fadeIn * 20);
        createdTitle.stay(stay * 20);
        createdTitle.fadeOut(fadeOut * 20);

        createdTitle.title(new TextComponent(ChatUtil.getFormattedString(title).replace("%server%", serverInfo.getName())));
        createdTitle.subTitle(new TextComponent(ChatUtil.getFormattedString(subTitle)));

        createdTitle.send(proxiedPlayer);

    }

    public void sendReconnectingTitle(int fadeIn, int stay, int dots, BungeeMessages title, BungeeMessages subTitle, ProxiedPlayer proxiedPlayer) {

        createdTitle.fadeIn(fadeIn);
        createdTitle.stay(stay);

        createdTitle.title(new TextComponent(ChatUtil.getFormattedString(title).replace("%dots%", Utils.getDots(dots))));
        createdTitle.subTitle(new TextComponent(ChatUtil.getFormattedString(subTitle)));

        createdTitle.send(proxiedPlayer);

    }
}
