package me.candiesjar.fallbackserver.commands.base;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.PlaceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FallbackVelocityCommand implements SimpleCommand {
    private final FallbackServerVelocity fallbackServerVelocity;

    private final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public FallbackVelocityCommand(FallbackServerVelocity fallbackServerVelocity) {
        this.fallbackServerVelocity = fallbackServerVelocity;

        subCommands.put("reload", new ReloadSubCommand(fallbackServerVelocity));
    }

    @Override
    public void execute(Invocation invocation) {
        final CommandSource commandSource = invocation.source();
        final String[] args = invocation.arguments();

        final String adminPermission = VelocityConfig.ADMIN_PERMISSION.get(String.class);
        final boolean commandWithoutPermission = VelocityConfig.COMMAND_WITHOUT_PERMISSION.get(Boolean.class);

        if (!commandSource.hasPermission(adminPermission) && !commandWithoutPermission) {
            return;
        }

        if (!commandSource.hasPermission(adminPermission)) {
            commandSource.sendMessage(VelocityMessages.color("&8&l» &7Running &b&nFallback Server version &7by &b&nCandiesJar"
                    .replace("version", fallbackServerVelocity.getVersion())));
            return;
        }

        if (args.length == 0) {
            VelocityMessages.MAIN_COMMAND.sendList(commandSource, new PlaceHolder("version", fallbackServerVelocity.getVersion()));
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

    @Override
    public List<String> suggest(Invocation invocation) {

        CommandSource commandSource = invocation.source();

        if (!commandSource.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
            return Collections.emptyList();
        }

        if (!VelocityConfig.TAB_COMPLETION.get(Boolean.class)) {
            return Collections.emptyList();
        }

        String[] args = invocation.arguments();

        switch (args.length) {

            case 0:
            case 1:
                List<String> completion = new ArrayList<>(subCommands.keySet());
                Collections.sort(completion);
                return completion;

        }
        return Collections.emptyList();
    }
}
