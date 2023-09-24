package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;

@RequiredArgsConstructor
public class ReloadSubCommand implements SubCommand {
    private final FallbackServerVelocity plugin;

    @Override
    public String getPermission() {
        return VelocityConfig.RELOAD_PERMISSION.get(String.class);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void perform(CommandSource commandSource, String[] args) {

        boolean oldCommand = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);
        String oldMode = VelocityConfig.FALLBACK_MODE.get(String.class);

        plugin.reloadAll();

        boolean newCommand = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);
        String newMode = VelocityConfig.FALLBACK_MODE.get(String.class);

        if (oldCommand != newCommand) {

            String[] aliases = VelocityConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]);

            CommandMeta commandMeta = plugin.getServer().getCommandManager()
                    .metaBuilder(VelocityConfig.LOBBY_ALIASES.getStringList().get(0))
                    .aliases(aliases)
                    .build();

            if (newCommand) {
                plugin.getServer().getCommandManager().register(commandMeta, new HubCommand(plugin));
            } else {
                plugin.getServer().getCommandManager().unregister(commandMeta);
            }

        }

        if (!oldMode.equals(newMode)) {
            plugin.reloadListeners();
        }

        VelocityMessages.RELOAD.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
    }
}
