package com.valens.spaserver.transport;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;

public class KQueueTransportProvider implements TransportProvider {

    @Override
    public Class<? extends ServerChannel> getServerSocketChannelClass() {
        return KQueueServerSocketChannel.class;
    }

    @Override
    public EventLoopGroup newEventLoopGroup() {
        return new KQueueEventLoopGroup();
    }

    @Override
    public EventLoopGroup newEventLoopGroup(int n) {
        return new KQueueEventLoopGroup(n);
    }

}
