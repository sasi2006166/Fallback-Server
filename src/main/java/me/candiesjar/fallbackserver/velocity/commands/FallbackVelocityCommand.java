package me.candiesjar.fallbackserver.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

public class FallbackVelocityCommand implements SimpleCommand {


    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();
        CommandSource commandSource = invocation.source();

        if (args.length > 0) {
        }

    }
}
