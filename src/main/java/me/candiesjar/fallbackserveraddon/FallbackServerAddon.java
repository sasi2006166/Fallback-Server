package me.candiesjar.fallbackserveraddon;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserveraddon.listeners.addon.PingListener;
import me.candiesjar.fallbackserveraddon.listeners.standalone.PlayerListener;
import me.candiesjar.fallbackserveraddon.utils.Utils;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class FallbackServerAddon extends JavaPlugin {

    @Getter
    public static FallbackServerAddon instance;

    @Setter
    private boolean allPluginsLoaded = true;

    @Getter
    private boolean locked = false;

    @Getter
    private FoliaLib foliaLib = new FoliaLib(this);

    private BukkitTask task;
    private WrappedTask foliaTask;

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
        saveDefaultConfig();
        schedule();

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§a!§7] Loaded successfully.");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Un-Loaded.");
    }

    public void loadDependencies() {
        BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);

        final Relocation foliaRelocation = new Relocation("folialib", "it{}frafol{}libs{}folialib");
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
            scheduleFolia();
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

    private void scheduleFolia() {

        foliaTask = foliaLib.getImpl().runTimer(() -> {

            if (locked) {
                foliaTask.cancel();
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
                foliaTask.cancel();
            }

        }, 20L, 40L);
    }

    private void executeStart() {
        switch (Objects.requireNonNull(getConfig().getString("settings.mode"))) {

            case "STANDALONE":
                getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Detected standalone mode, start completed.");
                break;

            case "ADDON":
                getServer().getPluginManager().registerEvents(new PingListener(this), this);
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Detected addon mode, start completed.");
                break;

            default:
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Detected an invalid mode...");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon]");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] §c§lFIRST SETUP GUIDE");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eSelect the mode of your server!");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eYou can choose between §bSTANDALONE §eor §bADDON§e.");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eYou can change the mode in the config.yml file.");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eAfter you have selected the mode, restart the server.");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §eCheck the config.yml for more information about this.");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon]");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] §cThe server will shut down now.");
                getServer().getConsoleSender().sendMessage("[FallbackServerAddon]");
                getServer().shutdown();
                break;

        }
    }
}
