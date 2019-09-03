package com.valens.spaserver.transport;

import com.valens.spaserver.HttpStaticFileServer;
import com.valens.spaserver.constants.ServerParams;
import com.valens.spaserver.constants.TransportType;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;

public interface TransportProvider {

    static TransportProvider newInstance() {
        switch (HttpStaticFileServer.serverParamsMap.get(ServerParams.TRANSPORT_TYPE)) {
            case TransportType.NIO:
                return new NioTransportProvider();
            case TransportType.EPOLL:
                return new EpollTransportProvider();
            case TransportType.KQUEUE:
                return new KQueueTransportProvider();
            default:
                throw new UnsupportedOperationException();
        }
    }

    Class<? extends ServerChannel> getServerSocketChannelClass();

    EventLoopGroup newEventLoopGroup();

    EventLoopGroup newEventLoopGroup(int n);
}
