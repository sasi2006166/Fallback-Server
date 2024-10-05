package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

@RequiredArgsConstructor
public class CreateSubCommand implements SubCommand {

    private final FallbackServerVelocity plugin;

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
        if (args.length != 3) {
            VelocityMessages.CREATE_COMMAND_PARAMETERS.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        String group = args[1];

        if (Utils.checkIfGroupExists(group)) {
            VelocityMessages.CREATE_COMMAND_EXISTS.send(commandSource,
                    new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                    new Placeholder("group", group));
            return;
        }

        String mode = args[2];

        switch (mode.toUpperCase()) {
            case "RECONNECT":
            case "DEFAULT":
            case "FALLBACK":
                break;
            default:
                VelocityMessages.CREATE_COMMAND_MODE.send(commandSource,
                        new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                        new Placeholder("mode", mode));
                return;
        }

        createGroup(group, mode);

        VelocityMessages.CREATE_COMMAND_CREATED.sendList(commandSource,
                new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                new Placeholder("group", group),
                new Placeholder("mode", mode));
    }

    public void createGroup(String group, String mode) {
        ConfigurationSection section = plugin.getServersTextFile().getConfig().getConfigurationSection("servers");
        List<String> servers = List.of("placeholder");

        section.set(group + ".servers", servers);
        section.set(group + ".lobbies", servers);
        section.set(group + ".mode", mode.toUpperCase());

        plugin.getServersTextFile().save();
        plugin.reloadAll();
    }
}
