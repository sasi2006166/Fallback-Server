package me.candiesjar.fallbackserveraddon;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.tchristofferson.configupdater.ConfigUpdater;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.commands.FallbackAddonCommand;
import me.candiesjar.fallbackserveraddon.listeners.addon.PingListener;
import me.candiesjar.fallbackserveraddon.listeners.standalone.MessageListener;
import me.candiesjar.fallbackserveraddon.listeners.standalone.PlayerListener;
import me.candiesjar.fallbackserveraddon.utils.*;
import me.candiesjar.fallbackserveraddon.utils.tasks.GeneralTask;
import me.candiesjar.fallbackserveraddon.utils.tasks.ThreadTask;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;

@Setter
public final class FallbackServerAddon extends JavaPlugin {

    @Getter
    public static FallbackServerAddon instance;

    @Getter
    private boolean placeholderApi = false;

    @Getter
    private boolean pLib = false;

    @Getter
    private boolean pEvents = false;

    @Getter
    private boolean allPluginsLoaded = true;

    @Getter
    private boolean locked = false;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getConsoleSender().sendMessage("\n" +
                "  ______ _____            _     _             \n" +
                " |  ____/ ____|  /\\      | |   | |            \n" +
                " | |__ | (___   /  \\   __| | __| | ___  _ __  \n" +
                " |  __| \\___ \\ / /\\ \\ / _` |/ _` |/ _ \\| '_ \\ \n" +
                " | |    ____) / ____ \\ (_| | (_| | (_) | | | |\n" +
                " |_|   |_____/_/    \\_\\__,_|\\__,_|\\___/|_| |_|\n");

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Warming up...");

        loadDependencies();
        loadConfig();
        checkVersion();
        schedule();

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§a!§7] Loaded successfully.");
    }

    @Override
    public void onDisable() {
        getCommand("fallbackserveraddon").unregister(Utils.getCommandMap(this));
        Utils.unregisterEvent(new PlayerListener(this));
        Utils.unregisterEvent(new PingListener(this));
        ThreadTask.stopMonitoring();
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Un-Loaded.");
    }

    private void loadDependencies() {
        BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);
        bukkitLibraryManager.addJitPack();

        Relocation scoreboardRelocation = new Relocation("scoreboard", "me{}candiesjar{}libs{}scoreboard");
        Library scoreboard = Library.builder()
                .groupId("fr{}mrmicky")
                .artifactId("FastBoard")
                .version("2.1.3")
                .relocate(scoreboardRelocation)
                .build();

        Relocation schedulerRelocation = new Relocation("scheduler", "me{}candiesjar{}libs{}scheduler");
        Library scheduler = Library.builder()
                .groupId("com{}github{}Anon8281")
                .artifactId("UniversalScheduler")
                .version("0.1.6")
                .relocate(schedulerRelocation)
                .build();

        Relocation updaterRelocation = new Relocation("updater", "me{}candiesjar{}libs{}updater");
        Library configUpdater = Library.builder()
                .groupId("com{}tchristofferson")
                .artifactId("ConfigUpdater")
                .version("2.1-SNAPSHOT")
                .relocate(updaterRelocation)
                .url("https://github.com/frafol/Config-Updater/releases/download/compile/ConfigUpdater-2.1-SNAPSHOT.jar")
                .build();

        bukkitLibraryManager.loadLibrary(scoreboard);
        bukkitLibraryManager.loadLibrary(scheduler);
        bukkitLibraryManager.loadLibrary(configUpdater);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] PlaceholderAPI support enabled.");
            placeholderApi = true;
        }

        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            pLib = true;
        }

        if (getServer().getPluginManager().getPlugin("PacketEvents") != null) {
            pEvents = true;
        }
    }

    private void checkVersion() {
        if (getDescription().getVersion().contains("Beta")) {
            getServer().getConsoleSender().sendMessage("[FallbackServerAddon]  ");
            getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7You're running a §c§lBETA VERSION §7of the plugin.");
            getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7If you find any bugs, please report them on discord.");
            getServer().getConsoleSender().sendMessage("[FallbackServerAddon]  ");
        }
        UpdateUtil.checkForUpdates();
    }

    @SneakyThrows
    private void updateConfig(File versionFile, YamlConfiguration version) {
        File configFile = new File(getDataFolder(), "config.yml");
        if (getDescription().getVersion().equals(version.getString("version"))) {
            return;
        }

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Creating new configurations...");
        ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());

        version.set("version", getDescription().getVersion());
        version.save(versionFile);
        saveDefaultConfig();
    }

    private void schedule() {
        GeneralTask.schedule(this, UniversalScheduler.getScheduler(this));
    }

    public void loadConfig() {
        File versionFile = new File(getDataFolder(), "version.yml");
        YamlConfiguration version = YamlConfiguration.loadConfiguration(versionFile);
        saveDefaultConfig();
        updateConfig(versionFile, version);
    }

    public void executeStart() {
        if (!getConfig().getBoolean("settings.protocollib_support", true)) pLib = false;
        if (!getConfig().getBoolean("settings.packetevents_support", true)) pEvents = false;
        getCommand("fallbackserveraddon").setExecutor(new FallbackAddonCommand(this));
        getCommand("fallbackserveraddon").setTabCompleter(new FallbackAddonCommand(this));
        String mode = getConfig().getString("settings.mode", "NONE");
        UpdateUtil.sendUpdateMessage();

        switch (mode) {
            case "STANDALONE":
                ScoreboardUtil.taskBoards();
                getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
                getServer().getMessenger().registerIncomingPluginChannel(this, "fs:reconnect", new MessageListener(this));
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Detected standalone mode, start completed.");
                break;
            case "ADDON":
                registerPing();
                ThreadTask.monitorMainThread();
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Detected addon mode, start completed.");
                break;
            default:
                sendSetup();
                break;
        }
    }

    public void executeReload(String oldValue) {
        String mode = getConfig().getString("settings.mode", "NONE");
        UpdateUtil.sendUpdateMessage();

        switch (mode) {
            case "STANDALONE":
                if (oldValue.equalsIgnoreCase("ADDON")) {
                    Utils.unregisterEvent(new PingListener(this));
                    PacketEventsUtil.terminate();
                }

                if (oldValue.equalsIgnoreCase("STANDALONE")) {
                    return;
                }

                ScoreboardUtil.reloadBoards();
                getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Detected standalone mode, start completed.");
                break;

            case "ADDON":
                if (oldValue.equalsIgnoreCase("STANDALONE")) {
                    Utils.unregisterEvent(new PlayerListener(this));
                }

                if (oldValue.equalsIgnoreCase("ADDON")) {
                    return;
                }

                registerPing();
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Detected addon mode, start completed.");
                break;

            default:
                if (oldValue.equalsIgnoreCase("STANDALONE")) {
                    Utils.unregisterEvent(new PlayerListener(this));
                }

                if (oldValue.equalsIgnoreCase("ADDON")) {
                    Utils.unregisterEvent(new PingListener(this));
                }

                sendSetup();
                break;
        }
    }

    private void sendSetup() {
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Detected an invalid mode...");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon]");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] §c§lFIRST SETUP GUIDE");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eSelect the mode of your server!");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eYou can choose between §bSTANDALONE §eor §bADDON§e.");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eYou can change the mode in the config.yml file.");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eAfter you have selected the mode, restart the server.");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eCheck the config.yml for more information about this.");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon]");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] §cThe plugin is now in passive mode:");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] §cUse §b/fsa reload §cwhen you finished the setup.");
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon]");
    }

    public void registerPing() {
        if (pEvents) {
            PacketEventsUtil.registerHandler();
            return;
        }
        getServer().getPluginManager().registerEvents(new PingListener(this), this);
    }
}
