package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.objects.TextFile;
import net.md_5.bungee.api.CommandSender;

public class ReloadSubCommand implements SubCommand {

    @Override
    public String getPermission() {
        return BungeeConfig.RELOAD_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        TextFile.reloadAll();
        BungeeMessages.RELOAD.send(sender, new Placeholder("prefix", FallbackServerBungee.getInstance().getPrefix()));
    }
}
