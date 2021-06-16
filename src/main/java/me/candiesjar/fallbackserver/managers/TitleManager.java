package me.candiesjar.fallbackserver.managers;

import me.candiesjar.fallbackserver.utils.Fields;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TitleManager {

    private final Title title = ProxyServer.getInstance().createTitle();

    public void sendTitle(ProxiedPlayer player) {
        title.fadeIn(Fields.FADEIN.getInt() * 20);
        title.fadeOut(Fields.FADEOUT.getInt() * 20);
        title.stay(Fields.TITLESTAY.getInt() * 20);
        title.title(new TextComponent(Fields.TITLE.getFormattedString()));
        title.subTitle(new TextComponent(Fields.TITLESUBTITLE.getFormattedString()));
        title.send(player);
    }
}
