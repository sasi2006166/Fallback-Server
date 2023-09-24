package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;

@RequiredArgsConstructor
public class StatusSubCommand implements SubCommand {

    private final FallbackServerVelocity plugin;

    @Override
    public String getPermission() {
        return VelocityConfig.STATUS_COMMAND_PERMISSION.get(String.class);
    }

    @Override
    public boolean isEnabled() {
        return VelocityConfig.STATUS_COMMAND.get(Boolean.class);
    }

    @Override
    public void perform(CommandSource sender, String[] args) {

        VelocityMessages.STATS_COMMAND.sendList(sender,
                new Placeholder("total_players", "" + plugin.getServer().getPlayerCount()),
                new Placeholder("used_memory", "" + Runtime.getRuntime().totalMemory() / (1024 * 1024)),
                new Placeholder("cores", "" + Runtime.getRuntime().availableProcessors()),
                new Placeholder("version", plugin.getVERSION()),
                new Placeholder("proxy_version", plugin.getServer().getVersion().getName()));

    }
}
