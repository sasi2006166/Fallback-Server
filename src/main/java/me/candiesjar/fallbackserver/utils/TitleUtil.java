package me.candiesjar.fallbackserver.utils;

import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public class TitleUtil {

    public static void sendKickTitle(Player player) {

        final Component fallback_title = Component.text(VelocityMessages.FALLBACK_TITLE.color());
        final Component fallback_sub_title = Component.text(VelocityMessages.FALLBACK_SUB_TITLE.color());

        final Title.Times times = Title.Times.of(Duration.ofSeconds(VelocityMessages.FALLBACK_FADE_IN.get(Integer.class))
                , Duration.ofSeconds(VelocityMessages.FALLBACK_STAY.get(Integer.class))
                , Duration.ofSeconds(VelocityMessages.FALLBACK_FADE_OUT.get(Integer.class)));

        final Title title = Title.title(fallback_title, fallback_sub_title, times);

        player.showTitle(title);

    }

    public static void sendHubTitle(Player player) {

        final Title.Times times = Title.Times.of(Duration.ofSeconds(VelocityMessages.HUB_TITLE_FADE_IN.get(Integer.class))
                , Duration.ofSeconds(VelocityMessages.HUB_TITLE_STAY.get(Integer.class))
                , Duration.ofSeconds(VelocityMessages.HUB_TITLE_FADE_OUT.get(Integer.class)));

        final Title title = Title.title(VelocityMessages.colorize(VelocityMessages.HUB_TITLE.get(String.class))
                , VelocityMessages.colorize(VelocityMessages.HUB_SUB_TITLE.get(String.class))
                , times);

        player.showTitle(title);

    }
}
