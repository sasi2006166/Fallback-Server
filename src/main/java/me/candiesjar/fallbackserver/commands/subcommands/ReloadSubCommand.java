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
import me.candiesjar.fallbackserver.utils.tasks.PingTask;

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

        boolean wasCommandEnabled = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);

        PingTask.getScheduledTask().cancel();
        plugin.reloadAll();

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

        plugin.loadServers();
        PingTask.reload();

        VelocityMessages.RELOAD.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
    }
}
