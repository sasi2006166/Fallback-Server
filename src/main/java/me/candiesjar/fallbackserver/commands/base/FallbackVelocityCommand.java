package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FallbackVelocityCommand implements SimpleCommand {

    private final HashMap<String, SubCommand> subCommands = Maps.newHashMap();

    public FallbackVelocityCommand(FallbackServerVelocity fallbackServerVelocity) {
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
            commandSource.sendMessage(Component.text(ChatUtil.color("&8&l» &7Running &b&nFallback Server version &7by &b&nCandiesJar"
                    .replace("version", FallbackServerVelocity.getVERSION()))));
            return;
        }

        if (args.length == 0) {
            VelocityMessages.MAIN_COMMAND.sendList(commandSource, new Placeholder("version", FallbackServerVelocity.getVERSION()));
            return;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            VelocityMessages.CORRECT_SYNTAX.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());

        if (!subCommand.isEnabled()) {
            return;
        }

        if (!commandSource.hasPermission(subCommand.getPermission())) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                    new Placeholder("permission", subCommand.getPermission()));
            return;
        }

        subCommand.perform(commandSource, args);

    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            CommandSource commandSource = invocation.source();

            if (!commandSource.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
                return Collections.emptyList();
            }

            if (!VelocityConfig.TAB_COMPLETE.get(Boolean.class)) {
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
        });
    }
}
