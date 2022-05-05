package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.TextFile;

public class ReloadSubCommand implements SubCommand {

    @Override
    public String getPermission() {
        return VelocityConfig.RELOAD_PERMISSION.get(String.class);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void perform(CommandSource commandSource, String[] args) {
        TextFile.reloadAll();
        commandSource.sendMessage(VelocityMessages.colorize(VelocityMessages.RELOAD.get(String.class)
                .replace("%prefix%", VelocityMessages.PREFIX.color())));
    }
}
