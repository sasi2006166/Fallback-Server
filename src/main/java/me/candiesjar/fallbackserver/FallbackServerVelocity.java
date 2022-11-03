package me.candiesjar.fallbackserver;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import me.candiesjar.fallbackserver.commands.base.FallbackVelocityCommand;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.listeners.CommandListener;
import me.candiesjar.fallbackserver.listeners.FallbackListener;
import me.candiesjar.fallbackserver.listeners.PlayerListener;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.TextFile;
import me.candiesjar.fallbackserver.stats.VelocityMetrics;
import me.candiesjar.fallbackserver.utils.VelocityUtils;
import me.candiesjar.fallbackserver.utils.tasks.LobbyTask;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "fallbackservervelocity",
        name = "FallbackServerVelocity",
        version = "3.1.3-Alpha1",
        url = "github.com/sasi2006166",
        authors = "CandiesJar"
)

@Getter
public class FallbackServerVelocity {

    public static final String VERSION = "3.1.3-Alpha1";

    private final ProxyServer server;
    private final Logger logger;
    private final VelocityMetrics.Factory metricsFactory;
    private final Path path;

    private TextFile configTextFile;
    private TextFile messagesTextFile;

    private ScheduledTask task;

    @Getter
    private static FallbackServerVelocity instance;

    @Getter
    private boolean isAlpha = false;

    private FallingServerManager fallingServerManager;

    @Inject
    public FallbackServerVelocity(ProxyServer server, Logger logger, VelocityMetrics.Factory metricsFactory, @DataDirectory Path path) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.path = path;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        logger.info("§b __________      ________________              ______      ________                               ");
        logger.info("§b ___  ____/_____ ___  /__  /__  /_______ _________  /__    __  ___/______________   ______________");
        logger.info("§b __  /_   _  __ `/_  /__  /__  __ \\  __ `/  ___/_  //_/    _____ \\_  _ \\_  ___/_ | / /  _ \\_  ___/");
        logger.info("§b _  __/   / /_/ /_  / _  / _  /_/ / /_/ // /__ _  ,<       ____/ //  __/  /   __ |/ //  __/  /    ");
        logger.info("§b /_/      \\__,_/ /_/  /_/  /_.___/\\__,_/ \\___/ /_/|_|      /____/ \\___//_/    _____/ \\___//_/     ");

        logger.info("§7[§b!§7] Creating configuration files... §7[§b!§7]");

        fallingServerManager = new FallingServerManager();

        configTextFile = new TextFile(path, "config.yml");
        messagesTextFile = new TextFile(path, "messages.yml");

        loadCommands();

        loadListeners();

        loadStats(metricsFactory);

        task = server
                .getScheduler()
                .buildTask(this, new LobbyTask(fallingServerManager))
                .repeat(VelocityConfig.TASK_PERIOD.get(Integer.class), TimeUnit.SECONDS)
                .schedule();

        logger.info("§7[§b!§7] Plugin loaded successfully §7[§b!§7]");
        checkAlpha();
        checkUpdate();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("§7[§c!§7] §bFallbackServer §7is disabling.. §7[§c!§7]");
        task.cancel();
    }

    private void checkAlpha() {

        if (getVersion().contains("Alpha")) {
            isAlpha = true;
            getLogger().info(" ");
            getLogger().info("§7You're running an §c§lALPHA VERSION §7of Fallback Server.");
            getLogger().info("§7If you find any bugs, please report them on discord.");
            getLogger().info(" ");
        }

    }

    private void checkUpdate() {
        VelocityUtils.getUpdates().whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error("§7[§b!§7] An error occurred while checking for updates §7[§b!§7]");
                logger.error(throwable.getMessage());
                return;
            }

            if (result != null && result) {
                logger.info("§7[§b!§7] A new version of FallbackServerVelocity is available! §7[§b!§7]");
            }
        });
    }

    private void loadStats(VelocityMetrics.Factory factory) {

        logger.info("§7[§b!§7] Preparing telemetry... §7[§b!§7]");

        boolean shouldUseStatistics = VelocityConfig.TELEMETRY.get(Boolean.class);

        if (shouldUseStatistics) {
            factory.make(this, 12602);
        }
    }

    private void loadCommands() {
        logger.info("§7[§b!§7] Loading commands... §7[§b!§7]");

        server.getCommandManager().register("fsv", new FallbackVelocityCommand(this),
                "fallbackservervelocity",
                "fallbackserver");

        boolean isLobbyCommandEnabled = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);

        if (isLobbyCommandEnabled) {
            String[] aliases = VelocityConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]);

            CommandMeta commandMeta = server.getCommandManager()
                    .metaBuilder(VelocityConfig.LOBBY_ALIASES.getStringList().get(0))
                    .aliases(aliases)
                    .build();

            server.getCommandManager().register(commandMeta, new HubCommand(this));

        }
    }

    private void loadListeners() {

        logger.info("§7[§b!§7] Preparing events... §7[§b!§7]");

        server.getEventManager().register(this, new FallbackListener(this));

        boolean updateChecker = VelocityConfig.UPDATE_CHECKER.get(Boolean.class);
        boolean disabledServers = VelocityConfig.DISABLED_SERVERS.get(Boolean.class);

        if (updateChecker) {
            server.getEventManager().register(this, new PlayerListener());
        }

        if (disabledServers) {
            server.getEventManager().register(this, new CommandListener(this));
        }

    }

    public boolean isHub(String serverName) {
        List<String> list = new ArrayList<>();

        for (String lobby : VelocityConfig.LOBBIES_LIST.getStringList()) {
            String toLowerCase = lobby.toLowerCase();
            list.add(toLowerCase);
        }

        return list.contains(serverName.toLowerCase());
    }

    public String getVersion() {
        return VERSION;
    }

    public YamlFile getConfig() {
        return getConfigTextFile().getConfig();
    }


}
