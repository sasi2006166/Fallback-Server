package me.candiesjar.fallbackserver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.commands.base.FallbackVelocityCommand;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityVersion;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.listeners.*;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.TextFile;
import me.candiesjar.fallbackserver.stats.VelocityMetrics;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.WorldUtil;
import me.candiesjar.fallbackserver.utils.tasks.LobbyTask;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import org.slf4j.Logger;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "fallbackservervelocity",
        name = "FallbackServerVelocity",
        version = "3.2.0-Beta2",
        url = "github.com/sasi2006166",
        authors = "CandiesJar",
        dependencies = {
                @Dependency(id = "limboapi", optional = true)
        }
)

@Getter
public class FallbackServerVelocity {

    @Getter
    private static FallbackServerVelocity instance;

    @Getter
    public final String VERSION = "3.2.0-Beta2";

    @Getter
    private TextFile configTextFile, messagesTextFile, serversTextFile, versionTextFile;

    @Getter
    @Setter
    private boolean beta = false;

    @Getter
    @Setter
    private boolean ajQueue = false;

    @Getter
    @Setter
    private boolean maintenance = false;

    @Getter
    @Setter
    private boolean debug = false;

    @Setter
    private boolean limboApi = false;

    private final ProxyServer server;
    private final Logger logger;
    private final VelocityMetrics.Factory metricsFactory;
    private final PluginContainer pluginContainer;
    private final Path path;

    private FallingServerManager fallingServerManager;
    private PlayerCacheManager playerCacheManager;
    private ScheduledTask task;

    @Inject
    public FallbackServerVelocity(ProxyServer server, Logger logger, VelocityMetrics.Factory metricsFactory, PluginContainer pluginContainer, @DataDirectory Path path) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.pluginContainer = pluginContainer;
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
        updateConfiguration();

        fallingServerManager = new FallingServerManager();
        playerCacheManager = PlayerCacheManager.getInstance();

        loadCommands();

        checkPlugins();

        loadListeners();

        loadStats(metricsFactory);

        loadTask();

        getLogger().info("§7[§b!§7] Plugin loaded successfully");
        checkForBeta();

        checkUpdate();

        checkDebug();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        getLogger().info("§7[§c!§7] §bFallbackServer §7is disabling..");
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

        Library updater = Library.builder()
                .groupId("ru{}vyarus")
                .artifactId("yaml-config-updater")
                .version("1.4.2")
                .build();

        libraryManager.addJitPack();
        libraryManager.addMavenCentral();
        libraryManager.loadLibrary(library);
        libraryManager.loadLibrary(updater);
    }

    private void checkDebug() {
        boolean useDebug = VelocityConfig.DEBUG_MODE.get(Boolean.class);

        if (useDebug) {
            setDebug(true);
            getLogger().warn(" ");
            getLogger().warn("You are using the debug mode");
            getLogger().warn("which can cause a lot of spam in the console");
            getLogger().warn("Remember that this mode should be enabled");
            getLogger().warn("only if developer is asking for it");
            getLogger().warn("or if you want to report a bug.");
            getLogger().warn("Thanks for using FallbackServer!");
            getLogger().warn(" ");
        }

    }

    private void checkPlugins() {

        if (getServer().getPluginManager().getPlugin("ajQueue").isPresent()) {
            getLogger().info("§7[§b!§7] Hooking in AjQueue");
            setAjQueue(true);
        }

        if (getServer().getPluginManager().getPlugin("Maintenance").isPresent()) {
            getLogger().info("§7[§b!§7] Hooking in Maintenance");
            setMaintenance(true);
        }

        if (getServer().getPluginManager().getPlugin("limboapi").isPresent()) {
            getLogger().info("§7[§b!§7] Hooking in LimboAPI");
            setLimboApi(true);
        }

    }

    private void loadConfiguration() {
        configTextFile = new TextFile(path, "config.yml");
        messagesTextFile = new TextFile(path, "messages.yml");
        serversTextFile = new TextFile(path, "servers.yml");
        versionTextFile = new TextFile(path, "version.yml");
    }

    private void loadTask() {
        task = server
                .getScheduler()
                .buildTask(this, new LobbyTask())
                .repeat(VelocityConfig.PING_DELAY.get(Integer.class), TimeUnit.SECONDS)
                .schedule();
    }

    private void checkForBeta() {
        if (getVERSION().contains("Alpha") || getVERSION().contains("Beta")) {
            setBeta(true);
            getLogger().info(" ");
            getLogger().info("§7You're running an §c§lBETA VERSION §7of Fallback Server.");
            getLogger().info("§7If you find any bugs, please report them on discord.");
            getLogger().info(" ");
        }
    }

    private void checkUpdate() {
        Utils.getUpdates().whenComplete((result, throwable) -> {
            if (throwable != null) {
                getLogger().error("§7[§b!§7] An error occurred while checking for updates");
                getLogger().error(throwable.getMessage());
                return;
            }

            if (result != null && result) {
                getLogger().info("§7[§b!§7] A new version of FallbackServerVelocity is available!");
            }
        });
    }

    private void updateConfiguration() {

        if (pluginContainer.getDescription().getVersion().isEmpty()) {
            return;
        }

        if (pluginContainer.getDescription().getVersion().get().equals(VelocityVersion.VERSION.getString())) {
            return;
        }

        getLogger().info("§7[§b!§7] Updating configuration...");
        YamlUpdater.create(new File(getPath() + "/config.yml"), FileUtils.findFile("https://raw.githubusercontent.com/sasi2006166/Fallback-Server/main/src/main/resources/config.yml"))
                .backup(true)
                .update();
        YamlUpdater.create(new File(getPath() + "/messages.yml"), FileUtils.findFile("https://raw.githubusercontent.com/sasi2006166/Fallback-Server/main/src/main/resources/messages.yml"))
                .backup(true)
                .update();
        versionTextFile.getConfig().set("version", pluginContainer.getDescription().getVersion());
        versionTextFile.save();
        loadConfiguration();
    }

    private void loadStats(VelocityMetrics.Factory factory) {
        boolean shouldUseStatistics = VelocityConfig.TELEMETRY.get(Boolean.class);

        if (shouldUseStatistics) {
            factory.make(this, 12602);
        }
    }

    private void loadCommands() {
        getLogger().info("§7[§b!§7] Loading commands...");

        server.getCommandManager().register("fsv", new FallbackVelocityCommand(this, this),
                "fallbackserver",
                "fs");

        boolean isLobbyCommandEnabled = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);

        if (isLobbyCommandEnabled) {
            String[] aliases = VelocityConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]);

            if (aliases.length == 0) {
                getLogger().error("§7[§c!§7] §cYou have to set at least one alias for the lobby command!");
                getLogger().error("§7[§c!§7] §cDisabling lobby command..");
                return;
            }

            CommandMeta commandMeta = server.getCommandManager()
                    .metaBuilder(VelocityConfig.LOBBY_ALIASES.getStringList().get(0))
                    .aliases(aliases)
                    .build();

            server.getCommandManager().register(commandMeta, new HubCommand(this));
        }
    }

    private void loadListeners() {
        getLogger().info("§7[§b!§7] Preparing events...");

        String mode = VelocityConfig.FALLBACK_MODE.get(String.class);

        switch (mode) {
            case "DEFAULT":
                server.getEventManager().register(this, new FallbackListener(this));
                getLogger().info("§7[§b!§7] Using default method");
                break;
            case "RECONNECT":
                loadReconnect();
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

    private void loadReconnect() {
        getLogger().info("§7[§b!§7] Running pre-checks");

        if (!limboApi) {
            getLogger().error(
                    "\nLimboAPI is missing from your plugins folder, for enabling \n" +
                            "reconnect method you need to install LimboAPI. \n" +
                            "You can download it from https://github.com/Elytrium/LimboAPI/releases \n" +
                            "Switching to fallback method.");
            server.getEventManager().register(this, new FallbackListener(this));
            return;
        }

        WorldUtil.createWorld();
        server.getEventManager().register(this, new ServerSwitchListener(this));
        server.getEventManager().register(this, new ReconnectListener(this));
        getLogger().info("§7[§b!§7] Enabled reconnect method");
    }

    public void cancelReconnect(UUID uuid) {
        FallbackLimboHandler limboHandler = playerCacheManager.remove(uuid);
        if (limboHandler != null) {
            limboHandler.getReconnectTask().cancel();
            limboHandler.getTitleTask().cancel();
            if (limboHandler.getConnectTask() != null) {
                limboHandler.getConnectTask().cancel();
            }
            limboHandler.clear();
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

    public void reloadListeners() {

        getServer().getEventManager().unregisterListener(this, new FallbackListener(this));

        if (limboApi) {
            getServer().getEventManager().unregisterListener(this, new ReconnectListener(this));
        }

        String mode = VelocityConfig.FALLBACK_MODE.get(String.class);

        switch (mode) {
            case "DEFAULT":
                getServer().getEventManager().register(this, new FallbackListener(this));
                break;
            case "RECONNECT":
                loadReconnect();
                break;
            default:
                getLogger().error("Configuration error under fallback_mode: " + VelocityConfig.FALLBACK_MODE.get(String.class));
                getLogger().error("Using default mode..");
                getServer().getEventManager().register(this, new FallbackListener(this));
                break;
        }

    }

    public void reloadAll() {
        task.cancel();
        loadTask();
        configTextFile.reload();
        messagesTextFile.reload();
        serversTextFile.reload();
    }

}
