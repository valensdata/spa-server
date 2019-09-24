package com.valens.spaserver.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

abstract public class AbstractFileHandler implements Handler {

    abstract public void process(ChannelHandlerContext ctx, FullHttpRequest request, String path) throws Exception;

}
