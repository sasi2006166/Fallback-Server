package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import net.md_5.bungee.api.CommandSender;

public class DebugSubCommand implements SubCommand {

    private final FallbackServerBungee plugin;

    public DebugSubCommand(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isDebug();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

    }
}
