package me.candiesjar.fallbackserver;

import lombok.Getter;
import me.candiesjar.fallbackserver.cache.PlayerCache;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.commands.base.SubCommandManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.listeners.*;
import me.candiesjar.fallbackserver.metrics.Metrics;
import me.candiesjar.fallbackserver.objects.TextFile;
import me.candiesjar.fallbackserver.utils.UpdateUtil;
import me.candiesjar.fallbackserver.utils.tasks.LobbyCheckerTask;
import me.candiesjar.fallbackserver.utils.tasks.ReconnectTask;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private boolean isAlpha = false;

    @Getter
    private boolean useAjQueue = false;

    @Getter
    private boolean useMaintenance = false;

    public void onEnable() {
        instance = this;

        version = getDescription().getVersion();

        getLogger().info("§b __________      ________________              ______      ________                               ");
        getLogger().info("§b ___  ____/_____ ___  /__  /__  /_______ _________  /__    __  ___/______________   ______________");
        getLogger().info("§b __  /_   _  __ `/_  /__  /__  __ \\  __ `/  ___/_  //_/    _____ \\_  _ \\_  ___/_ | / /  _ \\_  ___/");
        getLogger().info("§b _  __/   / /_/ /_  / _  / _  /_/ / /_/ // /__ _  ,<       ____/ //  __/  /   __ |/ //  __/  /    ");
        getLogger().info("§b /_/      \\__,_/ /_/  /_/  /_.___/\\__,_/ \\___/ /_/|_|      /____/ \\___//_/    _____/ \\___//_/     ");

        loadConfiguration();

        checkPlugins();

        loadListeners();

        loadCommands();

        startMetrics();

        getLogger().info("§7[§b!§7] Plugin loaded successfully §7[§b!§7]");
        getProxy().getScheduler().schedule(this, new LobbyCheckerTask(), 0, BungeeConfig.PING_DELAY.getInt(), TimeUnit.SECONDS);

        checkAlpha();

        UpdateUtil.checkUpdates();

    }

    public void onDisable() {
        instance = null;

        getLogger().info("§7[§c!§7] §bFallbackServer §7is disabling.. §7[§c!§7]");
    }

    private void checkPlugins() {

        if (getProxy().getPluginManager().getPlugin("ajQueue") != null) {
            getLogger().info("§7[§b!§7] Enabling ajQueue API §7[§b!§7]");
            useAjQueue = true;
        }

        if (getProxy().getPluginManager().getPlugin("Maintenance") != null) {
            getLogger().info("§7[§b!§7] Enabling Maintenance API §7[§b!§7]");
            useMaintenance = true;
        }

    }

    private void checkAlpha() {
        if (version.contains("Alpha")) {
            isAlpha = true;

            getLogger().info(" ");
            getLogger().info("§7You're running an §c§lALPHA VERSION §7of Fallback Server.");
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
            getProxy().getPluginManager().registerCommand(this, new HubCommand());
        }
    }

    private void loadListeners() {
        getLogger().info("§7[§b!§7] Starting all listeners.. §7[§b!§7]");

        getProxy().getPluginManager().registerListener(this, new ServerDisconnectListener());

        String mode = BungeeConfig.FALLBACK_MODE.getString();

        switch (mode) {
            case "DEFAULT":
                getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
                getLogger().info("§7[§b!§7] Using default method §7[§b!§7]");
                break;
            case "RECONNECT":
                getProxy().getPluginManager().registerListener(this, new ReconnectListener(this));
                getLogger().info("§7[§b!§7] Using reconnect method §7[§b!§7]");
                break;
            default:
                getLogger().severe("Configuration error under fallback_mode: " + BungeeConfig.FALLBACK_MODE.getString());
                getLogger().severe("Trying to enable default mode..");
                getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
                break;
        }

        boolean disabledServers = BungeeConfig.USE_COMMAND_BLOCKER.getBoolean();

        if (disabledServers) {
            getProxy().getPluginManager().registerListener(this, new ChatListener());
        }

        boolean checkUpdates = BungeeConfig.UPDATER.getBoolean();

        if (checkUpdates) {
            getProxy().getPluginManager().registerListener(this, new PlayerListener());
        }

    }

    private void startMetrics() {

        getLogger().info("§7[§b!§7] Starting stats service... §7[§b!§7]");

        boolean telemetry = BungeeConfig.TELEMETRY.getBoolean();

        if (telemetry)
            new Metrics(this, 11817);
    }

    public void cancelReconnect(UUID uuid) {
        ReconnectTask task = PlayerCache.getReconnectMap().remove(uuid);
        task.clear();
    }

    public String getPrefix() {
        return getMessagesConfig().getString(BungeeMessages.PREFIX.getPath());
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
