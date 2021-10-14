package me.candiesjar.fallbackserver.bungee.utils;

import me.candiesjar.fallbackserver.bungee.enums.ConfigFields;
import me.candiesjar.fallbackserver.bungee.enums.MessagesFields;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TitleUtil {

    private final Title title = ProxyServer.getInstance().createTitle();

    public void sendFallbackTitle(ProxiedPlayer player) {
        title.fadeIn(MessagesFields.FADE_IN.getInt() * 20);
        title.fadeOut(MessagesFields.FADE_OUT.getInt() * 20);
        title.stay(MessagesFields.STAY.getInt() * 20);
        title.title(new TextComponent(MessagesFields.FALLBACK_TITLE.getFormattedString()));
        title.subTitle(new TextComponent(MessagesFields.FALLBACK_SUB_TITLE.getFormattedString()));
        title.send(player);
    }

    public void sendHubTitle(ProxiedPlayer player) {
        title.fadeIn(MessagesFields.HUB_TITLE_FADE_IN.getInt() * 20);
        title.fadeOut(MessagesFields.HUB_TITLE_FADE_OUT.getInt() * 20);
        title.stay(MessagesFields.HUB_TITLE_STAY.getInt() * 20);
        title.title(new TextComponent(MessagesFields.HUB_TITLE.getFormattedString()));
        title.subTitle(new TextComponent(MessagesFields.HUB_SUB_TITLE.getFormattedString()));
        title.send(player);
    }
}
