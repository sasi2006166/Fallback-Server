package me.candiesjar.fallbackserver;

import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.commands.core.HubCommand;
import me.candiesjar.fallbackserver.commands.core.SubCommandManager;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeVersion;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.listeners.*;
import me.candiesjar.fallbackserver.metrics.BungeeMetrics;
import me.candiesjar.fallbackserver.objects.text.TextFile;
import me.candiesjar.fallbackserver.utils.FilesUtils;
import me.candiesjar.fallbackserver.utils.FallbackGroupsLoader;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.UpdateUtil;
import me.candiesjar.fallbackserver.utils.tasks.PingTask;
import net.byteflux.libby.BungeeLibraryManager;
import net.byteflux.libby.Library;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import ru.vyarus.yaml.updater.YamlUpdater;
import ru.vyarus.yaml.updater.util.FileUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public final class FallbackServerBungee extends Plugin {

    @Getter
    private static FallbackServerBungee instance;

    @Getter
    private TextFile configTextFile, messagesTextFile, versionTextFile, serversTextFile;

    @Getter
    private String version;

    @Getter
    private PlayerCacheManager playerCacheManager;

    @Getter
    private ServerTypeManager serverTypeManager;

    @Getter
    private OnlineLobbiesManager onlineLobbiesManager;

    @Getter
    private Pattern pattern;

    @Getter
    @Setter
    private ServerInfo reconnectServer = null;

    @Getter
    @Setter
    private boolean ajQueue = false;

    @Getter
    @Setter
    private boolean maintenance = false;

    @Getter
    @Setter
    private boolean needsUpdate = false;

    @Getter
    @Setter
    private boolean isDebug = false;

    public void onEnable() {
        instance = this;
        version = getDescription().getVersion();
        playerCacheManager = PlayerCacheManager.getInstance();
        serverTypeManager = ServerTypeManager.getInstance();
        onlineLobbiesManager = OnlineLobbiesManager.getInstance();
        pattern = Pattern.compile("&#([a-fA-F0-9]{6})");

        getLogger().info("\n" +
                "  _____     _ _ _                _     ____                           \n" +
                " |  ___|_ _| | | |__   __ _  ___| | __/ ___|  ___ _ ____   _____ _ __ \n" +
                " | |_ / _` | | | '_ \\ / _` |/ __| |/ /\\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|\n" +
                " |  _| (_| | | | |_) | (_| | (__|   <  ___) |  __/ |   \\ V /  __/ |   \n" +
                " |_|  \\__,_|_|_|_.__/ \\__,_|\\___|_|\\_\\|____/ \\___|_|    \\_/ \\___|_|   \n" +
                "                                                                      ");

        loadDependencies();
        loadConfiguration();
        updateConfiguration();

        checkPlugins();

        loadServers();

        loadListeners();

        loadCommands();

        loadTelemetry();

        checkVersion();

        getLogger().info("§7[§b!§7] Plugin loaded successfully");
        checkDebug();

        ErrorHandler.deleteLogFile();

        startPinging();
    }

    public void onDisable() {
        if (needsUpdate) {
            getLogger().info("§7[§b!§7] §7Installing new update..");
            FilesUtils.deleteFile(getFile().getName(), getDataFolder());
        }

        getLogger().info("§7[§c!§7] §bFallbackServer §7is disabling..");
    }

    private void loadDependencies() {
        BungeeLibraryManager bungeeLibraryManager = new BungeeLibraryManager(this);

        Library updater = Library.builder()
                .groupId("ru{}vyarus")
                .artifactId("yaml-config-updater")
                .version("1.4.2")
                .build();

        bungeeLibraryManager.addMavenCentral();
        bungeeLibraryManager.loadLibrary(updater);
    }

    private void loadConfiguration() {
        configTextFile = new TextFile(this, "config.yml");
        messagesTextFile = new TextFile(this, "messages.yml");
        serversTextFile = new TextFile(this, "servers.yml");
        versionTextFile = new TextFile(this, "version.yml");
    }

    private void updateConfiguration() {
        if (getDescription().getVersion().equals(BungeeVersion.VERSION.getString())) {
            return;
        }

        getLogger().info("§7[§b!§7] Updating configuration...");
        YamlUpdater.create(new File(getDataFolder().toPath() + "/config.yml"), FileUtils.findFile("https://raw.githubusercontent.com/sasi2006166/Fallback-Server/main/src/main/resources/config.yml"))
                .backup(true)
                .update();
        YamlUpdater.create(new File(getDataFolder().toPath() + "/messages.yml"), FileUtils.findFile("https://raw.githubusercontent.com/sasi2006166/Fallback-Server/main/src/main/resources/messages.yml"))
                .backup(true)
                .update();
        versionTextFile.getConfig().set("version", getDescription().getVersion());
        versionTextFile.save();

        loadConfiguration();
    }

    private void checkPlugins() {
        if (getProxy().getPluginManager().getPlugin("ajQueue") != null) {
            getLogger().info("§7[§b!§7] Hooked in ajQueue");
            setAjQueue(true);
        }

        if (getProxy().getPluginManager().getPlugin("Maintenance") != null) {
            String author = getProxy().getPluginManager().getPlugin("Maintenance").getDescription().getAuthor();

            if (!author.equals("kennytv")) {
                return;
            }

            getLogger().info("§7[§b!§7] Hooked in Maintenance");
            setMaintenance(true);
        }
    }

    private void loadListeners() {
        getLogger().info("§7[§b!§7] Starting all listeners..");

        getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerKickListener(this));

        ServerInfo serverInfo = ReconnectUtil.checkForPhysicalServer();
        setReconnectServer(serverInfo);

        boolean disabledServers = BungeeConfig.USE_COMMAND_BLOCKER.getBoolean();
        boolean checkUpdates = BungeeConfig.UPDATER.getBoolean();
        boolean joinSorting = BungeeConfig.JOIN_BALANCING.getBoolean();

        if (disabledServers) {
            getProxy().getPluginManager().registerListener(this, new ChatEventListener());
        }

        if (checkUpdates) {
            getProxy().getPluginManager().registerListener(this, new GeneralPlayerListener(this));
        }

        if (joinSorting) {
            getProxy().getPluginManager().registerListener(this, new PlayerJoinListener(this));
        }

    }

    private void loadCommands() {
        getLogger().info("§7[§b!§7] Preparing commands..");
        getProxy().getPluginManager().registerCommand(this, new SubCommandManager(this));

        boolean lobbyCommand = BungeeConfig.LOBBY_COMMAND.getBoolean();

        if (lobbyCommand) {
            List<String> aliases = BungeeConfig.LOBBY_ALIASES.getStringList();

            if (aliases.isEmpty()) {
                ErrorHandler.add(Severity.WARNING, "Optional, but you have to set at least one alias for the lobby command");
                return;
            }

            getProxy().getPluginManager().registerCommand(this, new HubCommand(this));
        }
    }

    private void loadTelemetry() {
        boolean telemetry = BungeeConfig.TELEMETRY.getBoolean();

        if (telemetry) {
            getLogger().info("§7[§b!§7] Starting telemetry service...");
            new BungeeMetrics(this, 11817);
        }
    }

    private void checkVersion() {
        UpdateUtil.checkUpdates();
    }

    private void startPinging() {
        String strategy = BungeeConfig.PING_STRATEGY.getString();
        PingTask.start(strategy);
    }

    private void checkDebug() {
        boolean useDebug = BungeeConfig.USE_DEBUG.getBoolean();

        if (useDebug) {
            setDebug(true);
            getLogger().warning(" ");
            getLogger().warning("You are using the debug mode");
            getLogger().warning("Enable this mode only if developer");
            getLogger().warning("is asking for it.");
            getLogger().warning(" ");
        }

    }

    public void loadServers() {
        Configuration defaultFallback = getConfig().getSection("settings.fallback");
        new FallbackGroupsLoader(serverTypeManager, onlineLobbiesManager).loadServers(defaultFallback, true);
        Configuration additionalServers = getServersConfig().getSection("servers");
        new FallbackGroupsLoader(serverTypeManager, onlineLobbiesManager).loadServers(additionalServers, false);
    }

    public Configuration getConfig() {
        return configTextFile.getConfig();
    }

    public Configuration getMessagesConfig() {
        return messagesTextFile.getConfig();
    }

    public Configuration getServersConfig() {
        return serversTextFile.getConfig();
    }

    public Configuration getVersionConfig() {
        return versionTextFile.getConfig();
    }

    public void reloadTask() {
        PingTask.reload();
    }

}
