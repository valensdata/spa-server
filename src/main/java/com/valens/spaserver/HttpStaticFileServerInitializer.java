package com.valens.spaserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpStaticFileServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final ServerParams serverParams;
    private final CachedFile cachedIndexFile;

    public HttpStaticFileServerInitializer(SslContext sslCtx, ServerParams serverParams, CachedFile cachedIndexFile) {
        this.sslCtx = sslCtx;
        this.serverParams = serverParams;
        this.cachedIndexFile = cachedIndexFile;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.channel().attr(HttpStaticFileServer.SERVER_PARAMS).set(serverParams);
        pipeline.channel().attr(HttpStaticFileServer.INDEX_CACHED).set(cachedIndexFile);
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpStaticFileServerHandler());
    }
}
