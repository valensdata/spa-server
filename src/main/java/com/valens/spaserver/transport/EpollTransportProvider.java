package com.valens.spaserver.transport;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

public class EpollTransportProvider implements TransportProvider {

    @Override
    public Class<? extends ServerChannel> getServerSocketChannelClass() {
        return EpollServerSocketChannel.class;
    }

    @Override
    public EventLoopGroup newEventLoopGroup() {
        return new EpollEventLoopGroup();
    }

    @Override
    public EventLoopGroup newEventLoopGroup(int n) {
        return new EpollEventLoopGroup(n);
    }

}
