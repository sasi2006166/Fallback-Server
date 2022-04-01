package me.candiesjar.fallbackserver.bungee.commands.subcommands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.objects.PlaceHolder;
import me.candiesjar.fallbackserver.bungee.objects.TextFile;
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
        BungeeMessages.RELOAD.send(sender, new PlaceHolder("prefix", FallbackServerBungee.getInstance().getPrefix()));
    }
}
