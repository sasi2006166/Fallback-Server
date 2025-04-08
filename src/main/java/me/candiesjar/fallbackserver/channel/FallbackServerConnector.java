package me.candiesjar.fallbackserver.channel;

import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LoginSuccess;

public class FallbackServerConnector extends ServerConnector {

    private final ProxyServer proxyServer;
    private final UserConnection userConnection;
    private final BungeeServerInfo bungeeServerInfo;

    public FallbackServerConnector(ProxyServer bungee, UserConnection user, BungeeServerInfo target) {
        super(bungee, user, target);
        this.proxyServer = bungee;
        this.userConnection = user;
        this.bungeeServerInfo = target;
    }

    @Override
    public void exception(Throwable t) throws Exception {
        super.exception(t);
    }

    @Override
    public void handle(Kick kick) throws Exception {
        super.handle(kick);
    }

    @Override
    public void handle(LoginSuccess loginSuccess) throws Exception {
        super.handle(loginSuccess);
    }
}
