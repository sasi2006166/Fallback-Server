package me.candiesjar.fallbackserver.bungee.utils;

import net.md_5.bungee.api.config.ServerInfo;

public class RedirectServerWrapper {

    private final ServerInfo serverInfo;
    private boolean isOnline;
    private final ServerGroup serverGroup;
    private final boolean redirectable;
    private int onlinePlayersCount;

    private boolean allowAliases = true;

    public RedirectServerWrapper(ServerInfo serverInfo){
        this(serverInfo, null, false);
    }

    public RedirectServerWrapper(ServerInfo serverInfo, ServerGroup serverGroup, boolean redirectable) {
        this.serverInfo = serverInfo;
        this.serverGroup = serverGroup;
        this.redirectable = redirectable;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public ServerGroup getServerGroup() {
        return serverGroup;
    }

    public boolean isRedirectable() {
        return redirectable;
    }

    public int getOnlinePlayersCount() {
        return onlinePlayersCount;
    }

    public void setOnlinePlayersCount(int onlinePlayersCount) {
        this.onlinePlayersCount = onlinePlayersCount;
    }

    public void addProxiedPlayer() {
        onlinePlayersCount++;
    }

    public void removeProxiedPlayer() {
        onlinePlayersCount--;
    }

    public boolean isAllowAliases() {
        return allowAliases;
    }

    public void setAllowAliases(boolean allowAliases) {
        this.allowAliases = allowAliases;
    }
}