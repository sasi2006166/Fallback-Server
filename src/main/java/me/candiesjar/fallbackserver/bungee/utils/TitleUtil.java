package me.candiesjar.fallbackserver.bungee.utils;

import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.utils.chat.ChatUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TitleUtil {

    private final Title title = ProxyServer.getInstance().createTitle();

    public void sendFallbackTitle(ProxiedPlayer player) {
        title.fadeIn(BungeeMessages.FALLBACK_FADE_IN.getInt() * 20);
        title.fadeOut(BungeeMessages.FALLBACK_FADE_OUT.getInt() * 20);
        title.stay(BungeeMessages.FALLBACK_STAY.getInt() * 20);
        title.title(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.FALLBACK_TITLE)));
        title.subTitle(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.FALLBACK_SUB_TITLE)));
        title.send(player);
    }

    public void sendHubTitle(ProxiedPlayer player) {
        title.fadeIn(BungeeMessages.HUB_TITLE_FADE_IN.getInt() * 20);
        title.fadeOut(BungeeMessages.HUB_TITLE_FADE_OUT.getInt() * 20);
        title.stay(BungeeMessages.HUB_TITLE_STAY.getInt() * 20);
        title.title(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.HUB_TITLE)));
        title.subTitle(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.HUB_SUB_TITLE)));
        title.send(player);
    }
}
