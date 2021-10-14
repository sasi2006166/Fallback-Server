package me.candiesjar.fallbackserver.bungee;

import com.google.common.io.ByteStreams;
import me.candiesjar.fallbackserver.bungee.commands.FallbackCommand;
import me.candiesjar.fallbackserver.bungee.commands.HubCommand;
import me.candiesjar.fallbackserver.bungee.enums.ConfigFields;
import me.candiesjar.fallbackserver.bungee.enums.MessagesFields;
import me.candiesjar.fallbackserver.bungee.enums.SpreadMode;
import me.candiesjar.fallbackserver.bungee.listeners.ChatListener;
import me.candiesjar.fallbackserver.bungee.listeners.FallbackListener;
import me.candiesjar.fallbackserver.bungee.metrics.Metrics;
import me.candiesjar.fallbackserver.bungee.utils.RedirectServerWrapper;
import me.candiesjar.fallbackserver.bungee.utils.ServerGroup;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class FallbackServerBungee extends Plugin {

    private static FallbackServerBungee instance;
    private Configuration config;
    private Configuration messagesConfig;
    private Map<String, RedirectServerWrapper> servers;
    private List<ServerGroup> serverGroups;
    private ScheduledTask checker;

    public static FallbackServerBungee getInstance() {
        return instance;
    }

    public void onEnable() {

        // Instances
        getLogger().info("§7[§b!§7] Loading configuration §7[§b!§7]");
        instance = this;
        servers = new HashMap<>();
        serverGroups = new ArrayList<>();
        loadConfig();
        loadMessages();

        // Listeners
        getLogger().info("§7[§b!§7] Loading listeners §7[§b!§7]");
        loadListeners();

        // Commands
        getLogger().info("§7[§b!§7] Loading commands §7[§b!§7]");
        loadCommands();

        // Stats
        getLogger().info("§7[§b!§7] Loading stats §7[§b!§7]");
        startMetrics();

        // Setup
        getLogger().info("§7[§b!§7] Loading plugin §7[§b!§7]");
        setup();

        getLogger().info("§b __________      ________________              ______      ________                               ");
        getLogger().info("§b ___  ____/_____ ___  /__  /__  /_______ _________  /__    __  ___/______________   ______________");
        getLogger().info("§b __  /_   _  __ `/_  /__  /__  __ \\  __ `/  ___/_  //_/    _____ \\_  _ \\_  ___/_ | / /  _ \\_  ___/");
        getLogger().info("§b _  __/   / /_/ /_  / _  / _  /_/ / /_/ // /__ _  ,<       ____/ //  __/  /   __ |/ //  __/  /    ");
        getLogger().info("§b /_/      \\__,_/ /_/  /_/  /_.___/\\__,_/ \\___/ /_/|_|      /____/ \\___//_/    _____/ \\___//_/     ");
        getLogger().info("§7Loaded successfully, for any doubts see the config.yml file!");

        startCheck();
    }

    public void onDisable() {
        getLogger().info("§7[§c!§7] §cCleaning list(s) §7[§c!§7]");
        instance = null;
        servers.clear();
        serverGroups.clear();
        getLogger().info("§7[§c!§7] §cDisabling FallbackServer §7[§c!§7]");
    }

    private void loadCommands() {
        getProxy().getPluginManager().registerCommand(this, new FallbackCommand());
        if (ConfigFields.USE_HUB_COMMAND.getBoolean()) {
            getProxy().getPluginManager().registerCommand(this, new HubCommand());
        }
    }

    private File loadConfigurations(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);
        try {
            if (!resourceFile.exists()) {
                resourceFile.createNewFile();
                try (InputStream in = plugin.getResourceAsStream(resource);
                     OutputStream out = new FileOutputStream(resourceFile)) {
                    ByteStreams.copy(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resourceFile;
    }

    private void loadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                    loadConfigurations(instance, "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        try {
            messagesConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(
                    loadConfigurations(instance, "messages.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCheck() {
        if (ConfigFields.UPDATE_CHECKER.getBoolean())
            if (checkUpdates())
                getLogger().info(MessagesFields.NEW_UPDATE.getFormattedString()
                        .replace("%prefix%", MessagesFields.PREFIX.getFormattedString()));
    }

    private void loadListeners() {
        getProxy().getPluginManager().registerListener(this, new FallbackListener(this));
        if (ConfigFields.DISABLE_SERVERS.getBoolean())
            getProxy().getPluginManager().registerListener(this, new ChatListener());
    }

    private void startMetrics() {
        if (ConfigFields.STATS.getBoolean())
            new Metrics(this, 11817);
    }

    private void setup() {
        if (servers == null)
            servers = new HashMap<>();
        else
            servers.clear();

        for (String key : config.getSection("groups").getKeys()) {

            SpreadMode spreadMode = null;
            int minimalProgressive = 0;

            if (getConfig().get("groups." + key + ".spread-mode") != null) {
                spreadMode = SpreadMode.valueOf(getConfig().getString("groups." + key + ".spread-mode"));
            }
            if (getConfig().get("groups." + key + ".progressive-minimal") != null) {
                minimalProgressive = getConfig().getInt("groups." + key + ".progressive-minimal");
            }

            ServerGroup serverGroup = new ServerGroup(
                    this,
                    key,
                    config.getBoolean("groups." + key + ".bottom-kick"),
                    config.getBoolean("groups." + key + ".spread"),
                    config.getString("groups." + key + ".parent-group"),
                    spreadMode,
                    minimalProgressive,
                    config.getString("groups." + key + ".permission")
            );

            for (String serverNames : config.getStringList("groups." + key + ".servers")) {
                ServerInfo info = getProxy().getServerInfo(serverNames.replace("%", "."));
                if (info == null) {
                    getLogger().warning("Failed to find server: " + key + "(PATH: groups." + key + "servers > " + serverNames);
                    continue;
                }

                RedirectServerWrapper redirectServerWrapper = new RedirectServerWrapper(info, serverGroup, true);
                if (!servers.containsKey(serverNames))
                    servers.put(serverNames, redirectServerWrapper);

                serverGroup.addServer(redirectServerWrapper);
            }

            if (!config.getString("groups." + key + ".servers-regex").equalsIgnoreCase("") && !config.getString("group." + key + ".servers-regex").equalsIgnoreCase("none")) {
                String regexString = config.getString("groups." + key + ".servers-regex");

                for (Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
                    if (!entry.getKey().matches(regexString)) {
                        continue;
                    }

                    String servername = entry.getKey();
                    if (servers.containsKey(servername)) {
                        getLogger().info(String.format("Server %s matched the regex of %s but is not added due to already being assigned a server group.", servername, serverGroup.getName()));
                        continue;
                    }

                    RedirectServerWrapper redirectServerWrapper = new RedirectServerWrapper(entry.getValue(), serverGroup, true);
                    servers.put(servername, redirectServerWrapper);

                    serverGroup.addServer(redirectServerWrapper);
                }
            }

            for (String servername : config.getStringList("groups." + key + ".connected")) {
                ServerInfo info = getProxy().getServerInfo(servername.replace("%", "."));
                if (info == null) {
                    getLogger().warning("Failed to find the server: " + key + "(PATH: groups." + key + "servers > " + servername);
                    continue;
                }

                RedirectServerWrapper redirectServerWrapper = new RedirectServerWrapper(info, serverGroup, false);
                servers.put(servername, redirectServerWrapper);

                if (!servers.containsKey(servername))
                    servers.put(servername, redirectServerWrapper);

                serverGroup.addConnectedServer(redirectServerWrapper);
            }

            if (!config.getString("groups." + key + ".connected-regex").equalsIgnoreCase("none")) {
                String regexString = config.getString("groups." + key + ".connected-regex");

                for (Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
                    if (!entry.getKey().matches(regexString)) {
                        continue;
                    }

                    String servername = entry.getKey();
                    if (servers.containsKey(servername)) {
                        continue;
                    }

                    RedirectServerWrapper redirectServerWrapper = new RedirectServerWrapper(entry.getValue(), serverGroup, true);
                    servers.put(servername, redirectServerWrapper);

                    serverGroup.addConnectedServer(redirectServerWrapper);
                }
            }
            serverGroups.add(serverGroup);
        }
        if (checker != null) {
            checker.cancel();
        }
        checker = getProxy().getScheduler().schedule(this, this::updateServers, 0, 30, TimeUnit.SECONDS);
    }

    public void updateServers() {
        for (RedirectServerWrapper server : servers.values()) {
            ServerInfo info = server.getServerInfo();
            if (info == null) {
                continue;
            }
            info.ping((serverPing, throwable) -> getProxy().getScheduler().schedule(this, () -> {
                if (throwable == null) {
                    server.setOnline(true);
                    server.setOnlinePlayersCount(info.getPlayers().size());
                } else {
                    server.setOnline(false);
                    server.setOnlinePlayersCount(0);
                }
            }, 1, TimeUnit.MILLISECONDS));
        }
    }

    public boolean checkUpdates() {
        try {
            URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();
            String response = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            return !getDescription().getVersion().equals(response);
        } catch (IOException e) {
            getLogger().severe("Cannot check for updates, maybe something is blocking it?");
        }
        return false;
    }

    public Configuration getConfig() {
        return config;
    }

    public boolean isHub(ServerInfo server) {
        return ConfigFields.LOBBY_SERVER.getStringList().contains(server.getName());
    }

    public ServerGroup getServerGroup(String name) {
        for (ServerGroup serverGroup : serverGroups) {
            if (serverGroup.getName().equalsIgnoreCase(name)) {
                return serverGroup;
            }
        }
        return null;
    }

    public RedirectServerWrapper getServer(String name) {
        if (servers.containsKey(name)) {
            return servers.get(name);
        }
        return null;
    }

    public ServerGroup getUnknownServerGroup() {
        return getServerGroup(getConfig().getString("unknown-group"));
    }

    public void reloadConfig() {
        loadConfig();
        loadMessages();
    }

    public Configuration getMessagesConfig() {
        return messagesConfig;
    }
}
