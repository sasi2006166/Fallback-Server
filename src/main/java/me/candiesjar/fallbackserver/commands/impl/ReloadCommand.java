package me.candiesjar.fallbackserver.commands.impl;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.core.HubCommand;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;

@RequiredArgsConstructor
public class ReloadCommand implements ISubCommand {
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

        boolean wasCommandEnabled = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);

        boolean isCommandEnabled = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);

        if (wasCommandEnabled != isCommandEnabled) {
            String[] aliases = VelocityConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]);

            if (aliases.length == 0) {
                aliases = new String[]{"hub"};
            }

            CommandMeta commandMeta = plugin.getServer().getCommandManager()
                    .metaBuilder(VelocityConfig.LOBBY_ALIASES.getStringList().get(0))
                    .aliases(aliases)
                    .build();

            if (isCommandEnabled) {
                plugin.getServer().getCommandManager().register(commandMeta, new HubCommand(plugin));
            } else {
                plugin.getServer().getCommandManager().unregister(commandMeta);
            }

        }

        plugin.getServerTypeManager().clear();
        plugin.getOnlineLobbiesManager().clear();

        plugin.reloadAll();

        VelocityMessages.RELOAD.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
    }
}
