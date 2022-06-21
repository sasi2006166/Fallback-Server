package me.candiesjar.fallbackserver;

import lombok.Getter;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.commands.base.SubCommandManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.listeners.ChatListener;
import me.candiesjar.fallbackserver.listeners.FallbackListener;
import me.candiesjar.fallbackserver.listeners.PlayerListener;
import me.candiesjar.fallbackserver.listeners.ReconnectListener;
import me.candiesjar.fallbackserver.metrics.Metrics;
import me.candiesjar.fallbackserver.objects.TextFile;
import me.candiesjar.fallbackserver.utils.UpdateUtil;
import me.candiesjar.fallbackserver.utils.tasks.LobbyCheckerTask;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.concurrent.TimeUnit;

public final class FallbackServerBungee extends Plugin {

    private static FallbackServerBungee instance;

    @Getter
    private TextFile configTextFile;

    @Getter
    private TextFile messagesTextFile;

    @Getter
    private String version;

    public static FallbackServerBungee getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;

        version = getDescription().getVersion();

        getLogger().info("§b __________      ________________              ______      ________                               ");
        getLogger().info("§b ___  ____/_____ ___  /__  /__  /_______ _________  /__    __  ___/______________   ______________");
        getLogger().info("§b __  /_   _  __ `/_  /__  /__  __ \\  __ `/  ___/_  //_/    _____ \\_  _ \\_  ___/_ | / /  _ \\_  ___/");
        getLogger().info("§b _  __/   / /_/ /_  / _  / _  /_/ / /_/ // /__ _  ,<       ____/ //  __/  /   __ |/ //  __/  /    ");
        getLogger().info("§b /_/      \\__,_/ /_/  /_/  /_.___/\\__,_/ \\___/ /_/|_|      /____/ \\___//_/    _____/ \\___//_/     ");

        // Instances
        getLogger().info("§7[§b!§7] Creating configuration files... §7[§b!§7]");

        configTextFile = new TextFile(this, "config.yml");
        messagesTextFile = new TextFile(this, "messages.yml");

        // Listeners
        getLogger().info("§7[§b!§7] Starting all listeners... §7[§b!§7]");
        loadListeners();

        // Commands
        getLogger().info("§7[§b!§7] Preparing commands... §7[§b!§7]");
        loadCommands();

        // Stats
        getLogger().info("§7[§b!§7] Starting stats service... §7[§b!§7]");
        startMetrics();

        getLogger().info("§7[§b!§7] Plugin loaded successfully §7[§b!§7]");
        getProxy().getScheduler().schedule(this, new LobbyCheckerTask(), 0, BungeeConfig.TASK_PERIOD.getInt(), TimeUnit.SECONDS);
        UpdateUtil.checkUpdates();

    }

    public void onDisable() {
        instance = null;

        configTextFile = null;
        messagesTextFile = null;

        version = null;

        getLogger().info("§7[§c!§7] §bFallbackServer §7is disabling... §7[§c!§7]");
    }

    public String getPrefix() {
        return getMessagesConfig().getString(BungeeMessages.PREFIX.getPath());
    }

    private void loadCommands() {
        getProxy().getPluginManager().registerCommand(this, new SubCommandManager());

        if (BungeeConfig.LOBBY_COMMAND.getBoolean()) {
            getProxy().getPluginManager().registerCommand(this, new HubCommand());
        }
    }

    private void loadListeners() {

        switch (BungeeConfig.FALLBACK_MODE.getString()) {
            case "DEFAULT":
                getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
                getLogger().info("§7[§b!§7] Enabled default kicking method §7[§b!§7]");
                break;
            case "RECONNECT":
                getProxy().getPluginManager().registerListener(this, new ReconnectListener());
                getLogger().info("§7[§b!§7] Enabled reconnect kicking method §7[§b!§7]");
                break;
            default:
                getLogger().severe("Configuration error under fallback_mode: " + BungeeConfig.FALLBACK_MODE.getString());
                getLogger().severe("Enabling default configuration");
                getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
                break;
        }

        if (BungeeConfig.DISABLED_SERVERS.getBoolean())
            getProxy().getPluginManager().registerListener(this, new ChatListener());
        if (BungeeConfig.UPDATE_CHECKER.getBoolean())
            getProxy().getPluginManager().registerListener(this, new PlayerListener());
    }

    private void startMetrics() {
        if (BungeeConfig.TELEMETRY.getBoolean())
            new Metrics(this, 11817);
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
