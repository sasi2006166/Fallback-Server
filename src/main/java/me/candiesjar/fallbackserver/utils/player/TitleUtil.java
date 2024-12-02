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

    private Title createNewTitle() {
        return ProxyServer.getInstance().createTitle();
    }

    private void setTitleTiming(Title title, int fadeIn, int stay, int fadeOut) {
        title.fadeIn(fadeIn * 20);
        title.stay(stay * 20);
        title.fadeOut(fadeOut * 20);
    }

    private void setTitleText(Title title, String formattedTitle, String formattedSubTitle) {
        title.title(new TextComponent(formattedTitle));
        title.subTitle(new TextComponent(formattedSubTitle));
    }

    public void sendTitle(int fadeIn, int stay, int fadeOut, BungeeMessages title, BungeeMessages subTitle, ServerInfo serverInfo, ProxiedPlayer proxiedPlayer) {
        Title createdTitle = createNewTitle();
        setTitleTiming(createdTitle, fadeIn, stay, fadeOut);

        String formattedTitle = ChatUtil.getFormattedString(title)
                .replace("%server%", serverInfo.getName())
                .replace("%dots%", Utils.getDots(0));

        String formattedSubTitle = ChatUtil.getFormattedString(subTitle)
                .replace("%server%", serverInfo.getName())
                .replace("%dots%", Utils.getDots(0));

        setTitleText(createdTitle, formattedTitle, formattedSubTitle);
        createdTitle.send(proxiedPlayer);
    }

    public void sendReconnectingTitle(int fadeIn, int stay, int dots, BungeeMessages title, BungeeMessages subTitle, ProxiedPlayer proxiedPlayer) {
        Title createdTitle = createNewTitle();
        setTitleTiming(createdTitle, fadeIn, stay, 0);

        String formattedTitle = ChatUtil.getFormattedString(title)
                .replace("%dots%", Utils.getDots(dots));

        String formattedSubTitle = ChatUtil.getFormattedString(subTitle)
                .replace("%dots%", Utils.getDots(dots));

        setTitleText(createdTitle, formattedTitle, formattedSubTitle);
        createdTitle.send(proxiedPlayer);
    }

    public void clearPlayerTitle(ProxiedPlayer proxiedPlayer) {
        Title createdTitle = createNewTitle();
        createdTitle.reset();
        createdTitle.clear();
        createdTitle.send(proxiedPlayer);
    }
}
