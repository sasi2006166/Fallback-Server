package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;

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
        fallbackServerVelocity.getConfigTextFile().reload();
        fallbackServerVelocity.getMessagesTextFile().reload();

        VelocityMessages.RELOAD.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
    }
}
