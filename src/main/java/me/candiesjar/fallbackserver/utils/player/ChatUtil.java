package me.candiesjar.fallbackserver.utils.player;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.stream.Collectors;

public class ChatUtil {

    private final FallbackServerBungee plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatUtil(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    public String getString(BungeeMessages msg) {
        return plugin.getMessagesConfig().getString(msg.getPath());
    }

    public String getString(BungeeMessages msg, Placeholder... placeholders) {
        return applyPlaceholders(getString(msg), placeholders);
    }

    public List<String> getStringList(BungeeMessages msg) {
        return plugin.getMessagesConfig().getStringList(msg.getPath());
    }

    public String getFormattedString(BungeeMessages bungeeMessages, Placeholder... placeholders) {
        return getString(bungeeMessages, placeholders);
    }

    public List<String> getStringList(BungeeMessages msg, Placeholder... placeholders) {
        return getStringList(msg).stream()
                .map(s -> applyPlaceholders(s, placeholders))
                .collect(Collectors.toList());
    }

    public Component asComponent(String s) {
        return miniMessage.deserialize(s);
    }

    public void sendList(CommandSender sender, List<String> messages) {
        Audience audience = plugin.adventure().sender(sender);
        messages.forEach(m -> audience.sendMessage(asComponent(m)));
    }

    public void sendFormattedList(BungeeMessages bungeeMessages, CommandSender commandSender, Placeholder... placeholders) {
        sendList(commandSender, getStringList(bungeeMessages, placeholders));
    }

    public BaseComponent[] asBungeeComponents(String s) {
        Component component = miniMessage.deserialize(s);
        return BungeeComponentSerializer.get().serialize(component);
    }

    public void clearChat(ProxiedPlayer player) {
        Audience audience = plugin.adventure().sender(player);
        for (int i = 0; i < 100; i++) {
            audience.sendMessage(Component.empty());
        }
    }

    public boolean checkMessage(String message, String name) {
        List<String> blocked = plugin.getConfig()
                .getStringList("settings.command_blocker_list." + name)
                .stream()
                .map(cmd -> "/" + cmd)
                .collect(Collectors.toList());

        return blocked.stream().anyMatch(message::equalsIgnoreCase);
    }

    public String applyPlaceholders(String s, Placeholder... placeholders) {
        for (Placeholder p : placeholders) {
            s = s.replace(p.getKey(), p.getValue());
        }
        return s;
    }
}
