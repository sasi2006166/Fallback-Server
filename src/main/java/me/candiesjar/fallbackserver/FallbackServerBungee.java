package me.candiesjar.fallbackserver;

import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.commands.base.SubCommandManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeVersion;
import me.candiesjar.fallbackserver.handlers.ReconnectHandler;
import me.candiesjar.fallbackserver.listeners.*;
import me.candiesjar.fallbackserver.metrics.BungeeMetrics;
import me.candiesjar.fallbackserver.objects.TextFile;
import me.candiesjar.fallbackserver.utils.FilesUtils;
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
import java.util.UUID;

public final class FallbackServerBungee extends Plugin {

    @Getter
    private static FallbackServerBungee instance;

    @Getter
    private TextFile configTextFile, messagesTextFile, versionTextFile, serversTextFile;

    @Getter
    private String version;

    @Getter
    private ServerInfo reconnectServer;

    @Getter
    private PlayerCacheManager playerCacheManager;

    @Getter
    @Setter
    private boolean firstEnable = true;

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
    private boolean needsUpdate = false;

    @Getter
    @Setter
    private boolean isDebug = false;

    @Getter
    @Setter
    private boolean isReconnect = false;

    public void onEnable() {
        instance = this;
        version = getDescription().getVersion();
        playerCacheManager = PlayerCacheManager.getInstance();

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

        checkDebug();

        checkPlugins();

        loadListeners();

        loadCommands();

        startMetrics();

        getLogger().info("§7[§b!§7] Plugin loaded successfully");
        PingTask.start();

        checkForBeta();

        UpdateUtil.checkUpdates();
    }

    public void onDisable() {
        if (needsUpdate) {
            getLogger().info("§7[§b!§7] §7Installing new update..");
            FilesUtils.deleteFile(getFile().getName(), getDataFolder());
        }

        getLogger().info("§7[§c!§7] §bFallbackServer §7is disabling..");
    }

    private void checkDebug() {
        boolean useDebug = BungeeConfig.USE_DEBUG.getBoolean();

        if (useDebug) {
            setDebug(true);
            getLogger().warning(" ");
            getLogger().warning("You are using the debug mode");
            getLogger().warning("which can cause a lot of spam in the console");
            getLogger().warning("Remember that this mode should be enabled");
            getLogger().warning("only if developer is asking for it");
            getLogger().warning("or if you want to report a bug.");
            getLogger().warning("Thanks for using FallbackServer!");
            getLogger().warning(" ");
        }

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

    private void checkForBeta() {
        if (version.contains("Beta")) {
            setBeta(true);
            getLogger().warning(" ");
            getLogger().warning("§7You're running a §c§lBETA VERSION §7of the plugin.");
            getLogger().warning("§7Updater is disabled for debugging purposes.");
            getLogger().warning("§7If you find any bugs, please report them on discord.");
            getLogger().warning(" ");
        }
    }

    private void loadConfiguration() {
        configTextFile = new TextFile(this, "config.yml");
        messagesTextFile = new TextFile(this, "messages.yml");
        serversTextFile = new TextFile(this, "servers.yml");
        versionTextFile = new TextFile(this, "version.yml");
    }

    private void loadCommands() {
        getLogger().info("§7[§b!§7] Preparing commands..");
        getProxy().getPluginManager().registerCommand(this, new SubCommandManager(this));

        boolean lobbyCommand = BungeeConfig.LOBBY_COMMAND.getBoolean();

        if (lobbyCommand) {

            List<String> aliases = BungeeConfig.LOBBY_ALIASES.getStringList();

            if (aliases.isEmpty()) {
                getLogger().severe("§7[§c!§7] §cYou have to set at least one alias for the lobby command!");
                getLogger().severe("§7[§c!§7] §cDisabling lobby command..");
                return;
            }

            getProxy().getPluginManager().registerCommand(this, new HubCommand(this));
        }
    }

    private void loadListeners() {
        getLogger().info("§7[§b!§7] Starting all listeners..");

        getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(this));
        getProxy().getPluginManager().registerListener(this, new MoveListener(this));
        String mode = BungeeConfig.FALLBACK_MODE.getString();

        switch (mode) {
            case "DEFAULT":
                getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
                getLogger().info("§7[§b!§7] Using default method");
                break;
            case "RECONNECT":
                setReconnect(true);
                getProxy().getPluginManager().registerListener(this, new ReconnectListener(this));
                getLogger().info("§7[§b!§7] Using reconnect method");

                boolean physicalServer = BungeeConfig.RECONNECT_USE_SERVER.getBoolean();

                if (!physicalServer) {
                    break;
                }

                reconnectServer = getProxy().getServerInfo(BungeeConfig.RECONNECT_SERVER.getString());

                if (reconnectServer == null) {
                    getLogger().severe("The server " + BungeeConfig.RECONNECT_SERVER.getString() + " does not exist!");
                    getLogger().severe("Check your config.yml file for more infos.");
                    getLogger().severe("Please add it and RESTART your proxy.");
                    getLogger().severe("Moving to limbo mode instead.");
                }

                break;
            default:
                getLogger().severe("Configuration error under fallback_mode: " + BungeeConfig.FALLBACK_MODE.getString());
                getLogger().severe("Using default mode..");
                getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
                break;
        }

        boolean disabledServers = BungeeConfig.USE_COMMAND_BLOCKER.getBoolean();
        boolean checkUpdates = BungeeConfig.UPDATER.getBoolean();

        if (disabledServers) {
            getProxy().getPluginManager().registerListener(this, new ChatListener());
        }

        if (checkUpdates) {
            getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        }

    }

    private void startMetrics() {
        boolean telemetry = BungeeConfig.TELEMETRY.getBoolean();

        if (telemetry) {
            getLogger().info("§7[§b!§7] Starting telemetry service...");
            new BungeeMetrics(this, 11817);
        }
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

    public void cancelReconnect(UUID uuid) {
        ReconnectHandler task = playerCacheManager.remove(uuid);

        if (task == null) {
            return;
        }

        task.getReconnectTask().cancel();

        if (task.getTitleTask() != null) {
            task.getTitleTask().cancel();
        }

        if (task.getConnectTask() != null) {
            task.getConnectTask().cancel();
        }

        task.clear();
    }

    public void reloadTask() {
        PingTask.reload();
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
}
