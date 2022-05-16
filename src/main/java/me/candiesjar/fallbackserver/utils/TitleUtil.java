package me.candiesjar.fallbackserver.utils;

import com.velocitypowered.api.proxy.Player;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;

@UtilityClass
public class TitleUtil {

    public void sendTitle(int fadeIn, int stay, int fadeOut, String title, String sub_title, Player player) {

        final Title.Times times = Title.Times.of(Duration.ofSeconds(fadeIn),
                Duration.ofSeconds(stay),
                Duration.ofSeconds(fadeOut));

        final Title createdTitle = Title.title(Component.text(title), Component.text(sub_title), times);

        player.showTitle(createdTitle);

    }

}
