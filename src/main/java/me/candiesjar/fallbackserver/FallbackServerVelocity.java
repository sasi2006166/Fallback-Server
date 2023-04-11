package me.candiesjar.fallbackserver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.commands.base.FallbackVelocityCommand;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.listeners.*;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.TextFile;
import me.candiesjar.fallbackserver.stats.VelocityMetrics;
import me.candiesjar.fallbackserver.utils.VelocityUtils;
import me.candiesjar.fallbackserver.utils.WorldUtil;
import me.candiesjar.fallbackserver.utils.tasks.LobbyTask;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "fallbackservervelocity",
        name = "FallbackServerVelocity",
        version = "3.1.3-Alpha1",
        url = "github.com/sasi2006166",
        authors = "CandiesJar",
        dependencies = {
                @Dependency(id = "limboapi", optional = true)
        }
)

@Getter
public class FallbackServerVelocity {

    @Getter
    public static final String VERSION = "3.1.3-Alpha1";

    @Getter
    private TextFile config, messages;

    @Getter
    private static FallbackServerVelocity instance;

    @Getter
    @Setter
    private boolean isAlpha = false;

    @Getter
    @Setter
    private boolean useAjQueue = false;

    @Getter
    @Setter
    private boolean useMaintenance = false;

    @Setter
    private boolean useLimbo = false;

    private final ProxyServer server;
    private final Logger logger;
    private final VelocityMetrics.Factory metricsFactory;
    private final Path path;

    private FallingServerManager fallingServerManager;
    private ScheduledTask task;

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

        loadDependencies();

        getLogger().info("\n" +
                "  _____     _ _ _                _     ____                           \n" +
                " |  ___|_ _| | | |__   __ _  ___| | __/ ___|  ___ _ ____   _____ _ __ \n" +
                " | |_ / _` | | | '_ \\ / _` |/ __| |/ /\\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|\n" +
                " |  _| (_| | | | |_) | (_| | (__|   <  ___) |  __/ |   \\ V /  __/ |   \n" +
                " |_|  \\__,_|_|_|_.__/ \\__,_|\\___|_|\\_\\|____/ \\___|_|    \\_/ \\___|_|   \n" +
                "                                                                      ");

        loadConfiguration();

        fallingServerManager = new FallingServerManager();

        loadCommands();

        checkPlugins();

        loadListeners();

        loadStats(metricsFactory);

        loadTask();

        getLogger().info("§7[§b!§7] Plugin loaded successfully §7[§b!§7]");
        checkAlpha();
        checkUpdate();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        getLogger().info("§7[§c!§7] §bFallbackServer §7is disabling.. §7[§c!§7]");
        task.cancel();
    }

    private void loadDependencies() {

        VelocityLibraryManager<FallbackServerVelocity> libraryManager = new VelocityLibraryManager<>(getLogger(),
                getPath(),
                getServer().getPluginManager(),
                this);

        Library library = Library.builder()
                .groupId("me{}carleslc{}Simple-YAML")
                .artifactId("Simple-Yaml")
                .version("1.8.4")
                .build();

        libraryManager.addJitPack();
        libraryManager.loadLibrary(library);
    }

    private void checkPlugins() {

        if (getServer().getPluginManager().getPlugin("ajQueue").isPresent()) {
            getLogger().info("§7[§b!§7] Enabling ajQueue API §7[§b!§7]");
            setUseAjQueue(true);
        }

        if (getServer().getPluginManager().getPlugin("Maintenance").isPresent()) {
            getLogger().info("§7[§b!§7] Enabling Maintenance API §7[§b!§7]");
            setUseMaintenance(true);
        }

        if (getServer().getPluginManager().getPlugin("limboapi").isPresent()) {
            getLogger().info("§7[§b!§7] Enabling LimboAPI §7[§b!§7]");
            setUseLimbo(true);
        }

    }

    private void loadConfiguration() {
        getLogger().info("§7[§b!§7] Creating configuration files... §7[§b!§7]");
        config = new TextFile(path, "config.yml");
        messages = new TextFile(path, "messages.yml");
    }

    private void loadTask() {
        task = server
                .getScheduler()
                .buildTask(this, new LobbyTask(fallingServerManager))
                .repeat(VelocityConfig.PING_DELAY.get(Integer.class), TimeUnit.SECONDS)
                .schedule();
    }

    private void checkAlpha() {

        if (getVERSION().contains("Alpha")) {
            setAlpha(true);
            getLogger().info(" ");
            getLogger().info("§7You're running an §c§lALPHA VERSION §7of Fallback Server.");
            getLogger().info("§7If you find any bugs, please report them on discord.");
            getLogger().info(" ");
        }

    }

    private void checkUpdate() {
        VelocityUtils.getUpdates().whenComplete((result, throwable) -> {
            if (throwable != null) {
                getLogger().error("§7[§b!§7] An error occurred while checking for updates §7[§b!§7]");
                getLogger().error(throwable.getMessage());
                return;
            }

            if (result != null && result) {
                getLogger().info("§7[§b!§7] A new version of FallbackServerVelocity is available! §7[§b!§7]");
            }
        });
    }

    private void loadStats(VelocityMetrics.Factory factory) {

        getLogger().info("§7[§b!§7] Preparing telemetry... §7[§b!§7]");

        boolean shouldUseStatistics = VelocityConfig.TELEMETRY.get(Boolean.class);

        if (shouldUseStatistics) {
            factory.make(this, 12602);
        }
    }

    private void loadCommands() {
        getLogger().info("§7[§b!§7] Loading commands... §7[§b!§7]");

        server.getCommandManager().register("fsv", new FallbackVelocityCommand(this),
                "fallbackserver",
                "fs");

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

        getLogger().info("§7[§b!§7] Preparing events... §7[§b!§7]");

        String mode = VelocityConfig.FALLBACK_MODE.get(String.class);

        switch (mode) {
            case "DEFAULT":
                server.getEventManager().register(this, new FallbackListener(this));
                getLogger().info("§7[§b!§7] Using default method §7[§b!§7]");
                break;
            case "RECONNECT":
                getLogger().info("§7[§b!§7] Trying reconnect method §7[§b!§7]");

                if (!useLimbo) {
                    getLogger().error("" +
                            "LimboAPI is missing from your plugins folder, for enabling " +
                            "reconnect method you need to install LimboAPI." +
                            "You can download it from https://www.spigotmc.org/resources/limboapi.95748/" +
                            "Using default method instead.");
                    server.getEventManager().register(this, new FallbackListener(this));
                    return;
                }

                WorldUtil.createWorld();
                server.getEventManager().register(this, new ReconnectListener());
                getLogger().info("§7[§b!§7] Enabled reconnect method §7[§b!§7]");
                break;
            default:
                getLogger().error("Configuration error under fallback_mode: " + VelocityConfig.FALLBACK_MODE.get(String.class));
                getLogger().error("Using default mode..");
                server.getEventManager().register(this, new FallbackListener(this));
                break;
        }

        boolean updateChecker = VelocityConfig.UPDATER.get(Boolean.class);
        boolean disabledServers = VelocityConfig.USE_COMMAND_BLOCKER.get(Boolean.class);

        if (updateChecker) {
            server.getEventManager().register(this, new PlayerListener(this));
        }

        if (disabledServers) {
            server.getEventManager().register(this, new CommandListener(this));
        }

    }

    public void cancelReconnect(UUID uuid) {
        FallbackLimboHandler limbo = PlayerCacheManager.getInstance().remove(uuid);
        if (limbo != null) {
            limbo.getReconnectTask().cancel();
            limbo.getTitleTask().cancel();
            limbo.clear();
        }
    }

    public boolean isHub(String serverName) {
        LinkedList<String> list = Lists.newLinkedList();

        for (String lobby : VelocityConfig.LOBBIES_LIST.getStringList()) {
            String toLowerCase = lobby.toLowerCase();
            list.add(toLowerCase);
        }

        return list.contains(serverName.toLowerCase());
    }

}
