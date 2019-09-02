package com.valens.spaserver;

import com.valens.spaserver.constants.ServerParams;
import com.valens.spaserver.watch.FileWatchService;
import com.valens.spaserver.watch.FileWatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;

import java.io.File;
import java.util.HashMap;

public final class HttpStaticFileServer {

    static CachedFile cachedIndexFile;
    static String basePath;
    static String indexFilePath;

    static final HashMap<String, String> serverParamsMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        processServerParams(args);
        String rawBasePath = serverParamsMap.get(ServerParams.BASE_PATH);
        if (rawBasePath.endsWith("/")) {
            basePath = rawBasePath;
        } else {
            basePath = rawBasePath + File.separatorChar;
        }
        FileWatchService fileWatchService = new FileWatchService(basePath);
        cachedIndexFile = new CachedFile(serverParamsMap, "/index.html", fileWatchService);

        indexFilePath = basePath + "index.html";

        FileWatcher fileWatcher = new FileWatcher(fileWatchService);
        fileWatcher.start();

        // Configure SSL.
        final SslContext sslCtx;
        if (serverParamsMap.containsKey(ServerParams.SSL_ENABLED)) {
            sslCtx = SslContextUtil.newInstance();
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        int port = Integer.valueOf(serverParamsMap.get(ServerParams.PORT));
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
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
        for (String argument: args) {
            if (currentParam == null) {
                switch (argument) {
                    case "--ssl":
                        serverParamsMap.put(ServerParams.SSL_ENABLED, "true");
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

        if (!serverParamsMap.containsKey(ServerParams.PORT)) {
            if (serverParamsMap.containsKey(ServerParams.SSL_ENABLED)) {
                serverParamsMap.put(ServerParams.PORT, "8443");
            } else {
                serverParamsMap.put(ServerParams.PORT, "8080");
            }
        }
        if (!serverParamsMap.containsKey(ServerParams.BASE_PATH)) {
            serverParamsMap.put(ServerParams.BASE_PATH, System.getProperty("user.dir"));
        }
    }

}
