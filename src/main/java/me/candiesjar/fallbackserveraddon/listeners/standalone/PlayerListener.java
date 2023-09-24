package me.candiesjar.fallbackserveraddon.listeners.standalone;

import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.ActionBarUtil;
import me.candiesjar.fallbackserveraddon.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

public class PlayerListener implements Listener {

    private final FallbackServerAddon plugin;

    private boolean actionbar = false;

    public PlayerListener(FallbackServerAddon plugin) {
        this.plugin = plugin;
        checkActionBar();
    }

    private void checkActionBar() {
        try {
            Class.forName("net.md_5.bungee.api.ChatMessageType");
            actionbar = true;
        } catch (ClassNotFoundException ignored) {
            actionbar = false;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (actionbar && plugin.getConfig().getBoolean("settings.standalone.actionbar.enabled", false)) {
            ActionBarUtil.startActionBar(player, plugin.getConfig().getString("settings.standalone.actionbar.message", "&f"));
        }

        event.setJoinMessage(Utils.color(plugin.getConfig().getString("settings.standalone.join_message", null))
                .replace("%player_name%", player.getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        ActionBarUtil.stopActionBar(player);

        event.setQuitMessage(Utils.color(plugin.getConfig().getString("settings.standalone.quit_message", null))
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
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
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

    @SuppressWarnings("deprecation")
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
}
