package me.candiesjar.fallbackserver.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import me.candiesjar.fallbackserver.velocity.commands.FallbackVelocityCommand;
import me.candiesjar.fallbackserver.velocity.commands.HubCommand;
import me.candiesjar.fallbackserver.velocity.stats.VelocityMetrics;
import me.candiesjar.fallbackserver.velocity.utils.ConfigurationUtil;
import me.candiesjar.fallbackserver.velocity.utils.tasks.VelocityLobbyTask;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "fallbackservervelocity",
        name = "FallbackServerVelocity",
        version = "3.0.0.3",
        url = "github.com/sasi2006166",
        authors = "CandiesJar"
)

@Getter
public class FallbackServerVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final VelocityMetrics.Factory metricsFactory;
    private final Path path;

    @Inject
    public FallbackServerVelocity(ProxyServer server, Logger logger, VelocityMetrics.Factory metricsFactory, Path path) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.path = path;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        logger.info("§b __________      ________________              ______      ________                               ");
        logger.info("§b ___  ____/_____ ___  /__  /__  /_______ _________  /__    __  ___/______________   ______________");
        logger.info("§b __  /_   _  __ `/_  /__  /__  __ \\  __ `/  ___/_  //_/    _____ \\_  _ \\_  ___/_ | / /  _ \\_  ___/");
        logger.info("§b _  __/   / /_/ /_  / _  / _  /_/ / /_/ // /__ _  ,<       ____/ //  __/  /   __ |/ //  __/  /    ");
        logger.info("§b /_/      \\__,_/ /_/  /_/  /_.___/\\__,_/ \\___/ /_/|_|      /____/ \\___//_/    _____/ \\___//_/     ");

        logger.info("§7[§b!§7] Creating configuration files... §7[§b!§7]");
        ConfigurationUtil.saveConfiguration(path);

        logger.info("§7[§b!§7] Loading commands... §7[§b!§7]");
        loadCommands();

        logger.info("§7[§b!§7] Preparing telemetry... §7[§b!§7]");
        loadStats(metricsFactory);

        logger.info("§7[§b!§7] Starting schedulers... §7[§b!§7]");
        server.getScheduler().buildTask(this, new VelocityLobbyTask()).repeat(5, TimeUnit.SECONDS).schedule();


        logger.info("§7[§b!§7] Plugin loaded successfully §7[§b!§7]");

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {

        logger.info("§7[§b!§7] Plugin unloaded successfully §7[§b!§7]");
    }

    private void loadStats(VelocityMetrics.Factory factory) {
        factory.make(this, 12602);
    }

    private void loadCommands() {
        server.getCommandManager().register(Arrays.toString(ConfigurationUtil.getConfig().getStringList("").toArray(new String[0])), new HubCommand());
        server.getCommandManager().register("fsv", new FallbackVelocityCommand(this));
        CommandMeta commandMeta = server.getCommandManager().metaBuilder("")
                .aliases(ConfigurationUtil.getConfig().getStringList("").toArray(new String[0]))
                .build();
        server.getCommandManager().register(commandMeta, new HubCommand());
    }

}
