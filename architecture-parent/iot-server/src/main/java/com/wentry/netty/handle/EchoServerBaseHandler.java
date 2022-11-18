package com.wentry.netty.handle;

import com.wentry.netty.server.EchoServer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author rukawa
 */
@ChannelHandler.Sharable
public class EchoServerBaseHandler extends ChannelInboundHandlerAdapter {

    private transient static Logger logger = LoggerFactory.getLogger(EchoServerBaseHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Object clientID = ctx.channel().attr(AttributeKey.valueOf("ClientID")).get();
        System.out.println(clientID + "close.");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("inbound msg:\n\r" + msg);
        if (msg instanceof HttpRequest) {
            // 请求，解码器将请求转换成HttpRequest对象
            HttpRequest request = (HttpRequest) msg;

            // 获取请求参数
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            String name = "netty";
            if (queryStringDecoder.parameters().get("name") != null) {
                name = queryStringDecoder.parameters().get("name").get(0);
            }

            // 响应HTML
            String responseHtml = "<html><body>Hello, " + name + "</body></html>";
            byte[] responseBytes = responseHtml.getBytes(StandardCharsets.UTF_8);
            int contentLength = responseBytes.length;

            // 构造FullHttpResponse对象，FullHttpResponse包含message body
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseBytes));
            response.headers().set("Content-Type", "text/html; charset=utf-8");
            response.headers().set("Content-Length", Integer.toString(contentLength));

            ctx.writeAndFlush(response);
        } else {
            String outbound = "receive ok.";
            ctx.writeAndFlush(outbound);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
