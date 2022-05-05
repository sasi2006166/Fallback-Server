package me.candiesjar.fallbackserver.commands.base;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;

import java.util.HashMap;

public class SubCommandManager implements SimpleCommand {

    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    private final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public SubCommandManager() {

        subCommands.put("reload", new ReloadSubCommand());
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource commandSource = invocation.source();
        String[] args = invocation.arguments();

        if (!commandSource.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class)) && !VelocityConfig.COMMAND_WITHOUT_PERMISSION.get(Boolean.class)) {
            return;
        }

        if (!commandSource.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
            commandSource.sendMessage(VelocityMessages.colorize("&8&l» &7Running &b&nFallback Server %version% &7by &b&nCandiesJar"
                    .replace("%version%", instance.getVersion().get())));
            return;
        }

        if (args.length == 0) {
            VelocityMessages.sendList(commandSource, VelocityMessages.MAIN_COMMAND.getStringList());
            return;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            commandSource.sendMessage(VelocityMessages.colorize(VelocityMessages.PARAMETERS.get(String.class)
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());

        if (!subCommand.isEnabled()) {
            return;
        }

        if (!commandSource.hasPermission(subCommand.getPermission())) {
            commandSource.sendMessage(VelocityMessages.colorize(VelocityMessages.NO_PERMISSION.get(String.class)
                    .replace("%permission%", subCommand.getPermission())
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        subCommand.perform(commandSource, args);

    }

}
