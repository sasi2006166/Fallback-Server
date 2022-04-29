package me.candiesjar.fallbackserver.commands.subcommands;

import lombok.SneakyThrows;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.net.URL;

public class UpdateSubCommand implements SubCommand {

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @SneakyThrows
    @Override
    public void perform(CommandSender sender, String[] arguments) {

        if (!Utils.isUpdateAvailable()) {
            sender.sendMessage(new TextComponent("No update available."));
            return;
        }

        sender.sendMessage(new TextComponent("Update started..."));

        URL website = new URL("");

    }
}
