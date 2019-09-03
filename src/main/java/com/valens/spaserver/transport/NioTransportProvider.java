package com.valens.spaserver.transport;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NioTransportProvider implements TransportProvider {

    @Override
    public Class<? extends ServerChannel> getServerSocketChannelClass() {
        return NioServerSocketChannel.class;
    }

    @Override
    public EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }

    @Override
    public EventLoopGroup newEventLoopGroup(int n) {
        return new NioEventLoopGroup(n);
    }

}
