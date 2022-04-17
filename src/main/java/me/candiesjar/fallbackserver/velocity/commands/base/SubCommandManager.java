package me.candiesjar.fallbackserver.velocity.commands.base;

import com.velocitypowered.api.command.SimpleCommand;
import me.candiesjar.fallbackserver.velocity.FallbackServerVelocity;
import me.candiesjar.fallbackserver.velocity.commands.interfaces.SubCommand;

import java.util.HashMap;

public class SubCommandManager implements SimpleCommand {

    private final FallbackServerVelocity plugin;

    private final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public SubCommandManager(FallbackServerVelocity plugin) {
        this.plugin = plugin;
    }


    @Override
    public void execute(Invocation invocation) {

    }


}
