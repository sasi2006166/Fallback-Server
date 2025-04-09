package me.candiesjar.fallbackserver.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.md_5.bungee.BungeeCord;

public class PingChannelInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel channel) {
        BungeeCord.getInstance()
                .unsafe()
                .getBackendChannelInitializer()
                .getChannelAcceptor()
                .accept(channel);
    }
}
