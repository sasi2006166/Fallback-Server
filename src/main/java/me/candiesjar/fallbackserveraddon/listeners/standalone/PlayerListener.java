package me.candiesjar.fallbackserveraddon.listeners.standalone;

import io.papermc.lib.PaperLib;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.ActionBarUtil;
import me.candiesjar.fallbackserveraddon.utils.BossBarUtil;
import me.candiesjar.fallbackserveraddon.utils.ChatUtil;
import me.candiesjar.fallbackserveraddon.utils.ScoreboardUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

public class PlayerListener implements Listener {

    private final FallbackServerAddon plugin;

    public PlayerListener(FallbackServerAddon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("settings.standalone.teleport_worldspawn", true)) {
            teleport(player, player.getWorld().getSpawnLocation());
        }

        if (!plugin.getConfig().getString("settings.standalone.join_gamemode", "NONE").equals("NONE")) {
            switch (plugin.getConfig().getString("settings.standalone.join_gamemode", "NONE")) {
                case "SURVIVAL":
                    player.setGameMode(GameMode.SURVIVAL);
                    break;
                case "CREATIVE":
                    player.setGameMode(GameMode.CREATIVE);
                    break;
                case "ADVENTURE":
                    player.setGameMode(GameMode.ADVENTURE);
                    break;
                case "SPECTATOR":
                    player.setGameMode(GameMode.SPECTATOR);
                    break;
                default:
                    break;
            }
        }

        if (plugin.getConfig().getBoolean("settings.standalone.actionbar.enabled", false)) {
            ActionBarUtil.startActionBar(player, plugin.getConfig().getString("settings.standalone.actionbar.message"));
        }

        if (plugin.getConfig().getBoolean("settings.standalone.bossbar.enabled", false)) {
            BossBarUtil.sendBossBar(player,
                    ChatUtil.color(player, plugin.getConfig().getString("settings.standalone.bossbar.message")),
                    plugin.getConfig().getString("settings.standalone.bossbar.color"),
                    plugin.getConfig().getString("settings.standalone.bossbar.style"),
                    plugin.getConfig().getDouble("settings.standalone.bossbar.progress"));
        }

        if (plugin.getConfig().getBoolean("settings.standalone.scoreboard.enabled", false)) {
            ScoreboardUtil.createScoreboard(player);
        }

        if (!plugin.getConfig().getString("settings.standalone.join_sound").equals("NONE")) {
            Sound sound = Sound.valueOf(plugin.getConfig().getString("settings.standalone.join_sound"));
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        }

        if (plugin.getConfig().getString("settings.standalone.join_message").equals("none")) {
            event.setJoinMessage(null);
            return;
        }

        event.setJoinMessage(ChatUtil.color(player, plugin.getConfig().getString("settings.standalone.join_message", null))
                .replace("%player_name%", player.getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        BossBarUtil.removeBossBar(player);
        ActionBarUtil.stopActionBar(player);
        ScoreboardUtil.deleteScoreboard(player);

        if (plugin.getConfig().getString("settings.standalone.quit_message").equals("none")) {
            event.setQuitMessage(null);
            return;
        }

        event.setQuitMessage(ChatUtil.color(player, plugin.getConfig().getString("settings.standalone.quit_message", null))
                .replace("%player_name%", player.getName()));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.interact", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.pvp", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.item_drop", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.item_pickup", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.chat", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.command", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.move", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.damage", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.block_break", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.block_place", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.weather_cycle", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.hunger", true)) {
            event.setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (plugin.getConfig().getBoolean("settings.standalone.event_blocker.entity_spawn", true)) {
            event.setCancelled(true);
        }
    }

    private void teleport(Player player, Location location) {
        if (PaperLib.isPaper() && PaperLib.getMinecraftVersion() >= 575) {
            PaperLib.teleportAsync(player, location);
            return;
        }
        player.teleport(player.getWorld().getSpawnLocation());
    }
}
