package me.candiesjar.fallbackserveraddon;

import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserveraddon.commands.FSACommand;
import me.candiesjar.fallbackserveraddon.listeners.addon.PingListener;
import me.candiesjar.fallbackserveraddon.listeners.standalone.PlayerListener;
import me.candiesjar.fallbackserveraddon.utils.Utils;
import me.candiesjar.fallbackserveraddon.utils.tasks.FoliaTask;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class FallbackServerAddon extends JavaPlugin {

    @Getter
    public static FallbackServerAddon instance;

    @Setter
    @Getter
    private boolean allPluginsLoaded = true;

    @Setter
    @Getter
    private boolean locked = false;

    private BukkitTask task;

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

        getLogger().info("[FallbackServerAddon] §7[§b!§7] Warming up...");

        loadDependencies();
        saveDefaultConfig();
        schedule();

        getLogger().info("[FallbackServerAddon] §7[§a!§7] Loaded successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[FallbackServerAddon] §7[§c!§7] Un-Loaded.");
    }

    public void loadDependencies() {
        BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);

        Relocation foliaRelocation = new Relocation("folialib", "me{}candiesjar{}libs{}folialib");
        Library folia = Library.builder()
                .groupId("com{}tcoded{}folialib")
                .artifactId("folialib")
                .version("0.2.5")
                .url("https://github.com/TechnicallyCoded/FoliaLib/releases/download/v0.2.5/FoliaLib-0.2.5.jar")
                .relocate(foliaRelocation)
                .build();

        bukkitLibraryManager.loadLibrary(folia);
    }

    private void schedule() {

        if (Utils.isFolia()) {
            FoliaTask.schedule();
            return;
        }

        task = getServer().getScheduler().runTaskTimer(this, () -> {

            if (locked) {
                task.cancel();
                return;
            }

            for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
                if (!plugin.isEnabled()) {
                    allPluginsLoaded = false;
                    break;
                }
            }

            if (allPluginsLoaded) {
                locked = true;
                executeStart();
                task.cancel();
            }

        }, 20L, 40L);
    }

    public void executeStart() {
        getCommand("fallbackserveraddon").setExecutor(new FSACommand(this));
        String mode = getConfig().getString("settings.mode");

        switch (mode) {
            case "STANDALONE":
                getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
                getLogger().info("[FallbackServerAddon] §7[§b!§7] Detected standalone mode, start completed.");
                break;
            case "ADDON":
                getServer().getPluginManager().registerEvents(new PingListener(this), this);
                getLogger().info("[FallbackServerAddon] §7[§b!§7] Detected addon mode, start completed.");
                break;
            default:
                getLogger().info("[FallbackServerAddon] §7[§c!§7] Detected an invalid mode...");
                getLogger().info("[FallbackServerAddon]");
                getLogger().info("[FallbackServerAddon] §7[§c!§7] §c§lFIRST SETUP GUIDE");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eSelect the mode of your server!");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eYou can choose between §bSTANDALONE §eor §bADDON§e.");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eYou can change the mode in the config.yml file.");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eAfter you have selected the mode, restart the server.");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eCheck the config.yml for more information about this.");
                getLogger().info("[FallbackServerAddon]");
                getLogger().info("[FallbackServerAddon] §7[§c!§7] §cThe plugin is now in passive mode:");
                getLogger().info("[FallbackServerAddon] §7[§c!§7] §cUse §b/fsa reload §cwhen you finished the setup.");
                getLogger().info("[FallbackServerAddon]");
                break;
        }
    }

    public void executeReload(String oldValue) {
        String mode = getConfig().getString("settings.mode");

        switch (mode) {
            case "STANDALONE":
                if (oldValue.equalsIgnoreCase("ADDON")) {
                    Utils.unregisterEvent(new PingListener(this));
                }

                if (oldValue.equalsIgnoreCase("STANDALONE")) {
                    return;
                }

                getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
                getLogger().info("[FallbackServerAddon] §7[§b!§7] Detected standalone mode, start completed.");
                break;
            case "ADDON":
                if (oldValue.equalsIgnoreCase("STANDALONE")) {
                    Utils.unregisterEvent(new PlayerListener(this));
                }

                if (oldValue.equalsIgnoreCase("ADDON")) {
                    return;
                }

                getServer().getPluginManager().registerEvents(new PingListener(this), this);
                getLogger().info("[FallbackServerAddon] §7[§b!§7] Detected addon mode, start completed.");
                break;

            default:
                if (oldValue.equalsIgnoreCase("STANDALONE")) {
                    Utils.unregisterEvent(new PlayerListener(this));
                }

                if (oldValue.equalsIgnoreCase("ADDON")) {
                    Utils.unregisterEvent(new PingListener(this));
                }

                getLogger().info("[FallbackServerAddon] §7[§c!§7] Detected an invalid mode...");
                getLogger().info("[FallbackServerAddon]");
                getLogger().info("[FallbackServerAddon] §7[§c!§7] §c§lFIRST SETUP GUIDE");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eSelect the mode of your server!");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eYou can choose between §bSTANDALONE §eor §bADDON§e.");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eYou can change the mode in the config.yml file.");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eAfter you have selected the mode, restart the server.");
                getLogger().info("[FallbackServerAddon] §7[§e!§7] §eCheck the config.yml for more information about this.");
                getLogger().info("[FallbackServerAddon]");
                getLogger().info("[FallbackServerAddon] §7[§c!§7] §cThe plugin is now in passive mode:");
                getLogger().info("[FallbackServerAddon] §7[§c!§7] §cUse §b/fsa reload §cwhen you finished the setup.");
                getLogger().info("[FallbackServerAddon]");
                break;
        }
    }
}
