package com.valens.spaserver.handler;

import com.valens.spaserver.CachedFile;
import com.valens.spaserver.HttpStaticFileServer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedStream;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class IndexFileHandler extends AbstractFileHandler {

    private final CachedFile cachedIndexFile = HttpStaticFileServer.cachedIndexFile;

    @Override
    public void process(ChannelHandlerContext ctx, FullHttpRequest request, String path) throws Exception {

        final boolean keepAlive = HttpUtil.isKeepAlive(request);

        // Cache Validation
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        long fileLastModifiedSeconds;
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            fileLastModifiedSeconds = cachedIndexFile.getFileLastModifiedSeconds();

            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx, keepAlive);
                return;
            }
        } else {
            fileLastModifiedSeconds = cachedIndexFile.getFileLastModifiedSeconds();
        }

        final long fileLength = cachedIndexFile.getFileContent().length;

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);
        setDateAndCacheHeaders(response, fileLastModifiedSeconds);

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ChannelFuture lastContentFuture;

        if (ctx.pipeline().get(SslHandler.class) == null) {
            ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(new ByteArrayInputStream(cachedIndexFile.getFileContent()))));
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            lastContentFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(new ByteArrayInputStream(cachedIndexFile.getFileContent()))));
        }

        if (!keepAlive) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
