package me.candiesjar.fallbackserver.bungee.utils;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.enums.ConfigFields;
import me.candiesjar.fallbackserver.bungee.enums.SpreadMode;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ServerGroup {

    private final FallbackServerBungee plugin;

    private final String name;
    private final List<RedirectServerWrapper> servers;
    private final List<RedirectServerWrapper> connected;
    private String parent;
    private boolean bottomKick;
    private boolean spread;
    private SpreadMode spreadMode;
    private String permission;

    /* Vars for spreading */
    private int spreadCounter = 0;
    private int minimalProgressive;

    public ServerGroup(FallbackServerBungee plugin, String name, boolean bottomKick, boolean spread, String parent, SpreadMode spreadMode, int minimalProgressive, String permission) {
        this.plugin = plugin;
        this.name = name;
        this.bottomKick = bottomKick;
        this.spread = spread;
        this.parent = parent;
        if(this.parent.equalsIgnoreCase("none"))
            parent = null;

        this.servers = new ArrayList<>();
        this.connected = new ArrayList<>();

        if(spreadMode == null) {
            this.spreadMode = SpreadMode.valueOf(ConfigFields.SPREAD_MODE.getString());
            this.minimalProgressive = ConfigFields.MAX_PLAYERS.getInt();
        } else {
            this.spreadMode = spreadMode;
            this.minimalProgressive = minimalProgressive;
        }

        if(permission.equalsIgnoreCase("")) this.permission = null;
        else this.permission = permission;
    }

    public ServerGroup(FallbackServerBungee plugin, String name, boolean bottomKick, boolean spread, String parent, List<String> aliases, SpreadMode spreadMode, int minimalProgressive, String permission) {
        this(plugin, name, bottomKick, spread, parent, spreadMode, minimalProgressive, permission);
    }

    public String getName() {
        return name;
    }

    public List<RedirectServerWrapper> getServers() {
        return servers;
    }

    public List<RedirectServerWrapper> getConnected() {
        return connected;
    }

    public boolean isBottomKick() {
        return bottomKick;
    }

    public void setBottomKick(boolean bottomKick) {
        this.bottomKick = bottomKick;
    }

    public boolean isSpread() {
        return spread;
    }

    public void setSpread(boolean spread) {
        this.spread = spread;
    }

    public void addServer(RedirectServerWrapper server) {
        this.servers.add(server);
    }

    public void addConnectedServer(RedirectServerWrapper server) {
        this.connected.add(server);
    }

    public ServerGroup getParent() {
        return this.plugin.getServerGroup(parent);
    }

    public RedirectServerWrapper getRedirectServer(ProxiedPlayer player, String oldServer, boolean useParent) {
        return getRedirectServer(player, oldServer, useParent, SpreadMode.CYCLE);
    }

    public RedirectServerWrapper getRedirectServer(ProxiedPlayer player, String oldServer, boolean useParent, SpreadMode spreadMode) {
        RedirectServerWrapper redirectServer = null;

        if(isRestricted()) {
            if(!player.hasPermission(permission)) {
                ServerGroup parent = getParent();
                if(parent == null || !useParent) return null;

                return parent.getRedirectServer(player, oldServer, useParent, spreadMode);
            }
        }

        List<RedirectServerWrapper> onlineServers = servers.stream().filter(server -> server.isOnline() && !server.getServerInfo().getName().equalsIgnoreCase(oldServer)).collect(Collectors.toList());

        if(onlineServers.size() == 0) {
            ServerGroup parent = getParent();
            if(parent == null || !useParent) return null;

            return parent.getRedirectServer(player, oldServer, useParent, spreadMode);
        }

        if(spread) {
            if(spreadMode == SpreadMode.CYCLE) {
                spreadCounter++;
                if (spreadCounter >= onlineServers.size()) {
                    spreadCounter = 0;
                }

                redirectServer = onlineServers.get(spreadCounter);
            }
            else if(spreadMode == SpreadMode.PROGRESSIVE) {
                onlineServers.sort((o1, o2) -> {
                    return Integer.compare(o2.getOnlinePlayersCount(), o1.getOnlinePlayersCount());
                });

                for (RedirectServerWrapper serverWrapper : onlineServers) {
                    if (serverWrapper.getOnlinePlayersCount() < minimalProgressive) {
                        return serverWrapper;
                    }
                }

                return getRedirectServer(player, oldServer, useParent, SpreadMode.LOWEST);
            }
            else if(spreadMode == SpreadMode.LOWEST) {
                return onlineServers.stream().min(Comparator.comparing(RedirectServerWrapper::getOnlinePlayersCount)).get();
            }

        } else redirectServer = onlineServers.get(0);

        return redirectServer;
    }

    public int getAvailableServersSize() {
        return (int) servers.stream().filter(RedirectServerWrapper::isOnline).count();
    }

    public void setParent(String parentName) {
        this.parent = parentName;
    }

    public SpreadMode getSpreadMode() {
        return spreadMode;
    }

    public void setSpreadMode(SpreadMode spreadMode) {
        this.spreadMode = spreadMode;
    }

    public int getMinimalProgressive() {
        return minimalProgressive;
    }

    public void setMinimalProgressive(int minimalProgressive) {
        this.minimalProgressive = minimalProgressive;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isRestricted() {
        return permission != null;
    }
}