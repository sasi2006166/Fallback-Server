package me.candiesjar.fallbackserver.commands.base;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;

import java.util.HashMap;

public class SubCommandManager implements SimpleCommand {

    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    private final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public SubCommandManager() {

        subCommands.put("reload", new ReloadSubCommand());

    }

    @Override
    public void execute(Invocation invocation) {

        final CommandSource commandSource = invocation.source();
        final String[] args = invocation.arguments();

        if (!commandSource.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class)) && !VelocityConfig.COMMAND_WITHOUT_PERMISSION.get(Boolean.class)) {
            return;
        }

        if (!commandSource.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
            commandSource.sendMessage(VelocityMessages.color("&8&l» &7Running &b&nFallback Server version &7by &b&nCandiesJar"
                    .replace("version", instance.getVersion().get())));
            return;
        }

        if (args.length == 0) {
            VelocityMessages.MAIN_COMMAND.sendList(commandSource, new PlaceHolder("version", instance.getVersion().get()));
            return;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            VelocityMessages.PARAMETERS.send(commandSource, new PlaceHolder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        final SubCommand subCommand = subCommands.get(args[0].toLowerCase());

        if (!subCommand.isEnabled()) {
            return;
        }

        if (!commandSource.hasPermission(subCommand.getPermission())) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new PlaceHolder("prefix", VelocityMessages.PREFIX.color()),
                    new PlaceHolder("permission", subCommand.getPermission()));
            return;
        }

        subCommand.perform(commandSource, args);

    }

}
