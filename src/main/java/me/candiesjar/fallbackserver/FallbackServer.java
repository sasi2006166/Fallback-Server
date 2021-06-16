package me.candiesjar.fallbackserver;

import com.google.common.io.ByteStreams;
import me.candiesjar.fallbackserver.commands.FallbackCommand;
import me.candiesjar.fallbackserver.commands.HubCommand;
import me.candiesjar.fallbackserver.listeners.FallbackListener;
import me.candiesjar.fallbackserver.utils.Fields;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

public final class FallbackServer extends Plugin {

    public Configuration config;
    public static FallbackServer instance;
    private File configFile;

    public void onEnable() {

        // Instances
        getLogger().info("§7Creating config file...");
        instance = this;
        createConfig();

        // Listeners
        getLogger().info("§7Registering listeners...");
        getProxy().getPluginManager().registerListener(this, new FallbackListener(this));

        // Commands
        getLogger().info("§7Registering commands...");
        getProxy().getPluginManager().registerCommand(this, new FallbackCommand());
        getProxy().getPluginManager().registerCommand(this, new HubCommand());

        getLogger().info("§b  ______    _ _ _                _       _____                          ");
        getLogger().info("§b |  ____|  | | | |              | |     / ____|                         ");
        getLogger().info("§b | |__ __ _| | | |__   __ _  ___| | __ | (___   ___ _ ____   _____ _ __ ");
        getLogger().info("§b |  __/ _` | | | '_ \\ / _` |/ __| |/ /  \\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|");
        getLogger().info("§b | | | (_| | | | |_) | (_| | (__|   <   ____) |  __/ |   \\ V /  __/ |");
        getLogger().info("§b |_|  \\__,_|_|_|_.__/ \\__,_|\\___|_|\\_\\ |_____/ \\___|_|    \\_/ \\___|_|");
        getLogger().info("§7Loaded §bsuccessfully§7, for any doubts see the config.yml file!");

        if (ProxyServer.getInstance().getServerInfo(Fields.LOBBYSERVER.getString()) == null) {
            getLogger().severe("You didn't setup the lobby! Check config.yml");
        }
    }

    public void onDisable() {
        instance = null;
        getLogger().info("§cDisabling Fallback Server!");
    }

    public void createConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(getConfig())) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                getLogger().severe("Cannot create configuration file.");
            }
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getConfig());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getConfig() {
        return configFile;
    }

    public Configuration getConfigFile() {
        return config;
    }

    public void reloadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getConfig());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
