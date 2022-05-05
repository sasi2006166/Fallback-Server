package me.candiesjar.fallbackserver;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.commands.base.SubCommandManager;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.listeners.ChatListener;
import me.candiesjar.fallbackserver.listeners.FallbackListener;
import me.candiesjar.fallbackserver.listeners.PlayerListener;
import me.candiesjar.fallbackserver.objects.TextFile;
import me.candiesjar.fallbackserver.stats.VelocityMetrics;
import me.candiesjar.fallbackserver.utils.UpdateUtil;
import me.candiesjar.fallbackserver.utils.tasks.LobbyTask;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Getter
@Plugin(
        id = "fallbackservervelocity",
        name = "FallbackServerVelocity",
        version = "3.0.0.4",
        url = "github.com/sasi2006166",
        authors = "CandiesJar"
)

public class FallbackServerVelocity {

    private ProxyServer server;
    private Logger logger;
    private VelocityMetrics.Factory metricsFactory;
    private Path path;

    private TextFile configTextFile;
    private TextFile messagesTextFile;

    private static FallbackServerVelocity instance;

    public static FallbackServerVelocity getInstance() {
        return instance;
    }

    @Inject
    public FallbackServerVelocity(ProxyServer server, Logger logger, VelocityMetrics.Factory metricsFactory, @DataDirectory Path path) {
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
        instance = this;

        configTextFile = new TextFile(path, "config.yml");
        messagesTextFile = new TextFile(path, "messages.yml");

        logger.info("§7[§b!§7] Loading commands... §7[§b!§7]");
        loadCommands();

        logger.info("§7[§b!§7] Preparing events... §7[§b!§7]");
        loadListeners();

        logger.info("§7[§b!§7] Preparing telemetry... §7[§b!§7]");
        loadStats(metricsFactory);

        logger.info("§7[§b!§7] Starting schedulers... §7[§b!§7]");
        server.getScheduler().buildTask(this, new LobbyTask()).repeat(5, TimeUnit.SECONDS).schedule();

        logger.info("§7[§b!§7] Plugin loaded successfully §7[§b!§7]");
        UpdateUtil.checkUpdates();

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {

        logger.info("§7[§b!§7] Unloading plugin §7[§b!§7]");

        server = null;
        logger = null;
        metricsFactory = null;
        path = null;
    }

    private void loadStats(VelocityMetrics.Factory factory) {
        if (VelocityConfig.USE_STATS.get(Boolean.class)) {
            factory.make(this, 12602);
        }
    }

    private void loadCommands() {
        server.getCommandManager().register("fsv", new SubCommandManager());

        CommandMeta commandMeta = server.getCommandManager().metaBuilder("")
                .aliases(VelocityConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]))
                .build();

        server.getCommandManager().register(commandMeta, new HubCommand());

    }

    private void loadListeners() {
        server.getEventManager().register(this, new PlayerListener());
        server.getEventManager().register(this, new FallbackListener());
        server.getEventManager().register(this, new ChatListener());
    }

    public boolean isHub(ServerInfo serverInfo) {
        return VelocityConfig.LOBBIES.getStringList().contains(serverInfo.getName());
    }

    public Optional<String> getVersion() {
        return server.getPluginManager().getPlugin("fallbackservervelocity").get().getDescription().getVersion();
    }

}
