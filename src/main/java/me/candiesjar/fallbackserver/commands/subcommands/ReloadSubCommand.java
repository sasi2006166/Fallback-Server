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
    private final FallbackServerVelocity fallbackServerVelocity;

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

        boolean hubReload = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);

        fallbackServerVelocity.reloadAll();

        boolean reloadCommand = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);

        if (hubReload != reloadCommand) {

            String[] aliases = VelocityConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]);

            CommandMeta commandMeta = fallbackServerVelocity.getServer().getCommandManager()
                    .metaBuilder(VelocityConfig.LOBBY_ALIASES.getStringList().get(0))
                    .aliases(aliases)
                    .build();

            if (reloadCommand) {
                fallbackServerVelocity.getServer().getCommandManager().register(commandMeta, new HubCommand(fallbackServerVelocity));
            } else {
                fallbackServerVelocity.getServer().getCommandManager().unregister(commandMeta);
            }

        }

        fallbackServerVelocity.reloadAll();

        VelocityMessages.RELOAD.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
    }
}
