package me.candiesjar.fallbackserver.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.Protocol;

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
    protected void initChannel(Channel channel) throws Exception {
        PipelineUtils.BASE.initChannel(channel);
        channel.pipeline().addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder(Protocol.HANDSHAKE, false, userConnection.getPendingConnection().getVersion()));
        channel.pipeline().addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder(Protocol.HANDSHAKE, false, userConnection.getPendingConnection().getVersion()));
        channel.pipeline().get(HandlerBoss.class).setHandler(new ServerConnector(proxyServer, userConnection, bungeeServerInfo));
    }
}
