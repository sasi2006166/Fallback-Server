package me.candiesjar.fallbackserver.commands.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.commands.impl.*;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SubCommandManager implements SimpleCommand {

    private final FallbackServerVelocity plugin;
    private final HashMap<String, ISubCommand> subCommands = Maps.newHashMap();

    public SubCommandManager(FallbackServerVelocity fallbackServerVelocity, FallbackServerVelocity plugin) {
        this.plugin = plugin;

        subCommands.put("reload", new ReloadCommand(fallbackServerVelocity));
        subCommands.put("status", new StatusCommand(fallbackServerVelocity));
        subCommands.put("servers", new ServersCommand(fallbackServerVelocity));
        subCommands.put("group", new GroupCommand(fallbackServerVelocity));
        subCommands.put("add", new AddCommand(fallbackServerVelocity));
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
            String messageTemplate = "<dark_gray>» <gray>Running <aqua><underlined>Fallback Server version</underlined> <gray>by <aqua><underlined>CandiesJar</underlined>";
            String formattedMessage = messageTemplate.replace("version", plugin.getVersion());

            commandSource.sendMessage(plugin.getMiniMessage().deserialize(formattedMessage));
            return;
        }

        if (args.length == 0) {
            VelocityMessages.MAIN_COMMAND.sendList(commandSource, new Placeholder("version", plugin.getVersion()));
            return;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            VelocityMessages.CORRECT_SYNTAX.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        ISubCommand ISubCommand = subCommands.get(args[0].toLowerCase());

        if (!ISubCommand.isEnabled()) {
            return;
        }

        if (!commandSource.hasPermission(ISubCommand.getPermission())) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                    new Placeholder("permission", ISubCommand.getPermission()));
            return;
        }

        ISubCommand.perform(commandSource, args);
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
