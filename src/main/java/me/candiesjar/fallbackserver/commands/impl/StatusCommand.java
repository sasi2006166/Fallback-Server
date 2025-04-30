package me.candiesjar.fallbackserver.commands.impl;

import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

@RequiredArgsConstructor
public class StatusCommand implements ISubCommand {

    private final FallbackServerBungee plugin;

    @Override
    public String getPermission() {
        return BungeeConfig.STATUS_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.STATUS_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

        BungeeMessages.STATS_COMMAND.sendList(sender,
                new Placeholder("total_players", "" + ProxyServer.getInstance().getPlayers().size()),
                new Placeholder("used_memory", "" + Runtime.getRuntime().totalMemory() / (1024 * 1024)),
                new Placeholder("cores", "" + Runtime.getRuntime().availableProcessors()),
                new Placeholder("version", plugin.getVersion()),
                new Placeholder("proxy_version", plugin.getProxy().getVersion()));
    }
}
