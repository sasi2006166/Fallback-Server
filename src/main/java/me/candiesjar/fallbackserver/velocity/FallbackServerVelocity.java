package me.candiesjar.fallbackserver.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import me.candiesjar.fallbackserver.velocity.commands.FallbackVelocityCommand;
import me.candiesjar.fallbackserver.velocity.commands.HubCommand;
import me.candiesjar.fallbackserver.velocity.stats.VelocityMetrics;
import me.candiesjar.fallbackserver.velocity.utils.ConfigurationUtil;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;

@Plugin(
        id = "fallbackservervelocity",
        name = "FallbackServerVelocity",
        version = "3.0.0",
        url = "github.com/sasi2006166",
        authors = "CandiesJar"
)
public class FallbackServerVelocity {

    private static FallbackServerVelocity instance;

    public static FallbackServerVelocity getInstance() {
        return instance;
    }

    @Inject
    public void onProxyInitialization(Logger logger, CommandManager commandManager, @DataDirectory Path path, VelocityMetrics.Factory metricsFactory) {

        // Instances
        logger.info("§7[§b!§7] Loading configuration §7[§b!§7]");
        instance = this;
        ConfigurationUtil.saveConfiguration(path);
        commandManager.register(Arrays.toString(ConfigurationUtil.getConfig().getStringList("").toArray(new String[0])), new HubCommand());
        commandManager.register("fsv", new FallbackVelocityCommand());

        loadStats(metricsFactory);

        logger.info("§aPlugin loaded.");

    }

    private void loadStats(VelocityMetrics.Factory factory) {
        factory.make(this, 12602);
    }

    private void loadCommands(CommandManager commandManager) {
        CommandMeta commandMeta = commandManager.metaBuilder("")
                .aliases(ConfigurationUtil.getConfig().getStringList("").toArray(new String[0]))
                .build();
        commandManager.register(commandMeta, new HubCommand());
    }

}
