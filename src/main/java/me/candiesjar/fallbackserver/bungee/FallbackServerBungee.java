package me.candiesjar.fallbackserver.bungee;

import lombok.Getter;
import me.candiesjar.fallbackserver.bungee.commands.base.HubCommand;
import me.candiesjar.fallbackserver.bungee.commands.base.SubCommandManager;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.listeners.ChatListener;
import me.candiesjar.fallbackserver.bungee.listeners.FallbackListener;
import me.candiesjar.fallbackserver.bungee.listeners.PlayerListener;
import me.candiesjar.fallbackserver.bungee.metrics.Metrics;
import me.candiesjar.fallbackserver.bungee.objects.TextFile;
import me.candiesjar.fallbackserver.bungee.utils.UpdateUtil;
import me.candiesjar.fallbackserver.bungee.utils.tasks.LobbyCheckerTask;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class FallbackServerBungee extends Plugin {
    private final List<ServerInfo> availableServers = new ArrayList<>();

    private List<String> serverList;

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

        serverList = BungeeConfig.LOBBIES.getStringList();

        // Listeners
        getLogger().info("§7[§b!§7] Starting all listeners... §7[§b!§7]");
        loadListeners();

        // Commands
        getLogger().info("§7[§b!§7] Preparing commands... §7[§b!§7]");
        loadCommands();

        // Stats
        getLogger().info("§7[§b!§7] Starting stats service... §7[§b!§7]");
        startMetrics();

        // Setup
        getLogger().info("§7[§b!§7] Final steps... §7[§b!§7]");

        getProxy().getScheduler().schedule(this, new LobbyCheckerTask(), 0, BungeeConfig.TASK_PERIOD.getInt(), TimeUnit.SECONDS);

        getLogger().info("§7[§b!§7] Plugin loaded successfully §7[§b!§7]");
        UpdateUtil.checkUpdates();

    }

    public void onDisable() {
        instance = null;

        availableServers.clear();
        serverList.clear();

        configTextFile = null;
        messagesTextFile = null;

        getLogger().info("§7[§c!§7] §cDisabling plugin... §7[§c!§7]");
    }

    public String getPrefix() {
        return getMessagesConfig().getString(BungeeMessages.PREFIX.getPath());
    }

    private void loadCommands() {
        getProxy().getPluginManager().registerCommand(this, new SubCommandManager());

        if (BungeeConfig.USE_HUB_COMMAND.getBoolean()) {
            getProxy().getPluginManager().registerCommand(this, new HubCommand());
        }
    }

    private void loadListeners() {
        getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
        if (BungeeConfig.DISABLED_SERVERS.getBoolean())
            getProxy().getPluginManager().registerListener(this, new ChatListener());
        if (BungeeConfig.UPDATE_CHECKER.getBoolean())
            getProxy().getPluginManager().registerListener(this, new PlayerListener());
    }

    private void startMetrics() {
        if (BungeeConfig.USE_STATS.getBoolean())
            new Metrics(this, 11817);
    }

    public boolean isHub(ServerInfo server) {
        return BungeeConfig.LOBBIES.getStringList().contains(server.getName());
    }

    public Configuration getConfig() {
        return configTextFile.getConfig();
    }

    public Configuration getMessagesConfig() {
        return messagesTextFile.getConfig();
    }

    public List<String> getServerList() {
        return serverList;
    }
}
