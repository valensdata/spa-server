package com.valens.spaserver;

import com.valens.spaserver.constants.KeyStoreType;
import com.valens.spaserver.constants.ServerParams;
import com.valens.spaserver.constants.TransportType;
import com.valens.spaserver.transport.TransportProvider;
import com.valens.spaserver.watch.FileWatchService;
import com.valens.spaserver.watch.FileWatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;

import java.util.HashMap;

public final class HttpStaticFileServer {

    static CachedFile cachedIndexFile;
    static String basePath;
    static String indexFilePath;

    final private static int DEFAULT_PORT = 5000;

    public static final HashMap<String, String> serverParamsMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        processServerParams(args);
        String rawBasePath = serverParamsMap.get(ServerParams.BASE_PATH);
        if (rawBasePath.endsWith("/")) {
            basePath = rawBasePath;
        } else {
            basePath = rawBasePath + "/";
        }
        FileWatchService fileWatchService = new FileWatchService(basePath);
        cachedIndexFile = new CachedFile(serverParamsMap, "/index.html", fileWatchService);

        indexFilePath = basePath + "index.html";

        FileWatcher fileWatcher = new FileWatcher(fileWatchService);
        fileWatcher.start();

        final SslContext sslCtx;
        if (serverParamsMap.containsKey(ServerParams.SSL_ENABLED)) {
            sslCtx = SslContextUtil.newInstance();
        } else {
            sslCtx = null;
        }

        final TransportProvider transportProvider = TransportProvider.newInstance();

        EventLoopGroup bossGroup = transportProvider.newEventLoopGroup(1);
        EventLoopGroup workerGroup = transportProvider.newEventLoopGroup();
        int port = Integer.valueOf(serverParamsMap.get(ServerParams.PORT));
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(transportProvider.getServerSocketChannelClass())
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpStaticFileServerInitializer(sslCtx));

            Channel ch = b.bind(port).sync().channel();

            System.err.println("Serving at " +
                    (sslCtx != null ? "https" : "http") + "://0.0.0.0:" + port + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            fileWatcher.join();
        }
    }

    private static void processServerParams(String[] args) {
        String currentParam = null;
        boolean sslEnabled = false;
        for (String argument: args) {
            if (currentParam == null) {
                switch (argument) {
                    case "--ssl":
                        serverParamsMap.put(ServerParams.SSL_ENABLED, "true");
                        sslEnabled = true;
                        break;
                    case "--epoll":
                        serverParamsMap.put(ServerParams.TRANSPORT_TYPE, TransportType.EPOLL);
                        break;
                    case "--kqueue":
                        serverParamsMap.put(ServerParams.TRANSPORT_TYPE, TransportType.KQUEUE);
                        break;
                    default:
                        currentParam = argument;
                }
            } else {
                switch(currentParam) {
                    case "--port":
                    case "-p":
                        serverParamsMap.put(ServerParams.PORT, argument);
                        break;
                    case "--directory":
                    case "-d":
                        serverParamsMap.put(ServerParams.BASE_PATH, argument);
                        break;
                    case "--cert":
                    case "-c":
                        serverParamsMap.put(ServerParams.CERTIFICATE_PATH, argument);
                        break;
                    case "--key":
                    case "-k":
                        serverParamsMap.put(ServerParams.PRIVATE_KEY_PATH, argument);
                        break;
                    case "--keystore-type":
                        serverParamsMap.put(ServerParams.KEYSTORE_TYPE, argument);
                        break;
                    case "--keystore-path":
                        serverParamsMap.put(ServerParams.KEYSTORE_PATH, argument);
                        break;
                    case "--keystore-password":
                        serverParamsMap.put(ServerParams.KEYSTORE_PASSWORD, argument);
                        break;
                    case "--placeholder-prefix":
                    case "-pp":
                        serverParamsMap.put(ServerParams.PLACEHOLDER_PREFIX, argument);
                        break;
                }
                currentParam = null;
            }
        }

        serverParamsMap.putIfAbsent(ServerParams.PORT, String.valueOf(DEFAULT_PORT));
        serverParamsMap.putIfAbsent(ServerParams.BASE_PATH, System.getProperty("user.dir"));
        serverParamsMap.putIfAbsent(ServerParams.TRANSPORT_TYPE, TransportType.NIO);
        serverParamsMap.putIfAbsent(ServerParams.KEYSTORE_TYPE, KeyStoreType.CERT_CHAIN);
    }

}
