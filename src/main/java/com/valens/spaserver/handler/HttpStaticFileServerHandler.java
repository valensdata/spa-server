package com.valens.spaserver.handler;

import com.valens.spaserver.HttpStaticFileServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class HttpStaticFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements Handler {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        if (!GET.equals(request.method())) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = request.uri();

        final String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        if (path.equals(HttpStaticFileServer.indexFilePath)) {
            HttpStaticFileServer.indexFileHandler.process(ctx, request, path);
        } else {
            HttpStaticFileServer.fileHandler.process(ctx, request, path);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof DecoderException)) {
            cause.printStackTrace();
        }
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.equals("/")) {
            return HttpStaticFileServer.indexFilePath;
        }

        if (uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        String basePath = HttpStaticFileServer.basePath;

        uri = basePath + uri;

        File f = new File(uri);
        if (!f.exists()) {
            return HttpStaticFileServer.indexFilePath;
        }

        if (f.isDirectory()) {
            return null;
        }

        return uri;
    }

}
