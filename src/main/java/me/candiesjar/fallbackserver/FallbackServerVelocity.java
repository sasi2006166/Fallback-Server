package me.candiesjar.fallbackserver;

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
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.commands.base.FallbackVelocityCommand;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityVersion;
import me.candiesjar.fallbackserver.listeners.*;
import me.candiesjar.fallbackserver.objects.text.TextFile;
import me.candiesjar.fallbackserver.stats.VelocityMetrics;
import me.candiesjar.fallbackserver.utils.LoaderUtil;
import me.candiesjar.fallbackserver.utils.PluginUtil;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.WorldUtil;
import me.candiesjar.fallbackserver.utils.checks.OutdatedChecks;
import me.candiesjar.fallbackserver.utils.tasks.PingTask;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

@Plugin(
        id = "fallbackservervelocity",
        name = "FallbackServerVelocity",
        version = "3.2.0-Beta3.5",
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
    private TextFile configTextFile, messagesTextFile, serversTextFile, versionTextFile;

    @Getter
    public final String version = "3.2.0-Beta3.5";

    @Setter
    private boolean limboApi = false;

    @Getter
    private Pattern pattern;

    @Getter
    private MiniMessage miniMessage;

    @Getter
    @Setter
    private boolean reconnect = false;

    @Getter
    @Setter
    private boolean beta = false;

    @Getter
    @Setter
    private boolean outdated = false;

    @Getter
    @Setter
    private boolean ajQueue = false;

    @Getter
    @Setter
    private boolean maintenance = false;

    @Getter
    @Setter
    private boolean debug = false;

    private final ProxyServer server;
    private final Logger logger;
    private final ComponentLogger componentLogger;
    private final VelocityMetrics.Factory metricsFactory;
    private final PluginContainer pluginContainer;

    @Getter
    private final Path path;

    private ServerTypeManager serverTypeManager;
    private OnlineLobbiesManager onlineLobbiesManager;
    private PlayerCacheManager playerCacheManager;

    @Getter
    private final ChannelIdentifier reconnectIdentifier = MinecraftChannelIdentifier.create("fs", "reconnect");

    @Inject
    public FallbackServerVelocity(ProxyServer server, Logger logger, VelocityMetrics.Factory metricsFactory, PluginContainer pluginContainer, @DataDirectory Path path, ComponentLogger componentLogger) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.pluginContainer = pluginContainer;
        this.path = path;
        this.componentLogger = componentLogger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        miniMessage = miniMessage();
        serverTypeManager = ServerTypeManager.getInstance();
        onlineLobbiesManager = OnlineLobbiesManager.getInstance();
        playerCacheManager = PlayerCacheManager.getInstance();
        pattern = Pattern.compile("#[a-fA-F0-9]{6}");

        getComponentLogger().info(getMiniMessage().deserialize("""
                    <gradient:#00ffff:#0055ff>
                      _____     _ _ _                _     ____                         \s
                     |  ___|_ _| | | |__   __ _  ___| | __/ ___|  ___ _ ____   _____ _ __
                     | |_ / _` | | | '_ \\ / _` |/ __| |/ /\\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|
                     |  _| (_| | | | |_) | (_| | (__|   <  ___) |  __/ |   \\ V /  __/ |  \s
                     |_|  \\__,_|_|_|_.__/ \\__,_|\\___|_|\\_\\|____/ \\___|_|    \\_/ \\___|_| \s
                    </gradient>"""));

        loadDependencies();
        loadConfiguration();

        checkOutdated();
        updateConfiguration();

        checkPlugins();

        loadServers();

        loadCommands();

        loadListeners();

        loadStats(metricsFactory);

        loadTask();

        getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] <aqua>FallbackServer <gray>loaded successfully"));
        checkBeta();

        checkUpdate();

        checkDebug();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] <aqua>FallbackServer <gray>is disabling.."));
    }

    private void loadDependencies() {
        VelocityLibraryManager<FallbackServerVelocity> libraryManager = new VelocityLibraryManager<>(getComponentLogger(),
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

    private void loadConfiguration() {
        configTextFile = new TextFile(path, "config.yml");
        messagesTextFile = new TextFile(path, "messages.yml");
        serversTextFile = new TextFile(path, "servers.yml");
        versionTextFile = new TextFile(path, "version.yml");
    }

    private void updateConfiguration() {
        if (!outdated && pluginContainer.getDescription().getVersion().isEmpty()) {
            return;
        }

        if (!outdated && pluginContainer.getDescription().getVersion().get().equals(VelocityVersion.VERSION.getString())) {
            return;
        }

        getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Updating configuration..."));
        YamlUpdater.create(new File(getPath() + "/config.yml"), FileUtils.findFile("https://raw.githubusercontent.com/sasi2006166/Fallback-Server/main/src/main/resources/config.yml"))
                .backup(true)
                .update();
        YamlUpdater.create(new File(getPath() + "/messages.yml"), FileUtils.findFile("https://raw.githubusercontent.com/sasi2006166/Fallback-Server/main/src/main/resources/messages.yml"))
                .backup(true)
                .update();
        versionTextFile.getConfig().set("version", pluginContainer.getDescription().getVersion().get());
        versionTextFile.save();
        loadConfiguration();
    }

    private void loadCommands() {
        getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Loading commands..."));

        CommandMeta meta = server.getCommandManager().metaBuilder("fallbackserver")
                .aliases("fsv", "fallback", "fs")
                .build();

        server.getCommandManager().register(meta, new FallbackVelocityCommand(this, this));

        boolean isLobbyCommandEnabled = VelocityConfig.LOBBY_COMMAND.get(Boolean.class);

        if (isLobbyCommandEnabled) {
            String[] aliases = VelocityConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]);

            if (aliases.length == 0) {
                getComponentLogger().error(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] §cYou have to set at least one alias for the lobby command!"));
                getComponentLogger().error(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] §cUsing default 'hub' alias."));
                aliases = new String[]{"hub"};
            }

            CommandMeta commandMeta = server.getCommandManager()
                    .metaBuilder(VelocityConfig.LOBBY_ALIASES.getStringList().get(0))
                    .aliases(aliases)
                    .build();

            server.getCommandManager().register(commandMeta, new HubCommand(this));
        }
    }

    private void checkPlugins() {
        if (getServer().getPluginManager().getPlugin("ajQueue").isPresent()) {
            setAjQueue(true);
        }

        if (getServer().getPluginManager().getPlugin("Maintenance").isPresent()) {
            PluginContainer container = getServer().getPluginManager().getPlugin("Maintenance").get();
            String author = container.getDescription().getAuthors().toString();

            if (!author.equalsIgnoreCase("kennytv")) {
                return;
            }

            setMaintenance(true);
        }

        if (getServer().getPluginManager().getPlugin("limboapi").isPresent()) {
            setLimboApi(true);
        }

        if (getServer().getPluginManager().getPlugin("velocity-tools").isPresent()) {
            PluginUtil.handle();
        }
    }

    private void loadListeners() {
        getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Preparing events..."));
        server.getEventManager().register(this, new KickListener(this));

        boolean updateChecker = VelocityConfig.UPDATER.get(Boolean.class);
        boolean disabledServers = VelocityConfig.USE_COMMAND_BLOCKER.get(Boolean.class);
        boolean joinSorting = VelocityConfig.JOIN_BALANCING.get(Boolean.class);

        if (updateChecker) {
            server.getEventManager().register(this, new PlayerListener(this));
        }

        if (disabledServers) {
            server.getEventManager().register(this, new CommandListener(this));
        }

        if (joinSorting) {
            server.getEventManager().register(this, new JoinListener(this));
        }
    }

    private void loadStats(VelocityMetrics.Factory factory) {
        boolean shouldUseStatistics = VelocityConfig.TELEMETRY.get(Boolean.class);

        if (shouldUseStatistics) {
            factory.make(this, 12602);
        }
    }

    private void loadTask() {
        String mode = VelocityConfig.PING_MODE.get(String.class);
        getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Using <aqua>" + mode + " <gray>mode for pinging"));
        PingTask.start(mode);
    }

    private void checkBeta() {
        if (getVersion().contains("Beta")) {
            setBeta(true);
            getComponentLogger().warn(" ");
            getComponentLogger().warn(getMiniMessage().deserialize("<gray>You're running a <red><bold>BETA VERSION <gray>of the plugin."));
            getComponentLogger().warn(getMiniMessage().deserialize("<gray>Updater is disabled for debugging purposes."));
            getComponentLogger().warn(getMiniMessage().deserialize("<gray>If you find any bugs, please report them on discord."));
            getComponentLogger().warn(" ");
        }
    }

    private void checkUpdate() {
        Utils.getUpdates().whenComplete((result, throwable) -> {
            if (throwable != null) {
                getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] An error occurred while checking for updates"));
                getComponentLogger().info(getMiniMessage().deserialize(throwable.getMessage()));
                return;
            }

            if (result != null && result) {
                getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] A new version of FallbackServerVelocity is available!"));
            }
        });
    }

    private void checkDebug() {
        boolean useDebug = VelocityConfig.DEBUG.get(Boolean.class);

        if (useDebug) {
            setDebug(true);
            getComponentLogger().warn(" ");
            getComponentLogger().warn("You are using the debug mode");
            getComponentLogger().warn("Enable this mode only if developer");
            getComponentLogger().warn("is asking for it.");
            getComponentLogger().warn(" ");
        }
    }

    private void checkOutdated() {
        OutdatedChecks.handle();

        if (outdated) {
            getComponentLogger().error(" ");
            getComponentLogger().error("Your configuration is outdated!");
            getComponentLogger().error("Please update your configuration");
            getComponentLogger().error("by deleting the old one and restarting");
            getComponentLogger().error("the server.");
            getComponentLogger().error(" ");
        }

    }

    public void loadServers() {
        LoaderUtil.loadServers(getConfigTextFile().getConfig().getConfigurationSection("settings.fallback"));
        LoaderUtil.loadServers(getServersTextFile().getConfig().getConfigurationSection("servers"));
    }

    public void loadReconnect() {
        if (reconnect) {
            return;
        }

        getComponentLogger().info(getMiniMessage().deserialize("<gray>[<aqua>!<gray>] Running machine checks"));

        if (!limboApi) {
            getComponentLogger().error("LimboAPI is missing from your plugins folder, for enabling");
            getComponentLogger().error("reconnect method you need to install LimboAPI.");
            getComponentLogger().error("You can download it from https://github.com/Elytrium/LimboAPI/releases");
            setReconnect(false);
            return;
        }

        int coreCount = Runtime.getRuntime().availableProcessors();

        if (coreCount < 2) {
            getComponentLogger().error("You're using a single core for your proxy.");
            getComponentLogger().error("There is no issue with this, but in long term ");
            getComponentLogger().error("it may cause performance issues.");
        }

        WorldUtil.createLimbo();
        server.getEventManager().register(this, new ServerSwitchListener(this));
        server.getEventManager().register(this, new ReconnectListener(this));
        boolean physical = VelocityConfig.RECONNECT_USE_PHYSICAL.get(Boolean.class);

        if (physical) server.getChannelRegistrar().register(reconnectIdentifier);
        setReconnect(true);
    }

    public void reloadAll() {
        PingTask.getScheduledTask().cancel();
        getServerTypeManager().clear();
        getOnlineLobbiesManager().clear();
        configTextFile.reload();
        messagesTextFile.reload();
        serversTextFile.reload();
        loadServers();
        PingTask.reload();
    }

}
