package me.candiesjar.fallbackserver.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.netty.HandlerBoss;

public class BasicChannelInitializer extends ChannelInitializer<Channel> {

    private final ProxyServer proxyServer;
    private final UserConnection userConnection;
    private final BungeeServerInfo bungeeServerInfo;

    public BasicChannelInitializer(ProxyServer proxyServer, UserConnection userConnection, BungeeServerInfo bungeeServerInfo) {
        this.proxyServer = proxyServer;
        this.userConnection = userConnection;
        this.bungeeServerInfo = bungeeServerInfo;
    }

    @Override
    protected void initChannel(Channel channel) {
        BungeeCord.getInstance()
                .unsafe()
                .getBackendChannelInitializer()
                .getChannelAcceptor()
                .accept(channel);

        channel.pipeline().get(HandlerBoss.class)
                .setHandler(new FallbackServerConnector(proxyServer, userConnection, bungeeServerInfo));
    }
}
