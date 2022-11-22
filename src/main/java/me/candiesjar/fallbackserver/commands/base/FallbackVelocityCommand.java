package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;

import java.util.*;

public class FallbackVelocityCommand implements SimpleCommand {
    private final FallbackServerVelocity fallbackServerVelocity;

    private final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public FallbackVelocityCommand(FallbackServerVelocity fallbackServerVelocity) {
        this.fallbackServerVelocity = fallbackServerVelocity;

        subCommands.put("reload", new ReloadSubCommand(fallbackServerVelocity));
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        String[] args = invocation.arguments();

        String adminPermission = VelocityConfig.ADMIN_PERMISSION.get(String.class);
        boolean commandWithoutPermission = VelocityConfig.HIDE_COMMAND.get(Boolean.class);

        if (!commandSource.hasPermission(adminPermission) && commandWithoutPermission) {
            return;
        }

        if (!commandSource.hasPermission(adminPermission)) {
            commandSource.sendMessage(VelocityMessages.color("&8&l» &7Running &b&nFallback Server version &7by &b&nCandiesJar"
                    .replace("version", fallbackServerVelocity.getVersion())));
            return;
        }

        if (args.length == 0) {
            VelocityMessages.MAIN_COMMAND.sendList(commandSource, new Placeholder("version", fallbackServerVelocity.getVersion()));
            return;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            VelocityMessages.PARAMETERS.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());

        if (!subCommand.isEnabled()) {
            return;
        }

        if (!commandSource.hasPermission(subCommand.getPermission())) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                    new Placeholder("permission", subCommand.getPermission()));
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
                LinkedList<String> completion = Lists.newLinkedList(subCommands.keySet());
                Collections.sort(completion);
                return completion;

        }
        return Collections.emptyList();
    }
}
