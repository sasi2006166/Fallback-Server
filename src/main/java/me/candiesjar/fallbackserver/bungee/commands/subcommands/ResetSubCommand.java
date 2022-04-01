package me.candiesjar.fallbackserver.bungee.commands.subcommands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.objects.PlaceHolder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class ResetSubCommand implements SubCommand {

    // Like Pokemon games, code is here, but not activated.

    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    private boolean restoreConfig = false;
    private boolean restoreMessages = false;

    @Override
    public String getPermission() {
        return BungeeConfig.RESET_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.RESET_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        if (arguments.length < 2) {
            sender.sendMessage(new TextComponent("Messages or config?"));
            return;
        }
        switch (arguments[1]) {
            case "config":
                if (!restoreConfig) {
                    BungeeMessages.CONFIGURATION_WARN.sendList(sender);

                    restoreConfig = true;
                    return;
                }

                instance.getConfigTextFile().recreate();
                BungeeMessages.CONFIGURATION_RESTORED.send(sender, new PlaceHolder("prefix", instance.getPrefix()));
                restoreConfig = false;
                break;

            case "messages":
                if (!restoreMessages) {
                    BungeeMessages.MESSAGES_WARN.sendList(sender);

                    restoreMessages = true;
                    return;
                }

                instance.getMessagesTextFile().recreate();
                BungeeMessages.MESSAGES_RESTORED.send(sender, new PlaceHolder("prefix", instance.getPrefix()));
                break;
        }
    }
}
