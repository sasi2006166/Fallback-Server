package me.candiesjar.fallbackserver.utils;

import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TitleUtil {

    private final Title createdTitle = ProxyServer.getInstance().createTitle();

    public void sendTitle(int fadeIn, int stay, int fadeOut, BungeeMessages title, BungeeMessages subTitle, ProxiedPlayer proxiedPlayer) {

        createdTitle.fadeIn(fadeIn * 20);
        createdTitle.stay(stay * 20);
        createdTitle.fadeOut(fadeOut * 20);

        createdTitle.title(new TextComponent(ChatUtil.getFormattedString(title)));
        createdTitle.subTitle(new TextComponent(ChatUtil.getFormattedString(subTitle)));

        createdTitle.send(proxiedPlayer);

    }
}
