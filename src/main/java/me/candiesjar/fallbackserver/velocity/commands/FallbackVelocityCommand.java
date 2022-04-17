package me.candiesjar.fallbackserver.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.candiesjar.fallbackserver.velocity.FallbackServerVelocity;

public class FallbackVelocityCommand implements SimpleCommand {

    private final FallbackServerVelocity plugin;

    public FallbackVelocityCommand(FallbackServerVelocity plugin) {
        this.plugin = plugin;
    }


    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();
        CommandSource commandSource = invocation.source();

        if (args.length > 0) {

        }



    }
}
