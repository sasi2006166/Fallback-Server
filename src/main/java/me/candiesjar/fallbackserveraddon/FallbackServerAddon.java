package me.candiesjar.fallbackserveraddon;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.tchristofferson.configupdater.ConfigUpdater;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.candiesjar.fallbackserveraddon.commands.FSACommand;
import me.candiesjar.fallbackserveraddon.listeners.addon.PingListener;
import me.candiesjar.fallbackserveraddon.listeners.standalone.PlayerListener;
import me.candiesjar.fallbackserveraddon.utils.Utils;
import me.candiesjar.fallbackserveraddon.utils.tasks.GeneralTask;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@Setter
public final class FallbackServerAddon extends JavaPlugin {

    @Getter
    public static FallbackServerAddon instance;

    @Getter
    private boolean allPluginsLoaded = true;

    @Getter
    private boolean locked = false;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("\n" +
                "  ______ _____            _     _             \n" +
                " |  ____/ ____|  /\\      | |   | |            \n" +
                " | |__ | (___   /  \\   __| | __| | ___  _ __  \n" +
                " |  __| \\___ \\ / /\\ \\ / _` |/ _` |/ _ \\| '_ \\ \n" +
                " | |    ____) / ____ \\ (_| | (_| | (_) | | | |\n" +
                " |_|   |_____/_/    \\_\\__,_|\\__,_|\\___/|_| |_|\n");

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Warming up...");

        loadDependencies();
        loadConfig();
        schedule();

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§a!§7] Loaded successfully.");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Un-Loaded.");
    }

    private void loadDependencies() {
        BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);
        bukkitLibraryManager.addJitPack();

        Relocation schedulerRelocation = new Relocation("scheduler", "me{}candiesjar{}libs{}scheduler");
        Library scheduler = Library.builder()
                .groupId("com{}github{}Anon8281")
                .artifactId("UniversalScheduler")
                .version("0.1.6")
                .relocate(schedulerRelocation)
                .build();

        Relocation actionbarRelocation = new Relocation("actionbar", "me{}candiesjar{}libs{}actionbar");
        Library actionbar = Library.builder()
                .groupId("com{}connorlinfoot{}actionbarapi")
                .artifactId("ActionBarAPI")
                .version("2.0.0")
                .relocate(actionbarRelocation)
                .url("https://github.com/sasi2006166/ActionBarAPI/raw/master/ActionBarAPI-2.0.0.jar")
                .build();

        Relocation updaterRelocation = new Relocation("updater", "me{}candiesjar{}libs{}updater");
        Library configUpdater = Library.builder()
                .groupId("com{}tchristofferson")
                .artifactId("ConfigUpdater")
                .version("2.1-SNAPSHOT")
                .relocate(updaterRelocation)
                .url("https://github.com/frafol/Config-Updater/releases/download/compile/ConfigUpdater-2.1-SNAPSHOT.jar")
                .build();

        bukkitLibraryManager.loadLibrary(scheduler);
        bukkitLibraryManager.loadLibrary(actionbar);
        bukkitLibraryManager.loadLibrary(configUpdater);
    }

    @SneakyThrows
    private void updateConfig(File versionFile, YamlConfiguration version) {
        File configFile = new File(getDataFolder(), "config.yml");
        if (getDescription().getVersion().equals(version.getString("version"))) {
            return;
        }

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Creating new configurations...");
        try {
            ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());
        } catch (IOException ignored) {
            getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Unable to update configuration files, please reset it.");
            getPluginLoader().disablePlugin(this);
            return;
        }

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
        getCommand("fallbackserveraddon").setExecutor(new FSACommand(this));
        String mode = getConfig().getString("settings.mode", "NONE");

        switch (mode) {
            case "STANDALONE":
                getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Detected standalone mode, start completed.");
                break;
            case "ADDON":
                getServer().getPluginManager().registerEvents(new PingListener(this), this);
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Detected addon mode, start completed.");
                break;
            default:
                sendSetup();
                break;
        }
    }

    public void executeReload(String oldValue) {
        String mode = getConfig().getString("settings.mode", "NONE");

        switch (mode) {
            case "STANDALONE":

                if (oldValue.equalsIgnoreCase("ADDON")) {
                    Utils.unregisterEvent(new PingListener(this));
                }

                if (oldValue.equalsIgnoreCase("STANDALONE")) {
                    return;
                }

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

                getServer().getPluginManager().registerEvents(new PingListener(this), this);
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
}
