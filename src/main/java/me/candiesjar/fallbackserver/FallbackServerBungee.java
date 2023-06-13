package me.candiesjar.fallbackserver;

import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.cache.ServerCacheManager;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.commands.base.SubCommandManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.listeners.*;
import me.candiesjar.fallbackserver.metrics.BungeeMetrics;
import me.candiesjar.fallbackserver.objects.TextFile;
import me.candiesjar.fallbackserver.utils.FileUtils;
import me.candiesjar.fallbackserver.utils.UpdateUtil;
import me.candiesjar.fallbackserver.utils.tasks.PingTask;
import me.candiesjar.fallbackserver.handlers.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.UUID;

public final class FallbackServerBungee extends Plugin {

    @Getter
    private static FallbackServerBungee instance;

    @Getter
    private TextFile configTextFile;

    @Getter
    private TextFile messagesTextFile;

    @Getter
    private String version;

    @Getter
    @Setter
    private boolean isAlpha = false;

    @Getter
    @Setter
    private boolean useAjQueue = false;

    @Getter
    @Setter
    private boolean useMaintenance = false;

    @Getter
    @Setter
    private boolean needsUpdate = false;

    @Getter
    @Setter
    private boolean isDebug = false;

    @Getter
    private PlayerCacheManager playerCacheManager;

    @Getter
    private ServerCacheManager serverCacheManager;

    public void onEnable() {
        instance = this;

        version = getDescription().getVersion();

        getLogger().info("\n" +
                "  _____     _ _ _                _     ____                           \n" +
                " |  ___|_ _| | | |__   __ _  ___| | __/ ___|  ___ _ ____   _____ _ __ \n" +
                " | |_ / _` | | | '_ \\ / _` |/ __| |/ /\\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|\n" +
                " |  _| (_| | | | |_) | (_| | (__|   <  ___) |  __/ |   \\ V /  __/ |   \n" +
                " |_|  \\__,_|_|_|_.__/ \\__,_|\\___|_|\\_\\|____/ \\___|_|    \\_/ \\___|_|   \n" +
                "                                                                      ");

        loadConfiguration();

        playerCacheManager = PlayerCacheManager.getInstance();
        serverCacheManager = ServerCacheManager.getInstance();

        checkDebug();

        checkPlugins();

        loadListeners();

        loadCommands();

        startMetrics();

        getLogger().info("§7[§b!§7] Plugin loaded successfully §7[§b!§7]");
        PingTask.start();

        checkAlpha();

        UpdateUtil.checkUpdates();

    }

    public void onDisable() {

        if (needsUpdate) {
            getLogger().info("§7[§b!§7] §7Installing new update.. §7[§b!§7]");
            FileUtils.deleteFile(getFile().getName(), getDataFolder());
        }

        getLogger().info("§7[§c!§7] §bFallbackServer §7is disabling.. §7[§c!§7]");
    }

    private void checkDebug() {

        boolean useDebug = BungeeConfig.DEBUG_MODE.getBoolean();

        if (useDebug) {
            getLogger().severe("§7[§c!§7] Debug mode is enabled §7[§c!§7]");
            setDebug(true);
        }

    }

    private void checkPlugins() {

        if (getProxy().getPluginManager().getPlugin("ajQueue") != null) {
            getLogger().info("§7[§b!§7] Enabling ajQueue API §7[§b!§7]");
            setUseAjQueue(true);
        }

        if (getProxy().getPluginManager().getPlugin("Maintenance") != null) {
            getLogger().info("§7[§b!§7] Enabling Maintenance API §7[§b!§7]");
            setUseMaintenance(true);
        }

    }

    private void checkAlpha() {
        if (version.contains("Alpha")) {
            setAlpha(true);

            getLogger().info(" ");
            getLogger().info("§7You're running an §c§lALPHA VERSION §7of Fallback Server.");
            getLogger().info("§7This version doesn't contain updater.");
            getLogger().info("§7If you find any bugs, please report them on discord.");
            getLogger().info(" ");

        }
    }

    private void loadConfiguration() {
        getLogger().info("§7[§b!§7] Creating configuration files.. §7[§b!§7]");

        configTextFile = new TextFile(this, "config.yml");
        messagesTextFile = new TextFile(this, "messages.yml");
    }

    private void loadCommands() {
        getLogger().info("§7[§b!§7] Preparing commands.. §7[§b!§7]");

        getProxy().getPluginManager().registerCommand(this, new SubCommandManager(this));

        boolean lobbyCommand = BungeeConfig.LOBBY_COMMAND.getBoolean();

        if (lobbyCommand) {
            getProxy().getPluginManager().registerCommand(this, new HubCommand(this));
        }
    }

    private void loadListeners() {
        getLogger().info("§7[§b!§7] Starting all listeners.. §7[§b!§7]");

        getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));

        String mode = BungeeConfig.FALLBACK_MODE.getString();

        switch (mode) {
            case "DEFAULT":
                getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
                getLogger().info("§7[§b!§7] Using default method §7[§b!§7]");
                break;
            case "RECONNECT":
                getProxy().getPluginManager().registerListener(this, new ReconnectListener());
                getLogger().info("§7[§b!§7] Using reconnect method §7[§b!§7]");
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
            getLogger().info("§7[§b!§7] Starting stats service... §7[§b!§7]");
            new BungeeMetrics(this, 11817);
        }
    }

    public void cancelReconnect(UUID uuid) {
        ReconnectHandler task = PlayerCacheManager.getInstance().remove(uuid);
        if (task != null) {
            task.getReconnectTask().cancel();
            task.getTitleTask().cancel();
            task.clear();
        }
    }

    public boolean isHub(ServerInfo server) {
        return BungeeConfig.LOBBIES_LIST.getStringList().contains(server.getName());
    }

    public Configuration getConfig() {
        return configTextFile.getConfig();
    }

    public Configuration getMessagesConfig() {
        return messagesTextFile.getConfig();
    }
}
