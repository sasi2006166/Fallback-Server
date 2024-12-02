package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

public class GroupSubCommand implements SubCommand {

    private final FallbackServerVelocity plugin;
    private final ConfigurationSection section;

    public GroupSubCommand(FallbackServerVelocity plugin) {
        this.plugin = plugin;
        this.section = plugin.getServersTextFile().getConfig().getConfigurationSection("servers");
    }

    @Override
    public String getPermission() {
        return VelocityConfig.CREATE_COMMAND_PERMISSION.get(String.class);
    }

    @Override
    public boolean isEnabled() {
        return VelocityConfig.CREATE_COMMAND.get(Boolean.class);
    }

    @Override
    public void perform(CommandSource commandSource, String[] args) {
        if (args.length != 4) {
            VelocityMessages.GROUP_COMMAND_PARAMETERS.sendList(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        String parameter = args[1];
        String group, confirmation;

        switch (parameter) {
            case "create":
                group = args[2];
                String mode = args[3];
                handleCreate(group, mode, commandSource);
                break;
            case "delete":
                group = args[2];
                confirmation = args[3];

                if (confirmation == null || !confirmation.equalsIgnoreCase("confirm")) {
                    VelocityMessages.GROUP_COMMAND_MISSING_CONFIRM.send(commandSource,
                            new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
                    break;
                }

                handleDelete(group, commandSource);
                break;
        }
    }

    private void handleCreate(String group, String mode, CommandSource commandSource) {
        if (ServerManager.checkIfGroupExists(group)) {
            VelocityMessages.GROUP_ALREADY_EXISTS.send(commandSource,
                    new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                    new Placeholder("group", group));
            return;
        }

        switch (mode.toUpperCase()) {
            case "RECONNECT":
            case "DEFAULT":
            case "FALLBACK":
                break;
            default:
                VelocityMessages.GROUP_MODE_UNDEFINED.send(commandSource,
                        new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                        new Placeholder("mode", mode));
                return;
        }

        createGroup(group, mode);

        VelocityMessages.GROUP_COMMAND_DONE.sendList(commandSource,
                new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                new Placeholder("group", group),
                new Placeholder("mode", mode));
    }

    private void handleDelete(String group, CommandSource commandSource) {
        if (!ServerManager.checkIfGroupExists(group)) {
            VelocityMessages.GROUP_DOES_NOT_EXIST.send(commandSource,
                    new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                    new Placeholder("group", group));
            return;
        }

        deleteGroup(group);

        VelocityMessages.GROUP_COMMAND_DELETED.sendList(commandSource,
                new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                new Placeholder("group", group));
    }

    private void createGroup(String group, String mode) {
        List<String> servers = List.of("placeholder");

        section.set(group + ".servers", servers);
        section.set(group + ".lobbies", servers);
        section.set(group + ".mode", mode.toUpperCase());

        plugin.getServersTextFile().save();
    }

    private void deleteGroup(String group) {
        section.set(group, null);
        plugin.getServersTextFile().save();
    }

}
