package com.valens.spaserver;

import com.valens.spaserver.watch.FileWatchService;
import com.valens.spaserver.watch.FileWatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AttributeKey;

import java.io.File;

public final class HttpStaticFileServer {

    public static final AttributeKey<ServerParams> SERVER_PARAMS = AttributeKey.valueOf("serverParams");
    public static final AttributeKey<CachedFile> INDEX_CACHED = AttributeKey.valueOf("indexCached");

    public static void main(String[] args) throws Exception {
        ServerParams serverParams = processServerParams(args);
        FileWatchService fileWatchService = new FileWatchService(serverParams.getBasePath());
        CachedFile cachedIndexFile = new CachedFile(serverParams, "/index.html", fileWatchService);
        FileWatcher fileWatcher = new FileWatcher(fileWatchService);
        fileWatcher.start();

        // Configure SSL.
        final SslContext sslCtx;
        if (serverParams.getCertificatePath() != null) {
            sslCtx = SslContextBuilder.forServer(new File(serverParams.getCertificatePath()), new File(serverParams.getPrivateKeyPath())).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpStaticFileServerInitializer(sslCtx, serverParams, cachedIndexFile));

            Channel ch = b.bind(serverParams.getPort()).sync().channel();

            System.err.println("Serving at " +
                    (serverParams.getCertificatePath() != null ? "https" : "http") + "://0.0.0.0:" + serverParams.getPort() + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            fileWatcher.join();
        }
    }

    private static ServerParams processServerParams(String[] args) {
        ServerParams.Builder builder = ServerParams.Builder.newInstance();

        String currentParam = null;

        for (String argument: args) {
            if (currentParam == null) {
                currentParam = argument;
            } else {
                switch(currentParam) {
                    case "--port":
                    case "-p":
                        builder.setPort(Integer.valueOf(argument));
                        break;
                    case "--directory":
                    case "-d":
                        builder.setBasePath(argument);
                        break;
                    case "--cert":
                    case "-c":
                        builder.setCertificatePath(argument);
                        break;
                    case "--key":
                    case "-k":
                        builder.setPrivateKeyPath(argument);
                        break;
                    case "--placeholder-prefix":
                    case "-pp":
                        builder.setPlaceholderPrefix(argument);
                        break;
                }
                currentParam = null;
            }
        }

        return builder.build();
    }

}
