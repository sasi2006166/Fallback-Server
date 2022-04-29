package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.md_5.bungee.api.ProxyServer;

import java.util.List;

@UtilityClass
public class UpdateUtil {

    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    public void checkUpdates() {
        if (BungeeConfig.UPDATE_CHECKER.getBoolean()) {
            Utils.checkUpdates();
        }

        if (Utils.getRemoteVersion().equalsIgnoreCase("Loading")) {
            return;
        }

        if (Utils.isUpdateAvailable()) {
            List<String> updateMessageList = ChatUtil.getStringList(BungeeMessages.NEW_UPDATE,
                    new PlaceHolder("old_version", instance.getDescription().getVersion()),
                    new PlaceHolder("new_version", Utils.getRemoteVersion()));

            for (String s : updateMessageList)
                ProxyServer.getInstance().getLogger().info(ChatUtil.color(s));
        }

    }
}
