package me.candiesjar.fallbackserver.hook.impl;

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import me.candiesjar.fallbackserver.hook.api.IPluginHook;
import net.md_5.bungee.api.plugin.Plugin;

public class Maintenance implements IPluginHook {

    private final Plugin plugin;
    private MaintenanceProxy api;

    public Maintenance(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return "Maintenance";
    }

    @Override
    public boolean isAvailable() {
        return plugin.getProxy().getPluginManager().getPlugin("Maintenance") != null
                && plugin.getProxy().getPluginManager().getPlugin("Maintenance").getDescription().getAuthor().equals("kennytv");
    }

    @Override
    public void register() {
        if (!isAvailable()) {
            return;
        }

        api = (MaintenanceProxy) MaintenanceProvider.get();
    }

    @Override
    public void unregister() {

    }
}
